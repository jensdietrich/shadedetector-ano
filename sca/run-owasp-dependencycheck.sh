#!/bin/sh

# run owasp dependencycheck sca on all folders
root=$(pwd)
for d in ./*/ ; do
  cd $d;
  echo "running owasp dependency check analysis on ${d}"
  dependency-check -scan . -f JSON -o scan-results/dependency-check -prettyPrint;
  cd $root;
done