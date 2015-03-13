# How to create releases of JPrIME #

Naturally, we use Maven for building and packaging. The pom.xml has been set to JAR packaging, and also to use the Maven Shade plugin. This means that when packaging, two kinds of output should be generated in the target directory:
  * An original (small) JPrIME JAR file.
  * A "shaded" (or "über", if you wish) JPrIME JAR file, which contains the small JPrIME JAR file bundled with all dependencies.

## Steps ##
  1. Create the shaded JAR package. E.g., in Eclipse: right-click the project root folder and select "Run as -> Maven clean", then "Run as -> Maven package" or possibly "Run as -> Maven install". Alternatively, from the command prompt: `mvn clean` then `mvn package`.
  1. Rename the shaded snapshot JAR file to jprime-x.y.z.jar with a suitable x.y.z.
  1. Add it to Downloads on Google Code along with the current SVN revision. This can be seen e.g. from the SVN repository view in Eclipse.

On a side note: a tool for listing all (non-transitive) dependencies from the root pom.xml can be found in `jprime/src/main/python/getmavendependencies/GetMavenDependencies.py`