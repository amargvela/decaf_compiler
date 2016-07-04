#!/bin/sh

runsemantics() {
  $(git rev-parse --show-toplevel)/run.sh -t inter $1
}

fail=0

a="`dirname $0`"
for file in $a/illegal/*; do
  if runsemantics "$file" > /dev/null 2>&1; then
    echo "Illegal file $file semantic checked successfully.";
    fail=1
  fi
done

for file in $a/legal/*; do
  if ! runsemantics "$file"; then
    echo "Legal file $file failed to pass semantic checks.";
    fail=1
  fi
done

exit $fail;
