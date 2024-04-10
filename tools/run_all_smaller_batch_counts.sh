#!/bin/sh
for b in 4 3 2 1
do
	echo "Running with -bc $b in "`realpath batchcount$b`
	mkdir batchcount$b && cd batchcount$b && /usr/bin/time make -j 4 -k --output-sync=target -f ../Makefile EXTRA_FLAGS="-bc $b" > run_-bc_$b.out 2>&1
	cd ~/code/shadedetector
done
