# Plugin Guidance

Plugins should support judgment, not replace it. Install or enable only what the task needs.

Useful plugin categories for Ask:

- current documentation lookup;
- browser or Playwright verification;
- backend deployment and hosting tools;
- GitHub PR/issue/CI tools;
- code review tools;
- frontend design and product design tools when UI is in scope;
- documents/spreadsheets/presentations when producing formal artifacts.

Treat duplicate plugin families carefully. Prefer one working equivalent instead of loading conflicting tools.

Do not copy plugin caches, marketplace internals, authenticated state, generated runtime files, or local configuration into the repository.
