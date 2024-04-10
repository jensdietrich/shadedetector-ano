#!/bin/sh

# run steady sca on all folders
root=$(pwd)
for d in ./*/ ; do
  cd $d;
  echo "running steady  on ${d}"
  mvn org.eclipse.steady:plugin-maven:3.2.5:app 
  mvn org.eclipse.steady:plugin-maven:3.2.5:report -Dvulas.report.reportDir=$(pwd)/scan-results/steady
  cd $root;
done
