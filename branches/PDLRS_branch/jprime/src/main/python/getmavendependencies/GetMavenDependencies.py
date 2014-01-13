# People may be running this on different platforms, so no #!/usr/local/bin/python...

import sys
from xml.dom import minidom

# Read all lines from root Maven pom.xml.
xmldoc = minidom.parse(sys.path[0] + '/../../../../pom.xml')
deps = xmldoc.getElementsByTagName('dependency')
print 'Group ID\tArtifact ID\tVersion\tCLassifier\tScope'
print '____________________________________________'
for dep in deps:
    groupid = dep.getElementsByTagName('groupId')
    if groupid != None and len(groupid) > 0:
        groupid = groupid[0].firstChild.nodeValue
    else:
        groupid = ''
    artifactid = dep.getElementsByTagName('artifactId')
    if artifactid != None and len(artifactid) > 0:
        artifactid = artifactid[0].firstChild.nodeValue
    else:
        artifactid = ''
    version = dep.getElementsByTagName('version')
    if version != None and len(version) > 0:
        version = version[0].firstChild.nodeValue
    else:
        version = ''
    type = dep.getElementsByTagName('type')
    if type != None and len(type) > 0:
        type = type[0].firstChild.nodeValue
    else:
        type = ''
    classifier = dep.getElementsByTagName('classifier')
    if classifier != None and len(classifier) > 0:
        classifier = classifier[0].firstChild.nodeValue
    else:
        classifier = ''
    scope = dep.getElementsByTagName('scope')
    if scope != None and len(scope) > 0:
        scope = scope[0].firstChild.nodeValue
    else:
        scope = ''
    print groupid + '\t' + artifactid + '\t' + version + '\t' + type + '\t' + classifier + '\t' + scope