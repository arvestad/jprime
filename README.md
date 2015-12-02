

# About JPrIME

Thank you for using JPrIME!

JPrIME is a Java library primarily aimed at phylogenetics,
although other functionality may be added over time as well.

The package is developed and maintained by the computational biology
groups at [Science for Life Laboratory Stockholm](http://www.scilifelab.se/).
It has its roots in a C++ library named PrIME, primarily developed by PIs
Jens Lagergren, Lars Arvestad, and Bengt Sennblad. Frequent contributors to
JPrIME includes Joel Sjöstrand, Mehmood Alam Khan, Raja Hashim Ali, Ikram Ullah, Owais Mahmudhi,
and Auwn Muhammed.

# Installation

The easiest way to install JPrime is to download the latest [JAR file found in
our Dropbox directory](https://www.dropbox.com/sh/4yfyav5wmeyk34a/AAAhayS-dwx0OBeJl5RpuOYha?dl=0).
This is a single JAR file bundled with all external
dependencies. It will most likely be named 'jprime-X.Y.Z.jar', with
X.Y.Z referring to its current incarnation.

You may place the JAR file anywhere on your computer. However, for frequent use,
you may want to add its location to your CLASSPATH or similarly, as outlined
[on Wikipedia](http://en.wikipedia.org/wiki/Classpath_(Java)).

Alternatively, the location of the JAR file can be specified explicitly when you
start an application such as in this fictitious example:

```
java -cp ~/mypath/jprime-X.Y.Z.jar se/cbb/jprime/apps/MyApp
```

# Documentation

For instructions on how to run applications, tutorials, source code, etc., please
visit JPrIME's home at GitHub.com, https://github.com/arvestad/jprime.


# Releases and source code

JPrIME is currently hosted at GitHub: https://github.com/arvestad/jprime.

# Frequently Asked Questions

+ How can I sample [realisations](http://www.biomedcentral.com/1471-2105/14/S15/S10) using JPrIME-DLRS?

Yes, it is possible to sample realisations using JPrIME-DLRS. Please see the help file for providing arguments by using the following command:
java -jar jprime.jar Delirious -h

A typical example of running JPrIME while sampling realisations would look like this:
java -jar target/jprime-0.0.1-SNAPSHOT.jar Delirious -i 1000 -t 10 -sm WAG -o samples.mcmc sample_data/dlrs_example_1.stree.txt sample_data/dlrs_example_1.fa.txt sample_data/dlrs_example_1.map.txt
