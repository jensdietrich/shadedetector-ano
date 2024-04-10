#!/bin/bash -xe

grype db import vulnerability-db_v5_2023-04-27T10:34:58Z_90b83c0144433f50a35f.tar.gz
grype db status
