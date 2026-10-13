#!/bin/bash
# Frees the temporary files an agent session leaves behind.
#   clean-temp.sh                 safe: pulled saga databases, screenshots, old temp logs and task output
#   clean-temp.sh --sessions N    also lists session transcripts older than N days (dry run)
#   clean-temp.sh --sessions N --yes   ...and deletes them. Never touches the memory/ folder.
TMP=/private/tmp/claude-501
PROJ="$HOME/.claude/projects/-Users-caioferreira-Documents-Develop-Sagas-sagas-android"
days=""; yes=0
while [ $# -gt 0 ]; do case "$1" in --sessions) days="$2"; shift;; --yes) yes=1;; esac; shift; done
size() { du -sk "$1" 2>/dev/null | cut -f1; }
before=$(size "$TMP")

# Copies of the player's real saga database: large and private, never worth keeping.
find "$TMP" -type f \( -name "SagaDatabase*" -o -name "*.db" -o -name "*.db-wal" -o -name "*.db-shm" \) -delete 2>/dev/null
# Screenshots, temp copies and anything older than a day.
find "$TMP" -type f \( -name "*.png" -o -name "*.jpg" \) -mtime +0 -delete 2>/dev/null
find "$TMP" -type f -mtime +1 -delete 2>/dev/null
find "$TMP" -mindepth 1 -type d -empty -delete 2>/dev/null
after=$(size "$TMP")
echo "tmp: ${before:-0} KB -> ${after:-0} KB"

if [ -n "$days" ]; then
  echo "session transcripts older than $days days in $PROJ (memory/ is never touched):"
  old=$(find "$PROJ" -mindepth 1 -maxdepth 1 -mtime +"$days" \( -type d -o -name "*.jsonl" \) ! -name memory)
  [ -z "$old" ] && echo "  none" && exit 0
  echo "$old" | while read -r f; do printf '  %s  %s\n' "$(du -sh "$f" | cut -f1)" "$(basename "$f")"; done
  if [ "$yes" = 1 ]; then echo "$old" | while read -r f; do rm -rf "$f"; done; echo "deleted."; else echo "dry run: add --yes to delete."; fi
fi
