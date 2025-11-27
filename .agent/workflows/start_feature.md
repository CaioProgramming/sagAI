---
description: Start a new feature development cycle by creating a fresh branch from develop
---

1. Ask the user for the feature name if not already provided.
2. Ensure the repository is clean (no uncommitted changes). If there are changes, ask the user to
   stash or commit them.
3. Switch to the develop branch: `git checkout develop`
4. Pull the latest changes: `git pull origin develop`
5. Create and switch to the new feature branch: `git checkout -b feature/<feature_name>`
6. Confirm to the user that they are now on the new branch and ready to start.
