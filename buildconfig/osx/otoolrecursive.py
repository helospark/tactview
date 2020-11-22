# From: https://stackoverflow.com/a/1517652
import subprocess
import sys
import os.path

def otool(s):
    o = subprocess.Popen(['/usr/bin/otool', '-L', s], stdout=subprocess.PIPE)
    for l in o.stdout:
        if l[0] == '\t':
            path = l.split(' ', 1)[0][1:]
            if "@rpath" in path:
                #print "Replace rpath ", s, " ", path
                currentPath=os.path.dirname(s)
                path = path.replace("@rpath", currentPath)
            yield path

need = set([sys.argv[1]])
done = set()

while need:
    needed = set(need)
    need = set()
    for f in needed:
        need.update(otool(f))
    done.update(needed)
    need.difference_update(done)

for f in sorted(done):
    print f
