#!/usr/bin/python

from __future__ import print_function
import sys

if len(sys.argv) != 2:
    print("Hayk did you forget to pass the extra output from parser? (Or was it you Akaki?! :| )")
    exit(1)
x = sys.argv[1]
y = "\n"
while x != "":
    if x[0] == '(':
        y += ' |  '
        print(y,end='')
    elif x[0] == ')':
        y = y[:-4]
    else:
        print(x[0],end='')
    x = x[1:]
print('\n')
