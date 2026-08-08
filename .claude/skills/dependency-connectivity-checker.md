---
name: dependency-connectivity-checker
description: Traces every class in a feature to verify all layers are connected — no orphaned services, no forgotten injections, no broken dependency chains. Catches the "it works up to this point but this dependency is forgotten" class of bugs.
user-invocable: true
allowed-tools: Read, Glob, Grep, Bash
---

# Dependency Connectivity Checker

Traces the full dependency chain of a feature to verify every part is wired, every class is reachable, and no component sits in isolation waiting to be connected.

## When to Run
- After creating multiple new files for a feature (3+ new classes)
- Before claiming a multi-layer feature is complete
- When a feature "seems done" but hasn't been tested end-to-end
- Proactively after any significant cross-package implementation

## Process

### Step 1: Map the feature's component graph

Identify every file created or modified for the feature. Build the full list:

```
Grepping for new files:
  Glob: **/decision/**/*.java → all decision-package files
  Glob: **/basic/api/dto/Decision*.java → decision DTOs
  Glob: **/basic/api/dto/Criterion*.java → criterion DTOs
  Glob: **/basic/application/decision/*.java → decision domain model
```

List every class with its layer role: Controller, Processor, Service, Mapper, DTO, Entity, Repository, AI Caller, Prompt.

### Step 2: Trace the call chain from entry point

Start at the controller (or processor if no controller) and follow every dependency:

```
DecisionController
  ├── @Autowired DeepSeekClarificationStructurer  → EXISTS? ✓/✗
  ├── @Autowired CompareService                   → EXISTS? ✓/✗
  │
  ├── clarify() calls:
  │   clarificationStructurer.clarify(rawQuery, mode, category, city, language)
  │   → method signature MATCHES? params match DTO fields? ✓/✗
  │
  └── compare() calls:
      compareService.compare(mode, resultIds, locale)
      → method signature MATCHES? ✓/✗
```

For each arrow in the call chain, verify:
1. The target class exists (no import pointing to non-existent class)
2. The constructor/field injection has a matching `@Component`/`@Service`/`@Repository`
3. The method signature matches the call site (param types, return type)
4. The target class's own dependencies are resolvable (recurse)

### Step 3: Verify Spring wiring

For every `@Component`, `@Service`, `@Repository`:
- It has `@RequiredArgsConstructor` or a constructor
- All `private final` fields are `@Component`/`@Service`/`@Repository` beans (or `@Value` strings)
- No non-bean `private final` fields that would fail injection

### Step 4: Check DTO flow — no missing population

For every Response DTO with fields, trace where those fields are set:

```
SearchCardResponse.decisionLabel
  Set in: StructuredSearchProcessor.toCard() line 474 ✓
  Source: ranked.evaluation.getDecisionLabel()

SearchCardResponse.criterionAssessments
  Set in: StructuredSearchProcessor.toCard() line 475-492 ✓
  Source: ranked.evaluation.getCriterionAssessments() → CriterionAssessmentResponse

SearchCardResponse.comparisonFacts
  Set in: StructuredSearchProcessor.toCard() line 496-504 ✓
  Source: ranked.evaluation.getComparisonFacts() → CriterionEvidenceResponse
```

Flag any Response DTO field that is declared but never populated by a builder/mapper/setter.

### Step 5: Check Prompt → AI Caller chain

For each `classpath:prompts/*.md` file:
- There must be exactly one `@Value("classpath:prompts/...")` Resource field that loads it
- That Resource must be read by exactly one method (`readPrompt()`)
- That method's consumer must be an AI caller class injected into a Processor or Controller
- The AI caller's output must be parsed and used by something downstream

```
decision-evaluator.md
  Loaded by: DeepSeekDecisionEvaluator.promptResource ✓
  Read by: DeepSeekDecisionEvaluator.readPrompt() ✓
  Caller: DeepSeekDecisionEvaluator.callDeepSeek() ✓
  Result consumed by: DeepSeekDecisionEvaluator.parseEvaluations() → CandidateEvaluation list
  Downstream: StructuredSearchProcessor.evaluateCandidates() → RankedDocument.evaluation → SearchCardResponse ✓
```

### Step 6: Check config/properties chain

For every `@Value` annotation used by the feature:
- The property key is documented (or uses a default)
- If it's a required key (no default), the application.yml contains it
- The property's type matches the `@Value` field type

### Step 7: Check error path connectivity

For every `throw new XxxException` in the feature:
- That exception class exists in `kz.ask.shared.error`
- The ErrorCode constant exists
- `GlobalExceptionHandler` has a handler for that exception type
- The HTTP status matches what the spec documents for that scenario

### Step 8: Report

```
CONNECTIVITY REPORT
===================
Components: N created, M modified
Dependencies: N total, M verified, K broken, J uncertain

BROKEN CHAINS:
  [list each — missing injection, orphaned class, wrong method signature]

UNPOPULATED FIELDS:
  [list each Response DTO field with no setter]

PROMPT → CODE GAPS:
  [list each prompt file with no downstream consumer]

ERROR PATH GAPS:
  [list each throw without exception handler coverage]
```

If zero issues: "All dependencies connected. Feature graph is fully wired."

## Anti-patterns to flag
- New `@Component` created but never injected anywhere
- Response DTO field declared but never populated
- AI prompt file created but never loaded by any Java code
- Method parameter added but caller not updated
- Constructor injection field mismatch (wrong type, missing bean)
- Layer violation: Processor injects Repository, Service returns Entity, etc.
