

import subprocess, re, shutil

## Grep all the files for MODID

cmd="find . -iname '*.java' | xargs grep 'MODID = ' -h"
regex = "\"(.*)\""


result = subprocess.run(cmd.split(), stdout=subprocess.PIPE, shell=True)
lines = result.stdout.decode("utf-8").split("\r\n")

mod_ids = []
for line in lines:
    match = re.search(regex, line)
    if match:
        mod_id = match.group(1)
        mod_ids.append(mod_id)
        print(mod_id)


# Copy base file
fname_base = 'main/resources/META-INF/mods.toml.base'
fname_final = 'main/resources/META-INF/mods.toml'

shutil.copyfile(fname_base, fname_final)

author='Kirsty McNaught'
url = 'https://www.specialeffect.org.uk/eyemine'


# Append mod details

with open(fname_final, "a") as myfile:
    for mod_id in mod_ids:        
        myfile.write('[[mods]]\n')
        myfile.write('modId=\"{}\"\n'.format(mod_id))
        myfile.write('version=\"${file.jarVersion}\"\n')
        myfile.write('displayName=\"{}\"\n'.format(mod_id)) # TODO: nicer display names
        myfile.write('displayURL=\"{}\"\n'.format(url))
        myfile.write('authors=\"{}\"\n'.format(author))
        # myfile.write('\n')

        myfile.write('[[dependencies.{}]]\n'.format(mod_id))
        myfile.write('\tmodId="forge"\n')
        myfile.write('\tmandatory=true\n')    
        myfile.write('\tversionRange="[25,)"\n')    
        myfile.write('\tordering="NONE"\n')    
        myfile.write('\tside="BOTH"\n') # TODO: maybe client only??

        # Here's another dependency
        myfile.write('[[dependencies.{}]]\n'.format(mod_id))
        myfile.write('\tmodId="minecraft"\n')
        myfile.write('\tmandatory=true\n')
        myfile.write('\tversionRange="[1.14.4]"\n')
        myfile.write('\tordering="NONE"\n')
        myfile.write('\tside="BOTH"\n')

        myfile.write('\n')
            