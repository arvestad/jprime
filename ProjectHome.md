# What is JPrIME? #
JPrIME is a Java library with tools for phylogenetics. It is a complement to the the original C++ library [PrIME](http://prime.sbc.su.se/) developed by people in computational biology at [KTH](http://www.kth.se) and [Stockholm University](http://www.su.se), spearheaded by [Jens Lagergren](http://www.csc.kth.se/~jensl), [Lars Arvestad](http://www.csc.kth.se/~arve) and [Bengt Sennblad](http://www.sbc.su.se/~bens). Large parts of JPrIME can be attributed to [Joel Sjöstrand](http://www.csc.kth.se/~joelgs/). More recent contributors are [Mehmood Alam Khan](http://www.csc.kth.se/~malagori/), Owais Mahmudi, [Ikram Ullah](http://www.nada.kth.se/~ikramu/), Raja Hashim Ali, and Sayyed Auwn Muhammad.

PrIME and JPrIME have an emphasis on probabilistic phylogenetic models that typically employ a Markov-chain Monte Carlo (MCMC) framework.

![http://jprime.googlecode.com/files/mail-icon.png](http://jprime.googlecode.com/files/mail-icon.png)
As a user, you may want to consider signing up for the user community mailing list [here](https://mail.sbc.su.se/mailman/listinfo/primeusers).


---


---


# Releases #
![http://jprime.googlecode.com/files/download-icon.png](http://jprime.googlecode.com/files/download-icon.png)
Current and old releases of JPrIME can be obtained as single JAR files from
the [downloads page](http://code.google.com/p/jprime/downloads).
JPrIME requires Java SE 6. This may soon change to Java SE 7.


---


---


# Applications & tools available in JPrIME #
**Note: All applications mentioned below are distributed with and started with the JPrIME JAR file -- there are no individual binaries.**


### [DLRS & Delirious](DLRS.md) ###
> ![http://jprime.googlecode.com/files/DLRS_icon.png](http://jprime.googlecode.com/files/DLRS_icon.png)

DLRS is a species tree-aware phylogenetic model for gene family evolution that accounts for gene duplication, gene loss, and sequence evolution with a relaxed molecular clock. Read more in its [tutorial](http://code.google.com/p/jprime/wiki/DLRS).

### [DLTRS & Deleterious](DLTRS.md) ###
![http://jprime.googlecode.com/files/DLTRS_icon.png](http://jprime.googlecode.com/files/DLTRS_icon.png)

DLTRS is a species tree-aware phylogenetic model for gene family evolution that accounts for gene duplication, gene loss, lateral gene transfer, and sequence evolution with a relaxed molecular clock. Read more in its
[tutorial](http://code.google.com/p/jprime/wiki/DLTRS).

### [VMCMC](VMCMC.md) ###
![http://jprime.googlecode.com/files/VMCMC_icon2.png](http://jprime.googlecode.com/files/VMCMC_icon2.png)

VMCMC is a tool for analyzing output from Markov-chain Monte Carlo runs, in particular for determining an appropriate breakpoint for when the posterior distribution has been reached subsequent to the burn-in phase. Read more in its
[tutorial](http://code.google.com/p/jprime/wiki/VMCMC).


### [GenPhyloData](GenPhyloData.md) ###
![http://jprime.googlecode.com/files/GenPhyloData_icon.png](http://jprime.googlecode.com/files/GenPhyloData_icon.png)

GenPhyloData is a suite of tools for producing syntethic phylogenetic trees in accordance with DLRS, DLTRS and other models. Read more in its [tutorial](http://code.google.com/p/jprime/wiki/GenPhyloData).


---


---


# Licensing #
JPrIME is available under the New BSD License.


---


---


# External packages #
JPrIME makes use of a large number of fine open-source packages, among them [BioJava](http://www.biojava.org), [Forester](http://code.google.com/p/forester/), [JCommander](http://jcommander.org/), [Uncommons Maths](http://maths.uncommons.org/), [EJML](http://code.google.com/p/efficient-java-matrix-library/), [MDSJ](http://www.inf.uni-konstanz.de/algo/software/mdsj/), [Reflections](http://code.google.com/p/reflections/), [JScience](http://jscience.org/), and [JGraphT](http://jgrapht.org/).


---


---


# Becoming a contributing developer #
To get started as a contributing developer, you should:
  1. Apply for Google Code project membership.
  1. Read the [Developer's guide](http://code.google.com/p/jprime/wiki/DevelopersGuide) on the wiki, which will get you started.
  1. Preferably, you should also have a look at the [Wishlist](http://code.google.com/p/jprime/wiki/Wishlist) on the wiki before starting hacking away.