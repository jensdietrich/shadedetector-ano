#!/bin/sh
mkdir results && cd results && /usr/bin/time make -j 4 -k --output-sync=target -f ../Makefile > run.out 2>&1
