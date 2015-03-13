**VMCMC has not yet been released, and the information below is not guaranteed to be up-to-date. However, [this site](https://code.google.com/p/visualmcmc/) for VMCMC is guaranteed to be up to date. Feedback is much appreciated, should you want to try VMCMC out. We hope to be able offer a stable first release in the near future.**

> ![http://i58.tinypic.com/2up71o7.jpg](http://i58.tinypic.com/2up71o7.jpg)

# Introduction #

VMCMC is an application for analysing output of MCMC chains. It deals with various metrics for assessing MCMC convergence, as well as summary statistics of inferred real-valued parameters, tree topologies, etc. VMCMC can be used with output from the C++ version of PrIME, and general tab-delimited files such as those produced by JPrIME some applications (e.g., [Delirious](DLRS.md) and [Deleterious](DLTRS.md)).

VMCMC supports two modes for analysing an MCMC chain:
  1. Graphical user interface (GUI) for showing trace plots, etc.
  1. Command-line summary of important statistics.

**Download up-to-date vmcmc jar file from [this site](https://drive.google.com/file/d/0Bxs0shuzgCXscDFvbjB6bFlqNzg/edit?usp=sharing).**

# Running #

You start the GUI version of VMCMC by running e.g.:
```
java -jar VMCMC-x.y.z.jar
```
where the details of course refer to your current setup. You can always show all available options by typing e.g.:
```
java -jar VMCMC-x.y.z.jar -h
```
If you run out of memory when using Oracle's HotSpot Java VM, you can allocate more memory by specifying e.g. (these are not options of VMCMC):
```
java -Xms768m -Xmx1536m -jar VMCMC-x.y.z.jar
```
To initialize the GUI directly for a specific file, type e.g.:
```
java -jar VMCMC-x.y.z.jar myfile.mcmc
```
To suppress the GUI and only output summary statistics of the chain in the terminal, type e.g.:
```
java -jar VMCMC-x.y.z.jar myfile.mcmc -n
```

# Details #

Please refer to [VMCMC](https://code.google.com/p/visualmcmc/wiki/VMCMC) for a detailed tutorial and parameter settings.