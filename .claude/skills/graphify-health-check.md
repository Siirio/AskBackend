---
name: graphify-health-check
description: Run graphify-powered health analysis on the codebase — find dead code, doc-code mismatches, architecture violations, unused endpoints, and low-cohesion hotspots. Use after /graphify or when the user asks "what's broken", "what endpoints are dead", "what parts aren't working", or similar diagnostic questions.
type: diagnostic
---

# graphify-health-check

Analyze a graphify-built knowledge graph to surface what's broken, unused, improperly wired, or documented-but-not-implemented. Runs after `graphify` has already built `graphify-out/graph.json`.

## Trigger words

"what's broken", "dead code", "dead endpoints", "unused", "doc mismatch", "what parts aren't working", "improper usage", "architecture health", "code health", "what should I fix"

## Prerequisites

`graphify-out/graph.json` must exist. If missing, run `/graphify` on the target directory first.

## Analysis Pipeline

Run these analyses in order. All use the Python interpreter stored at `graphify-out/.graphify_python`.

### 1. Architecture Violations

Read `AI_Knowledge/features/search/ai-architecture.md` (and any other `*-architecture.md` files found via Glob) for the "Current violations to remove" section. Cross-reference each violation against the graph: does the code still match the violation description?

```bash
$(cat graphify-out/.graphify_python) -c "
import json
from pathlib import Path
g = json.loads(Path('graphify-out/graph.json').read_text(encoding='utf-8'))
# Find nodes in known-violation files
for n in g['nodes']:
    src = n.get('source_file','')
    label = n.get('label','')
    if 'StructuredSearchProcessor' in label or 'SearchTermEnricher' in label or 'BusinessProductProcessor' in label:
        if 'src/main/java' in src:
            print(f'VIOLATION: {label} still exists at {src}')
"
```

### 2. Dead Endpoint Mappings

Count `@GetMapping` / `@PostMapping` / etc. nodes with zero incoming edges. These are endpoints that nothing calls internally.

```bash
$(cat graphify-out/.graphify_python) -c "
import json
from collections import Counter
from pathlib import Path
g = json.loads(Path('graphify-out/graph.json').read_text(encoding='utf-8'))
nodes_by_id = {n['id']: n for n in g['nodes']}
incoming = Counter()
for e in g['links']:
    incoming[e['target']] += 1
mapping_labels = {'GetMapping','PostMapping','PatchMapping','DeleteMapping','PutMapping','RequestMapping'}
dead = [(mid, nodes_by_id[mid]['label'], nodes_by_id[mid]['source_file'])
        for mid in [n['id'] for n in g['nodes'] if n['label'] in mapping_labels]
        if incoming[mid] == 0]
print(f'Mappings with zero callers: {len(dead)}')
for mid, label, src in dead:
    print(f'  {label} in {src}')
"
```

### 3. Service/Processor Methods with No Callers

Find methods in `*ServiceImpl` or `*Processor` classes that have zero incoming edges — these exist but nothing uses them.

```bash
$(cat graphify-out/.graphify_python) -c "
import json
from collections import Counter
from pathlib import Path
g = json.loads(Path('graphify-out/graph.json').read_text(encoding='utf-8'))
nodes_by_id = {n['id']: n for n in g['nodes']}
incoming = Counter()
for e in g['links']:
    incoming[e['target']] += 1
generics = {'Override','Transactional','Component','RequiredArgsConstructor','Logger','Getter','Setter','Builder'}
for n in g['nodes']:
    nid = n['id']
    label = n.get('label','')
    src = n.get('source_file','')
    if 'src/main/java' not in src: continue
    if label in generics: continue
    if ('serviceimpl_' in nid.lower() or 'processor_' in nid.lower()) and incoming[nid] == 0:
        print(f'  {label} ({src})')
"
```

### 4. Doc Concepts with No Code Connection

Find documentation concepts (from `AI_Knowledge/`, `docs/`, `AGENTS.md`, `CLAUDE.md`) that have zero edges connecting them to any code node. These are documented ideas that may not be implemented.

```bash
$(cat graphify-out/.graphify_python) -c "
import json
from collections import Counter
from pathlib import Path
g = json.loads(Path('graphify-out/graph.json').read_text(encoding='utf-8'))
nodes_by_id = {n['id']: n for n in g['nodes']}
incoming = Counter()
outgoing = Counter()
for e in g['links']:
    incoming[e['target']] += 1
    outgoing[e['source']] += 1
doc_paths = ['ai_knowledge','/docs/','agents.md','claude.md','readme.md','.claude/skills']
generics = {'Getter','Setter','Builder','Entity','Table','Override','Transactional','Component',
            'Repository','RestController','Lock','AllArgsConstructor','NoArgsConstructor'}
for n in g['nodes']:
    src = n.get('source_file','').lower()
    label = n.get('label','')
    if label in generics or len(label) < 4: continue
    if not any(p in src for p in doc_paths): continue
    deg = incoming.get(n['id'],0) + outgoing.get(n['id'],0)
    if deg <= 1:
        print(f'  [{deg}] {label}')
"
```

### 5. Layer Direction Violations

Check for edges where a Processor or domain service calls into the API layer (wrong direction).

```bash
$(cat graphify-out/.graphify_python) -c "
import json
from pathlib import Path
g = json.loads(Path('graphify-out/graph.json').read_text(encoding='utf-8'))
nodes_by_id = {n['id']: n for n in g['nodes']}
for e in g['links']:
    src_node = nodes_by_id.get(e['source'], {})
    tgt_node = nodes_by_id.get(e['target'], {})
    src_src = src_node.get('source_file','')
    tgt_src = tgt_node.get('source_file','')
    is_src_processor = 'processor' in src_src.lower() and 'application' in src_src.lower()
    is_tgt_controller = 'controller' in tgt_src.lower() and 'api' in tgt_src.lower()
    if is_src_processor and is_tgt_controller:
        print(f'LAYER VIOLATION: {src_node.get(\"label\")} -> {tgt_node.get(\"label\")} [{e.get(\"relation\")}]')
"
```

### 6. Low-Cohesion Communities

Read `graphify-out/GRAPH_REPORT.md` and flag any community with cohesion < 0.10. These are candidates for splitting.

### 7. Isolated Enum Values

Enum files with many zero-inbound-edge constants are likely dead error codes or unused types.

```bash
$(cat graphify-out/.graphify_python) -c "
import json
from collections import Counter, defaultdict
from pathlib import Path
g = json.loads(Path('graphify-out/graph.json').read_text(encoding='utf-8'))
incoming = Counter()
for e in g['links']:
    incoming[e['target']] += 1
by_file = defaultdict(list)
for n in g['nodes']:
    src = n.get('source_file','')
    if 'src/main/java' not in src or '/enums/' not in src: continue
    if incoming[n['id']] == 0 and n['label'] not in ('Getter','Setter','AllArgsConstructor'):
        by_file[src].append(n['label'])
for f, syms in sorted(by_file.items()):
    if len(syms) >= 3:
        print(f'{len(syms)} dead: {f} -> {syms[:8]}')
"
```

## Output Format

Report findings in this order (most actionable first):

1. **Architecture Violations** — known violations that still exist in code
2. **Dead Endpoints** — mappings with no callers
3. **Unused Service Methods** — uncalled processor/service methods
4. **Doc-Code Gaps** — documented concepts with no code anchor (skip skills/CLAUDE.md governance concepts — those are meta)
5. **Layer Violations** — wrong-direction calls
6. **Low Cohesion** — communities that should be split

For each finding: state whether it's a confirmed issue or needs manual verification (e.g., enum values may be used via reflection).

## Notes

- Enum constants and error codes often show as "dead" because they're resolved via reflection or framework conventions that AST can't trace. Flag them but don't claim certainty without manual grep verification.
- Documentation concepts from CLAUDE.md, AGENTS.md, and .claude/skills/ are meta-documentation — they describe the development process, not the product. Skip these when reporting doc-code gaps unless the user specifically asks about them.
- Dangling edges (2,373 in the current graph) are normal — they're external library references that AST can't resolve. Don't flag these.
