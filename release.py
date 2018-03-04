#! python

from subprocess import call

import subprocess
import fileinput
import re
import os
import sys
import getopt

if len(sys.argv) < 2:
	print("usage: ./release <forge-source-dir>")
	print("e.g. ./release ../forge-1.8.8-11.15.0.1608-1.8.8-mdk")
	sys.exit(1)

origPath = os.getcwd();
forgePath = sys.argv[1];

def safeProcess( cmd ):
	"Run command, return boolean for success"
	print(cmd);
	try:
		out = subprocess.check_output(cmd, shell=True)
		print(out.decode("utf-8").replace("\\\n", "\n"))
		return True;
	except subprocess.CalledProcessError as e:                                                                                                   
		print("Status : FAIL", e.returncode, e.output)
		return False;
		
def safeExit():
	print ("Exiting...")
	os.chdir(origPath)
	sys.exit(1)
#	safeProcess("git reset --hard head")

def get_version(filename):
    pattern = re.compile("String\s*VERSION\s*=\s*\"(\d*.\d*.\d*)\"");
    for line in fileinput.input(filename):
        if re.search(pattern, line): 
            print(pattern.search(line).groups())
            version = pattern.search(line).groups()[0]
            return version
    return None;        

# Don't continue if working copy is dirty
if not safeProcess('git diff-index --quiet HEAD --'):
	print( "Cannot build, git working copy dirty")
	safeExit()
	
# Go to forge code, and build
os.chdir(forgePath);
if not safeProcess("bash gradlew build"):
	print("Building mod failed, won't continue")
	os.chdir(origPath);	
	safeExit(); 
    
# Tag code by version
os.chdir(origPath);	
version_file = "java/com/specialeffect/utils/ModUtils.java"
version = get_version(version_file)
safeProcess("git tag release/{}".format(version))



