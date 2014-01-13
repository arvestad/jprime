#! /usr/bin/env python

import sys

usg = """
================================================================================
Simple script which given a *sorted* univariate list of floats extracts the
quantiles. Useful on big files. No interpolation, i.e. closest sample is chosen.

Usage: ./GetQuantiles <infile> [quantiles]

where the optional argument is a comma-separated list of quantiles, e.g.
0,0.25,0.5,0.75,1

Author: Joel Sjostrand, SciLifeLab, SU/KTH.
================================================================================
"""
if len(sys.argv) == 1:
    print usg
    sys.exit(0)

# Quantiles to find.
if len(sys.argv) > 2:
    qs = []
    for q in sys.argv[2].split(','):
        qs.append(float(q))
else:
    qs = (0.00, 0.01, 0.05, 0.10, 0.25, 0.50, 0.75, 0.90, 0.95, 0.99, 1.00)
k = len(qs)

# Count the number of lines.
f = open(sys.argv[1], 'r')
cnt = 0
for ln in f:
    cnt += 1
for i in range(1,k):
    if (qs[i] - qs[i-1]) * cnt < 1:
        f.close()
        raise "Cannot extract values, since too dense quantiles for this dataset."

# Reset file.
f.seek(0)
qidx = 0
qcnt = round(qs[qidx] * (cnt - 1))

# Obtain values.
print "QuantProb\tQuantVal"
for i in range(cnt):
    ln = f.readline().strip()
    if i >= qcnt:
        print "%s\t%s" % (qs[qidx], ln)
        qidx = min(qidx + 1, k - 1)
        qcnt = round(qs[qidx] * (cnt - 1))
        
f.close()
