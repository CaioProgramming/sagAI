---
description: Automatically detects uncommitted changes, generates a descriptive commit message, and commits them.
---

1. Check for uncommitted changes using `git status --porcelain`.
2. If there are no changes, print "No uncommitted changes found." and exit this workflow.
3. If there are changes:
   a. Run `git diff` to see the changes.
   b. Generate a concise and descriptive commit message based on the changes.
   c. Run `git add .` to stage all changes.
   d. Run `git commit -m "<generated_message>"` to commit the changes.
