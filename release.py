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

def updateModVersion(filename):
	pattern = re.compile("(String\s*VERSION\s*=\s*\")(\d.)(\d*)");

	for line in fileinput.input(filename, inplace=True):
		if re.search(pattern, line): 
			majorVersion = pattern.search(line).groups()[1]
			oldMinorVersion = pattern.search(line).groups()[2]
			newMinorVersion = str(int(oldMinorVersion) + 1);
			line = re.sub(pattern, "\\g<1>\\g<2>" + newMinorVersion, line);		
		print(line.rstrip('\n'))
	return majorVersion +newMinorVersion;
		
		
# Don't continue if working copy is dirty
if not safeProcess('git diff-index --quiet HEAD --'):
	print( "Cannot build, git working copy dirty")
	safeExit()
	
# Make sure the mod reports the new version.
newVersion = updateModVersion("com/specialeffect/eyegazemod/SpecialEffectEyeGaze.java");
print(newVersion)

# Go to forge code, and build
os.chdir(forgePath);
if not safeProcess("bash gradlew build"):
	print("Building mod failed, resetting code")
	os.chdir(origPath);	
	safeProcess("git checkout com/specialeffect/eyegazemod/SpecialEffectEyeGaze.java")
	safeExit(); 

# Commit changes
os.chdir(origPath);
safeProcess("git add com/specialeffect/eyegazemod/SpecialEffectEyeGaze.java")
safeProcess('git commit -m "Update version number to ' + newVersion + '"')
	
# Tag code
safeProcess("git tag release/" + newVersion)



