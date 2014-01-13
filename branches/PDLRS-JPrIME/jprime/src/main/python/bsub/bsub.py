#!/usr/bin/python

"""
********************************************************************************

Simple program for submitting batch jobs to PDC clusters. Usage, e.g.:

   ./bsub.py [options] <settingsfile> <batchfile> <batchid>

Input arguments:

   <settingsfile>   an XML file governing how to submit jobs, store output, etc.
   <batchfile>      a file where each line contains arguments for one job.
   <batchid>        an ID string enabling individual runs to be distinguished.

The program will create one or more shellscript files, which are each
submitted to the cluster. Each such file may span several parallel jobs
(processes).

Options:

   -h        displays help (this text).
   -e        displays an example of a settings file.
   -n        generates but does not execute the shellscript files nor the
             pre-command. Use this to inspect shellscript contents before
             submission.
   -c <CAC>  if using 'easy', overrides CAC argument specified in XML file
             with provided one.
   -t <time> if using 'easy', overrides requested node time argument in XML
             file with provided one.

--------------------------------------------------------------------------------

The actual command is specified in the settings file, where a string on the
form '%%n' is replaced with the n-th column from the batch file (indexed from
0). Similarly, one may use the following keywords:

   %%HOMEDIR       is replaced with the corresponding project path specified
                   in the settings file. A non-existing path is created.
   %%INDIR         similar to %%HOMEDIR, e.g. for data directory.
   %%OUTDIR        similar to %%HOMEDIR, e.g. for results directory.
   %%SETTINGSFILE  is replaced with the settings filename, path excluded.
   %%BATCHFILE     is replaced with the batch filename, path excluded.
   %%BATCHID       is replaced with the batch ID string.
   %%DATE          is replaced with the current date in UTF.
   %%SHELLSCRIPT   is replaced with the currently processed shellscript file,
                   path included.

!!!!!!!!    To get started, try running './bsub.py -e'    !!!!!!!!

--------------------------------------------------------------------------------

Example:

If "./bsub set.xml batchlist RUN11" is executed and 'set.xml' contains command
"superduperapp -o %%2.%%BATCHID.out %%0", and 'batchlist' contains:

   AAA BBB CCC
   aaa bbb ccc

...the resulting generated shellscript (named 'batchlist.RUN11.0.sh) will
contain:

   superduperapp -o CCC.RUN11.out AAA
   superduperapp -o ccc.RUN11.out aaa

Additionally, one has the possibility of including a "pre-command", executed
on the login node prior to submission, as well as a shellscript "preamble", which
may contain commands to be executed on the devoted node before starting batch commands.
The shellscript directory is always created automatically.

--------------------------------------------------------------------------------

Author: Joel Sjostrand, SBC/CSC, SU/KTH.

********************************************************************************
"""

import datetime
import getopt
import os
import re
import sys
from xml.dom import minidom, Node


###########################################################################
# Helper. Returns string s with occurrences of t replaced with u,
# unless any of s,t,u are None.
###########################################################################
def replaceSafe(s, t, u):
    if not s is None and not t is None and not u is None:
        return s.replace(t, u)
    return s


################################################################################
# Collects the various settings in a class.
################################################################################
class Settings:
    
    ###########################################################################
    # Constructor. Note: default values are set later.
    ###########################################################################
    def __init__(self, settingsFile, batchFile, batchID):
        self.DESCRIPTION = None
        self.HOME_DIR = None
        self.IN_DIR = None
        self.OUT_DIR = None
        self.PRE_CMD = None
        self.BATCH_FILE_DELIM = None
        self.BATCH_FILE_IGN_PREFIX = None
        self.CMD = None
#        self.ARG_PREFIX_ALL = None
#        self.ARG_PREFIX_INDIVID = dict()
        self.PROC_PER_NODE = None
        self.SHELLSCRIPT_OUT_DIR = None
        self.SHELLSCRIPT_PREAMBLE = None
        self.SHELLSCRIPT_CMD = None

        self.settingsFile = settingsFile.rsplit('/')[-1]
        self.batchFile = batchFile.rsplit('/')[-1]
        self.batchID = batchID
        self.utfdate = datetime.datetime.now().strftime("%Y-%m-%d")
    
    
    ###########################################################################
    # Ugly non-validating (where's the DTD anyway?) parsing of the XML file.
    ###########################################################################
    def parse(self,xmldoc):
        baseNode = xmldoc.firstChild
        if baseNode.nodeName != "Settings": raise Exception("Missing Settings node.")
        for n in baseNode.childNodes:
            if n.nodeName == "Description":
                self.DESCRIPTION = n.firstChild.nodeValue
            elif n.nodeName == "HomeDir":
                self.HOME_DIR = n.firstChild.nodeValue
            elif n.nodeName == "InDir":
                self.IN_DIR = n.firstChild.nodeValue
            elif n.nodeName == "OutDir":
                self.OUT_DIR = n.firstChild.nodeValue
            elif n.nodeName == "PreCmd":
                self.PRE_CMD = n.firstChild.nodeValue
            elif n.nodeName == "BatchFileDelim":
                self.BATCH_FILE_DELIM = n.firstChild.nodeValue
            elif n.nodeName == "BatchFileIgnorePrefix":
                self.BATCH_FILE_IGN_PREFIX = n.firstChild.nodeValue
            elif n.nodeName == "Cmd":
                self.CMD = n.firstChild.nodeValue
#            elif n.nodeName == "ArgPrefixes":
#                for c in n.childNodes:
#                    if c.nodeName == "Prefix":
#                        argtype = c.attributes["arg"].nodeValue
#                        if argtype == "all":
#                            self.ARG_PREFIX_ALL = c.firstChild.nodeValue
#                        else:
#                            argidx = int(argtype)
#                            self.ARG_PREFIX_INDIVID[argidx] = c.firstChild.nodeValue
            elif n.nodeName == "ProcessesPerNode":
                self.PROC_PER_NODE = n.firstChild.nodeValue
            elif n.nodeName == "ShellscriptOutDir":
                self.SHELLSCRIPT_OUT_DIR = n.firstChild.nodeValue
            elif n.nodeName == "ShellscriptPreamble":
                self.SHELLSCRIPT_PREAMBLE = n.firstChild.nodeValue
            elif n.nodeName == "ShellscriptCmd":
                self.SHELLSCRIPT_CMD = n.firstChild.nodeValue
    
    
    ###########################################################################
    # Validates and sets default values.
    ###########################################################################
    def validate(self):
        if self.DESCRIPTION == None:
            self.DESCRIPTION = ""    
        if self.HOME_DIR == None:
            self.HOME_DIR = ""
        if self.IN_DIR == None:
            self.IN_DIR = ""
        if self.OUT_DIR == None:
            self.OUT_DIR = ""
        if self.BATCH_FILE_DELIM == None:
            self.BATCH_FILE_DELIM = '\t'
        tmp = self.BATCH_FILE_DELIM.replace("\\t", '\t')
        self.BATCH_FILE_DELIM = tmp
        if self.BATCH_FILE_DELIM == "" or self.BATCH_FILE_DELIM.find("\\n") > -1:
            raise Exception("Invalid or missing batch file delimiter.")
        if self.BATCH_FILE_IGN_PREFIX == "":
            self.BATCH_FILE_IGN_PREFIX = None
        if self.CMD == None or self.CMD == "":
            raise Exception("Missing batch command.")
#        if self.ARG_PREFIX_ALL == None:
#            self.ARG_PREFIX_ALL = ""
        if self.PROC_PER_NODE == None or self.PROC_PER_NODE == "":
            self.PROC_PER_NODE = 7
        else:
            self.PROC_PER_NODE = max(int(self.PROC_PER_NODE),1)
        if self.SHELLSCRIPT_OUT_DIR == None or self.SHELLSCRIPT_OUT_DIR == "":
            raise Exception("Missing shellscript output directory.")
        elif not self.SHELLSCRIPT_OUT_DIR[-1] == '/':
            self.SHELLSCRIPT_OUT_DIR = self.SHELLSCRIPT_OUT_DIR + '/'
        if self.SHELLSCRIPT_CMD == None or self.SHELLSCRIPT_CMD == "":
            raise Exception("Missing shellscript command (i.e. submission command).")


#    ###########################################################################
#    # Callback helper. Prepends an argument with the "universal" prefix.
#    ###########################################################################
#    def prependAll(self,m):
#        arg = m.group(0)
#        return (self.ARG_PREFIX_ALL + arg)
    
    
#    ###########################################################################
#    # Callback helper. Prepends an argument with the corresp. individ. prefix.
#    ###########################################################################
#    def prependIndivid(self,m):
#        arg = m.group(0)
#        argidx = int(arg[2:])
#        if argidx in self.ARG_PREFIX_INDIVID:
#            return (self.ARG_PREFIX_INDIVID[argidx] + arg)
#        return arg
    
    
    ###########################################################################
    # Helper. Replaces the invariant %%-prefixed arguments in s.
    ###########################################################################    
    def replaceFixed(self, s):
        s = replaceSafe(s, "%%HOMEDIR", self.HOME_DIR)
        s = replaceSafe(s, "%%INDIR", self.IN_DIR)
        s = replaceSafe(s, "%%OUTDIR", self.OUT_DIR)
        s = replaceSafe(s, "%%SETTINGSFILE", self.settingsFile)
        s = replaceSafe(s, "%%BATCHFILE", self.batchFile)
        s = replaceSafe(s, "%%BATCHID", self.batchID)
        s = replaceSafe(s, "%%DATE", self.utfdate)
        return s
    
    
    ###########################################################################
    # Replaces invariant parameters, etc. in various strings.
    ###########################################################################
    def replaceParamsAndPrefixes(self):
 #       # Insert prefixes first.
 #       if self.ARG_PREFIX_ALL <> None and self.ARG_PREFIX_ALL <> "":
 #           self.CMD = re.sub("%%[0-9]+", self.prependAll, self.CMD)
 #       if self.ARG_PREFIX_INDIVID <> None and len(self.ARG_PREFIX_INDIVID) <> 0:
 #           self.CMD = re.sub("%%[0-9]+", self.prependIndivid, self.CMD)
 #       # Now carry on with substitutions.
        self.HOME_DIR = self.replaceFixed(self.HOME_DIR)
        self.IN_DIR = self.replaceFixed(self.IN_DIR)
        self.OUT_DIR = self.replaceFixed(self.OUT_DIR)
        self.PRE_CMD = self.replaceFixed(self.PRE_CMD)
        self.CMD = self.replaceFixed(self.CMD)
        self.SHELLSCRIPT_OUT_DIR = self.replaceFixed(self.SHELLSCRIPT_OUT_DIR)
        self.SHELLSCRIPT_PREAMBLE = self.replaceFixed(self.SHELLSCRIPT_PREAMBLE)
        self.SHELLSCRIPT_CMD = self.replaceFixed(self.SHELLSCRIPT_CMD)
        

###############################################################################
# Used for argument replacement when creating job commands.
###############################################################################
ReplList = ()
def subst(m):
    global ReplList
    arg = m.group(0)
    argidx = int(arg[2:])
    if argidx < 0 or argidx >= len(ReplList): raise Exception("Invalid parameter index.")
    return ReplList[argidx]
    


###############################################################################
# Generates and (if desired) executes the shellscripts.
###############################################################################
def genAndExecShellscripts(batchFile, sets, doExec):
    
    # Execute precommand.
    if sets.PRE_CMD <> None and sets.PRE_CMD <> "":
        if doExec:
            print ("Executing precommand...")
            print ("    " + sets.PRE_CMD)
            os.system(sets.PRE_CMD)
            print "...done."
        else:
            print ("Precommand (not executed): %s" % sets.PRE_CMD)
    
    # Read sequences from file. Remove empty lines and comments.
    f = open(batchFile, 'r')
    lns = f.readlines()
    f.close()
    while lns[-1] == "\n": del lns[-1]
    n = len(lns)
    if sets.BATCH_FILE_IGN_PREFIX <> None:
        i = 0
        while i < n:
            if lns[i].strip().startswith(sets.BATCH_FILE_IGN_PREFIX):
                del lns[i]
                i -= 1
                n -= 1
            i += 1
    
    # Create various directories, if not already there.
    if not os.path.exists(sets.SHELLSCRIPT_OUT_DIR):
        print("Creating shellscript directory %s" % sets.SHELLSCRIPT_OUT_DIR)
        os.makedirs(sets.SHELLSCRIPT_OUT_DIR)
    if doExec:
        if sets.HOME_DIR != None and sets.HOME_DIR != '' and not os.path.exists(sets.HOME_DIR):
            print("Creating home directory %s" % sets.HOME_DIR)
            os.makedirs(sets.HOME_DIR)
        if sets.IN_DIR != None and sets.IN_DIR != '' and not os.path.exists(sets.IN_DIR):
            print("Creating in directory %s" % sets.IN_DIR)
            os.makedirs(sets.IN_DIR)
        if sets.OUT_DIR != None and sets.OUT_DIR != '' and not os.path.exists(sets.OUT_DIR):
            print("Creating out directory %s" % sets.OUT_DIR)
            os.makedirs(sets.OUT_DIR)

    # Number of processes and number of nodes required.
    noOfNodes = n / sets.PROC_PER_NODE
    if n % sets.PROC_PER_NODE > 0: noOfNodes += 1
    
    # Special settings when using 'easy' submission.
    if sets.SHELLSCRIPT_CMD.find("esubmit") > -1:
        if re.search("-c *\S+", sets.SHELLSCRIPT_CMD) == None:
            print "Using user's default CAC.",
        else:
            cac = re.search('(?<=-c) *\S+', sets.SHELLSCRIPT_CMD)
            print ("Using CAC %s." % cac.group(0).strip()),
        if re.search("-m ", sets.SHELLSCRIPT_CMD) != None:
            print "Suppressing e-mail notifications.",
        tm = re.search('(?<=-t) *[0-9\.]+', sets.SHELLSCRIPT_CMD)
        print "Time limit is %s minutes." % tm.group(0).strip()
    
    # Generate the files.
    if doExec:
        print "Starting submission..."
    else:
        print "Starting to generate (but not execute) shellscript files..."
    i = 0
    for node in range(noOfNodes):
        shellfile = sets.SHELLSCRIPT_OUT_DIR + sets.batchFile + '.' + sets.batchID + '.' + str(node) + ".sh"
        f = open(shellfile, 'w')
        f.write("#! /bin/bash\n\n")
        if sets.SHELLSCRIPT_PREAMBLE <> None and sets.SHELLSCRIPT_PREAMBLE <> "":
            f.write(sets.SHELLSCRIPT_PREAMBLE + "\n\n")
        
        # Add jobs (processes) for this shellscript (node).
        for proc in range(sets.PROC_PER_NODE):
            if i >= n: break
            ln = lns[i].strip()
            global ReplList
            ReplList = ln.split(sets.BATCH_FILE_DELIM)
            s = re.sub("%%[0-9]+", subst, sets.CMD)
            s = replaceSafe(s, "%%SHELLSCRIPT", shellfile)
            f.write("(" + s + ") & \n")
            i += 1
        f.write("\nwait\n\n")
        f.close()
        os.system("chmod +x " + shellfile)

        # Submit!
        submitCmd = re.sub("%%[0-9]+", subst, sets.SHELLSCRIPT_CMD)    # Enables replacing e.g. %%3 with 4th argument of last processed line.
        submitCmd = submitCmd.replace("%%SHELLSCRIPT", shellfile)   
        if doExec:
            os.system(submitCmd)
            print ("    Submitted %s" % (shellfile))
        else:
            print ("    Generated but did not submit %s" % (shellfile))
    print ("...done. Processed %s jobs for %s nodes." % (n, noOfNodes))


################################################################################
# Main.
################################################################################
def main():

    # Read options.
    global doExec
    global settingsFile
    global batchFile
    global batchID
    doExec = True
    overrideCAC = None
    overrideTime = None
    try:
        opts, args = getopt.getopt(sys.argv[1:], "henc:t:", ["help", "show-settings-file", "no-execution", "cac=", "time="])
    except getopt.error, msg:
        print msg
        print "For help, use -h."
        sys.exit(2)
    for opt, val in opts:
        if opt in ("-h", "--help"):
            print __doc__
            sys.exit(0)
        if opt in ("-e", "--show-settings-file"):
            showSettingsFile()
            sys.exit(0)
        if opt in ("-n", "--no-execution"):
            doExec = False
        if opt in ("-c", "--cac"):
            overrideCAC = val
        if opt in ("-t", "--time"):
            overrideTime = val


    try:
        # Read arguments.
        if len(args) != 3:
            raise Exception("Wrong number of input arguments. For help, use -h.")
        settingsFile = args[0]
        batchFile = args[1]
        batchID = args[2]
        sets = Settings(settingsFile, batchFile, batchID)
        sets.parse(minidom.parse(settingsFile))
        sets.validate()
        sets.replaceParamsAndPrefixes()

        # Replace CAC and time if desired.
        if sets.SHELLSCRIPT_CMD.find("esubmit") > -1:
            if overrideCAC != None:
                sets.SHELLSCRIPT_CMD = re.sub("-c *\S+", "-c " + overrideCAC, sets.SHELLSCRIPT_CMD)
            if overrideTime != None:
                sets.SHELLSCRIPT_CMD = re.sub("-t *[0-9\.]+", "-t " + overrideTime, sets.SHELLSCRIPT_CMD)
        
        # Generate and process shellscripts.
        genAndExecShellscripts(batchFile, sets, doExec)
        
    except Exception, e:
        print "Error: ", e
        sys.exit(2)
    except:
        print "Unexpected error: ", sys.exc_info()[0]
        sys.exit(2)


################################################################################
# Displays and comments a sample XML settings file.
################################################################################
def showSettingsFile():
    print("""
Example of a settings file. Note: Tag names are case-sensitive.
Directories are created automatically prior to submission, so full paths
or making sure execution takes place in the right folder is recommended.

<Settings>
   <Description>Template bsub file.</Description>
   <HomeDir>~myname/mydir</HomeDir>
   <InDir>%%HOMEDIR/data</InDir>
   <OutDir>%%HOMEDIR/results/%%DATE</OutDir>
   <PreCmd>%%HOMEDIR/bin/mypreprocessor</PreCmd>
   <ShellscriptPreamble>
      module add easy;
      module add mpi;
      cd %%HOMEDIR;
   </ShellscriptPreamble>
   <BatchFileDelim>\\t</BatchFileDelim>
   <BatchFileIgnorePrefix>#</BatchFileIgnorePrefix>
   <Cmd>./bin/mymagicexe -abc -o %%OUTDIR/%%0.%%BATCHID.out %%0 %%1</Cmd>
   <ProcessesPerNode>7</ProcessesPerNode>
   <ShellscriptOutDir>%%HOMEDIR/tmp/%%DATE</ShellscriptOutDir>
   <ShellscriptCmd>esubmit -c mycac -t 1000 -m -n 1 %%SHELLSCRIPT</ShellscriptCmd>
</Settings>
    """)
    return


################################################################################
# Starter.
################################################################################
if __name__ == "__main__":
    main()

