# Developer's guide #

## Installation instructions ##

Below summary refers to Eclipse. Use of NetBeans, etc. should be similar.
The latest verified version is shown within brackets.

  1. Download and install Eclipse `[Eclipse Helios]`.
    * You probably want to set the default encoding to UTF-8 in Eclipse for both "Eclipse -> Preferences -> General -> Workspace" and all text content types under "Eclipse -> Preferences -> General -> Content Types".
    * You probably want to show .resource files in Eclipse. This can be changed by pressing the small down-arrow in the Package Explorer, and then clicking "Filters...".
    * To run Eclipse smoothly, you probably want edit eclipse.ini to set the path to the VM and set decent memory allocation settings.
  1. Install recommended Eclipse plugins or corresponding required tools:
    * Java Development Tools, if not already in place `[3.6.1]`.
    * Subversive with SVNKit or similarly, which enables SVN handling from within the IDE `[0.7.9 and 2.2.2]`.
    * Maven Integration for Eclipse, m2eclipse `[0.10.2]`.
  1. Go to the SVN Repository Exploring perspective and add the repository. The location, your username and password can be found under the Source page on this wiki.
  1. In the Java perspective, add the project as an existing SVN project. Do this by browsing for `trunk/JPrIME`.
  1. If needed, activate Maven by right-clicking the project and clicking "Maven -> Enable Dependency Management" or possibly "Configure -> Convert to Maven Project" or similarly. You may need to refresh the project afterwards.
  1. The Java version has been set to 1.6 in Maven but, just to be sure, verify that the Eclipse project settings don't disagree.
  1. Just to be certain, right-click the project and select "Maven -> Download Javadoc" to make sure dependencies can be used more easily.
  1. Start coding! (...possibly by creating a new branch first!)

## Version control ##
As mentioned, the source code is under version control using Subversion (SVN). Please note that "dot directories", target directories, and similar should not be under version control, i.e. they should be added to `svn:ignore`.

## Builds ##
Builds are handled using the very competent (and alas complex) framework Maven. In particular, Maven takes care of all external dependencies (see also below). For creating releases, see [this wiki page](CreatingReleases.md).

## File structure ##
The source file structure adheres to the Maven standard directory layout (as a subset thereof). Before adding new root folders, consider using a folder name proposed by this standard. Of particular importance are:
  * `src/main/java` - where main Java source code goes.
  * `src/main/resources` - where peripheral files for an actual release go.
  * `src/main/python` - where additional Python source code goes.
  * `src/main/shellscript` - where additional shellscript source code goes.
  * `src/test/java` - where Java unit tests and test suites go.
  * `src/test/resources` - where peripheral files for unit tests go.
  * `target` - where local builds are kept (not under version control).
  * `lib` - where non-Mavenized dependencies are kept, see below.
Furthermore, non-Java resources kan be maintained in the same manner, e.g. `src/main/shellscript` and so forth.

## Dependencies ##
Maven handles all external dependencies (most commonly JAR files). Typically, for a user, true Mavenized dependencies (and sub-dependencies!) are retrieved and stored under the `~/.m2` folder.
When there is a need for a new dependency, do the following:
  1. Try searching for it using the interface shown for `pom.xml` in the source root. If it is found, just add it. Note that current builds (as opposed to proper releases) are often denoted SNAPSHOT.
  1. If it does not exist in an open Maven repository, one must use a workaround. Currently, this consists of adding the JAR file to the `lib` folder in the SVN repository and then adding a system scope mock reference to the JAR in `pom.xml` (typically without aid of the interface).
  1. Try to add non-Javadoc dependencies. If you need Javadoc or sources, these can be collected without adding them as real project dependencies, e.g. in Eclipse by right clicking the project and selecting "Maven <- Download Javadoc".

## Testing ##
Unit tests and test suites are handled with JUnit. Tests reside under the `src/test/java` folder in a package structure  aligned with that of the source code. Maven can be told to run test suites when building.

## Bug tracking ##
Bug tracking is handled using the Issues page here at Google Code. It is possible to set it up for use with Mylyn in Eclipse (see e.g. [this page](http://alblue.bandlem.com/2009/04/google-code-and-mylyn-redux.html)), but it doesn't really seem worth circumventing potential problems...

## Logging ##
Logging is not handled with the built-in logger, but with Logback (included using Maven), which is a successor of log4j.

## Application parameter parsing ##
Parameter parsing can be achieved in (almost) GNU style using JCommander (included using Maven).

## XML ##
At the moment, ordinary XML tasks should be carried out with Java's built-in JAXB package if possible.

## Bioinformatics ##
BioJava 3 (included using Maven) should be sufficient for common bioinformatics tasks involving alignments and similar.