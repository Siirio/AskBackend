# code-rules-checker

Validate recent code changes against AI_Knowledge/CodeRules.md.

## Execution
1. git diff to get changed files
2. For each .java file: check against Java/Spring rules
3. Flag: naming violations, comments, hardcoded values, entity leaks, swallowed exceptions
4. Report: VIOLATION | {file}:{line} | {rule} | {fix}
5. Zero violations → "CodeRules: passed."
6. Write violations to Changelog.md. Critical → alert user. Minor → offer auto-fix.
