#!/usr/bin/env bash



C_SUBSET_COMPILER=../csc/csc
THREE_ADDR_TO_C_TRANSLATOR=../hw3/compiler.jar

[ $# -ne 1 ] && { echo "Usage $0 PROGRAM" >&2; exit 1; }

# set -v

PROGRAM=$1
BASENAME=`basename $PROGRAM .c`
echo $PROGRAM
${C_SUBSET_COMPILER} $PROGRAM > ${BASENAME}.3addr
${THREE_ADDR_TO_C_TRANSLATOR} -backend=cfg < ${BASENAME}.3addr > ${BASENAME}.cfg
md5sum ${BASENAME}.cfg ${BASENAME}.ta.cfg
