#!/bin/sh

# run snyk sca on all folders
root=$(pwd)
for d in ./*/ ; do
  cd $d;
  echo "running snyk analysis on ${d}"
  snyk test --json --json-file-output=scan-results/snyk/snyk-report.json;
  cd $root;
done