#! python

from subprocess import call

import subprocess
import fileinput
import re
import os
import sys
import getopt

if len(sys.argv) < 3:
    print("usage: ./update_version.py <forge-source-dir> --[major/minor/revision]")
    print("e.g:   ./update_version.py ../forge-1.8.8-11.15.0.1608-1.8.8-mdk --revision")
    sys.exit(1)

origPath = os.getcwd();
forge_dir = sys.argv[1]
version_level = sys.argv[2];
valid_version_levels = ['--major', '--minor', '--revision']
if version_level not in valid_version_levels:
    print('Version level "{}" not recognised, must be one of:'.format(version_level))
    print(valid_version_levels)
    sys.exit(1)

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
#    safeProcess("git reset --hard head")

def updateModVersion(filename, version_level):
    pattern = re.compile("(String\s*VERSION\s*=\s*\")(\d*).(\d*).(\d*)\"");

    new_version = None
    for line in fileinput.input(filename, inplace=True):
        if re.search(pattern, line): 
            major = int(pattern.search(line).groups()[1])
            minor = int(pattern.search(line).groups()[2])
            revision = int(pattern.search(line).groups()[3])
            
            if version_level == "--major":
                major += 1
                minor = 0
                revision = 0
            elif version_level == "--minor":
                minor += 1
                revision = 0
            elif version_level == "--revision":
                revision += 1
            new_version =  "{}.{}.{}".format(major, minor, revision)
            line = re.sub(pattern, "String VERSION  = \"{}\"".format(new_version), line);        
        print(line.rstrip('\n'))
    if not new_version:
        raise Exception("Could not find version in file {}".format(filename))
    return new_version;        
  
def updateVersionGradle(filename, new_version):
    # This updates the version in build.gradle. This file is 
    # not within our repo, so beware!
    
    #e.g. version = "1.0.3"
    pattern = re.compile('version\s*=\s*"(\d*.\d*.\d*)"');

    for line in fileinput.input(filename, inplace=True):
        if re.search(pattern, line): 
            curr_version = pattern.search(line).groups()[0]
            line = line.replace(curr_version, new_version)    
        print(line.rstrip('\n'))
    return new_version
    
    
# Don't continue if working copy is dirty
#if not safeProcess('git diff-index --quiet HEAD --'):
#    print( "Cannot continue, git working copy dirty")
#    safeExit()
    
# Make sure the mod reports the new version.
version_file = "java/com/specialeffect/utils/ModUtils.java"
new_version = updateModVersion(version_file, version_level);
print(new_version)

# Make sure gradle knows about the version
gradle_file = '{}/build.gradle'.format(forge_dir)
updateVersionGradle(gradle_file, new_version)

# Commit changes
safeProcess("git add {}".format(version_file))
safeProcess('git commit -m "Update version number to ' + new_version + '"')
    

