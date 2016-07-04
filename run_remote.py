#!/usr/bin/python

import hashlib
import random
import base64
import os
import sys
from subprocess import check_output, call, STDOUT
from time import time
SECRET_KEY = 'Mjg3ODAxNjM3NjI5MzIxOTkxOTc0NjIyNDQwMzUxNzE2MzM3OTU0NDE0NjE2NDkxNjk2Mjc4MTk2MTUyNjI4MTM3NTgwMjA3NjcwNzI3OTA1NDY5NzU1MTc1ODgxMTEyNzAyOA=='
FNULL = open(os.devnull, 'w')
ASM_PATH = "dist/tmp.s"

if len(sys.argv) < 2:
    print "Error: At least one argument is required (path to dcf file)"
else:
    DCF_PATH = sys.argv[1]
    opt = sys.argv[2] == '-opt' if len(sys.argv) == 3 else False
    key = "{0:x}".format(random.randint(0, 10**100)) + str(int(time()/3600))
    call(["rm", ASM_PATH], stderr=FNULL)
    if opt:
        call(["./run.sh", "--target=assembly", "--opt=all", DCF_PATH, "--outfile="+ASM_PATH], stdout=FNULL, stderr=STDOUT)
    else:
        call(["./run.sh", "--target=assembly", DCF_PATH, "--outfile="+ASM_PATH], stdout=FNULL, stderr=STDOUT)
    if not os.path.isfile(ASM_PATH):
        print "Error: run.sh failed to produce assembly file"
    else:
        with open(ASM_PATH, 'r') as f:
            value = hashlib.sha256(key+SECRET_KEY+hashlib.sha256(f.read()).hexdigest()).hexdigest()
        print check_output(["curl", "--form", "code=@"+ASM_PATH, "--form", "key="+key, "--form", "value="+value, "http://tsotnet.scripts.mit.edu/6035.py"], stderr=FNULL),
