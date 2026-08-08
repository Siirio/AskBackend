---
name: spec-task-checker
description: Rechecks completed implementation tasks against the spec markdown. Maps every spec requirement to code evidence, flags gaps, and verifies acceptance criteria. Run after completing a feature phase to catch spec drift before it compounds.
user-invocable: true
allowed-tools: Read, Glob, Grep, Bash
---

# Spec Task Checker

Rechecks completed implementation work against the specification document. Every spec section and requirement must have corresponding code evidence — no "I think I did that" allowed.

## When to Run
- After completing a feature phase or major implementation milestone
- Before claiming a task is "done" when the spec has numbered requirements
- When the user asks "does this match the spec?" or "проверь по спеке"
- Proactively after writing code that implements a spec section

## Process

### Step 1: Locate the spec

The spec is at `C:\MyProjects\Team\Ask\NOT_TO_COMMIT\ask-decision-search-backend-spec.md` (or the path the user provided).

Read the entire spec. Extract every numbered requirement, every "must"/"необходимо"/"требуется" statement, and every acceptance criterion. Build a checklist.

### Step 2: Map spec requirements → code evidence

For each requirement, find the code that satisfies it. Be specific:

| Spec section | Requirement | Code evidence | Status |
|---|---|---|---|
| 5. Clarification | POST /api/v1/search/clarification | DecisionController.java:34 clarify() | COVERED |
| 8.2 Grounded evaluation | CriterionAssessment with statuses | CriterionAssessment.java + DeepSeekDecisionEvaluator.java:111 parseAssessments() | COVERED |
| 18.3 | Attribute change → new projection | SearchProjectionComposer.java tokens/embeddingText | COVERED |

**Evidence must include a file path and method/class name.** Never write "seems covered" or "probably done."

### Step 3: Flag every gap

For each requirement with NO code evidence:

```
GAP: [spec section] [requirement]
  Spec says: [exact spec quote]
  Expected: [what should exist]
  Actual: [what exists or doesn't]
  Files to create/modify: [concrete list]
```

Do NOT fill gaps automatically. Report the full gap list to the user first, then offer to fix.

### Step 4: Acceptance criteria audit

Read the spec's acceptance criteria section (typically at the end). For each numbered criterion, confirm the behavior is guaranteed by code:

```
AC-1: "Запрос ноутбук для Java... не создаёт характеристику Docker/Java у ноутбука"
  Guarded by: decision-evaluator.md prompt line 8 — software names are use-cases
  Code enforcement: DeepSeekDecisionEvaluator hasInvalidEvidence() validates evidence against actual attributes
  Status: COVERED / NOT COVERED
```

### Step 5: Cross-reference check

Verify these cross-document relationships are intact:
- Every endpoint in the spec → has a Controller method
- Every request field in the spec → has a DTO field with validation annotations
- Every response field in the spec → has a Response DTO field
- Every error scenario in the spec → has a thrown exception with matching status
- Every AI prompt referenced → exists in classpath:prompts/

### Step 6: Report

Output a structured report:

```
SPEC CHECK REPORT
=================
Requirements: N total, M covered, K gaps, J uncertain
Acceptance criteria: N total, M covered, K gaps

GAPS:
  [list each gap with concrete fix location]

UNCERTAIN (ask user):
  [list requirements where code exists but spec intent is ambiguous]
```

## Anti-patterns to flag
- Requirements satisfied "by convention" without explicit code
- Spec says "must" but code has no enforcement
- Code exists but doesn't match the spec's described behavior
- Field added to DTO but not populated by the service layer
- AI prompt describes a behavior but no Java code ever triggers it
