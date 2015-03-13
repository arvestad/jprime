# Wishlist #

Below is a list of features that should / has already been added with noteworthy remarks.

| **Feature** |  **Comment** | **Priority** |
|:------------|:-------------|:-------------|
| ~~Unit testing framework~~ | Now using JUnit. | In place |
| ~~GCC option parsing~~ | Now using JCommander. Not quite pure GNU style though. | In place  |
| ~~Logging~~ | Logback now used instead of native logger. | In place |
| ~~Build system~~  | Now using Maven, See Maven JAR plugin for packaging. | In place |
| ~~Bug-tracking~~ | Now using issue tracking within Google Code. | In place |
| XML input and output | JAXB for standard stuff. External library required for condensed XML? | In place (at least partly) |
| ~~Numeric methods package.~~ | EJML now being used. | Medium |
| Multicore parallelization | There is good fork-join capabilities in Java 7. | Medium |
| Computer node parallelization  | Maybe use Hadoop instead of MPI? If MPI, use of some Java port, which one? | Medium |
| Program manuals auto-generated from Javadoc |  | Low |
| Profiling tool |  Not of major importance for now. | Low |
| Support for TeX in Javadoc |  | Low |