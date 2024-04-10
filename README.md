# ShadeDetector -- A Tool to Detect Vulnerabilities in Cloned or Shaded Components

## Overview

The tool takes the coordinates of Maven artifact (**GAV** - **G**roupId + **A**rtifactId + **V**ersion) and a testable proof-of-vulnerability (POV) project as input,
and will infer and report a list of artifacts that are cloning / shading the input artifact, and are also exposed to the same vulnerability. For each such artifact,
a POV is constructed from the original POV, proving the presence of the vulnerability.

## Testable Proof-of-vulnerability Projects (POV)

### The Structure of a POV

POVs make a vulnerability testable. Each POV has the following structure:

1. a POV is a simple (i.e. non-modular) Maven project.
2. a POV has a dependency on the vulnerable artifact.
3. a POV has a test-scope dependency on JUnit5, other dependencies should be avoided or minimised.
4. a POV has one or more tests that either all succeed or all fail if and only if the vulnerability can be exploited -- i.e. the vulnerability becomes the test oracle. Those tests may be the only classes defined in a POV. The test outcome (success or failure) that indicates vulnerability is specified by the `testSignalWhenVulnerable` element of its `pov-project.json` metadata file.
5. a POV test may declare dependencies on certain OS or JRE versions using standard JUnit annotations such as `@EnabledOnOs` or `@EnabledOnJre`.
6. sources in a POV should not directly use fully classified class names, instead, imports should be used (this is to aid the tool to automatically refactor dependencies).

### Sourcing POVs

1. some POVs can be found here: https://github.com/jensdietrich/xshady/
2. there are numerous proof-of-vulnerability (POV) projects on GitHub, such as https://github.com/frohoff/ysoserial; usually those projects need to be modified to make them POVs as described above
3. this is a collection of POVs: https://github.com/tuhh-softsec/vul4j, see also *Bui QC, Scandariato R, Ferreyra NE. Vul4J: a dataset of reproducible Java vulnerabilities geared towards the study of program repair techniques. MSR'22.

## Building

The project must be built with Java 11 or better. To build run `mvn package`. This will create the executable `shadedetector.jar` in `/target`.

## Running

```
usage: java -cp <classpath> Main [-a <arg>] [-bc <arg>] [-bs <arg>] [-c <arg>] [-cache <arg>] [-env <arg>] [-fa <arg>] [-fc <arg>] [-fdm <arg>] [-g
       <arg>] [-l <arg>] [-mcc <arg>] [-msc <arg>] [-o <arg>] [-o1 <arg>] [-o2 <arg>] [-o3 <arg>] [-pl <arg>] [-ps <arg>] [-r <arg>] [-s <arg>] [-sig
       <arg>] [-v <arg>] [-vg <arg>] -vov <arg> -vul <arg> [-vv <arg>]
Arguments:

 -a,--artifact <arg>                      the Maven artifact id of the artifact queried for clones (default read from PoV's pov-project.json)
 -bc,--batchcount <arg>                   the number of by-class REST API search query batches per candidate (optional, default is 5)
 -bs,--batchsize <arg>                    the maximum number of rows requested in each by-class REST API search query batch (optional, default is 200)
 -c,--clonedetector <arg>                 the clone detector to be used (optional, default is "ast")
 -cache,--cachedir <arg>                  path to root of cache folder hierarchy (default is ".cache")
 -env,--testenvironment <arg>             a property file defining environment variables used when running tests on generated projects used to verify
                                          vulnerabilities, for instance, this can be used to set the Java version
 -fa,--filterartifacts <arg>              a regex restricting the artifact GAVs to be considered (non-matching GAVs will be discarded). For debugging.
 -fc,--filterclassnames <arg>             a regex restricting the class names to be considered (non-matching class names will be discarded). For
                                          debugging.
 -fdm,--finaldirmode <arg>                how to construct the contents of the final directory specified with -vov (optional, one of COPY, SYMLINK,
                                          OLD_UNSAFE_MOVE_AND_RETEST; default is COPY)
 -g,--group <arg>                         the Maven group id of the artifact queried for clones (default read from PoV's pov-project.json)
 -l,--log <arg>                           a log file name (optional, if missing logs will only be written to console)
 -mcc,--minclonedclasses <arg>            the minimum number of classes detected as clones needed to trigger compilation and testing (optional,
                                          default is 11)
 -msc,--maxsearchclasses <arg>            the maximum number of class names to search via the REST API per candidate (optional, default is 10)
 -o,--output <arg>                        the component used to process and report results (optional, default is "log")
 -o1,--output1 <arg>                      an additional component used to process and report results
 -o2,--output2 <arg>                      an additional component used to process and report results
 -o3,--output3 <arg>                      an additional component used to process and report results
 -pl,--povlabel <arg>                     the label for this PoV (output will go under a subdir having this name; default is the basename of the path
                                          specified with -vul)
 -ps,--stats <arg>                        the file to which progress stats will be written (default is "stats.log")
 -r,--resultconsolidation <arg>           the query result consolidation strategy to be used (optional, default is "moreThanOne")
 -s,--classselector <arg>                 the strategy used to select classes (optional, default is "complexnames")
 -sig,--vulnerabilitysignal <arg>         the test signal indicating that the vulnerability is present, must be of one of: SUCCESS,FAILURE,ERROR
                                          (default read from testSignalWhenVulnerable in PoV's pov-project.json)
 -v,--version <arg>                       the Maven version of the artifact queried for clones (default read from PoV's pom.xml)
 -vg,--vulnerabilitygroup <arg>           the group name used in the projects generated to verify the presence of a vulnerability (default is "foo")
 -vov,--vulnerabilityoutput_final <arg>   the root folder where for each clone, a project created in the build cache folder will be
                                          copied/symlinked/moved if verification succeeds (i.e. if the vulnerability is shown to be present)
 -vul,--vulnerabilitydemo <arg>           a folder containing a Maven project that verifies a vulnerability in the original library with test(s), and
                                          can be used as a template to verify the presence of the vulnerability in a clone; values for -g, -a, -v and
                                          -sig are read from any contained pov-project.json
 -vv,--vulnerabilityversion <arg>         the version used in the projects generated to verify the presence of a vulnerability (default is "0.0.1")
```

## Setting the Environment

With `-env` an environment can be set to be used to build / test the POVs. If POV tests require a Java version different from the one used to run the tool, this can be used to set `JAVA_HOME` to point to a particular version of the Java Development Kit (JDK, not just JRE as POVs are compiled).

## Known Issues

In principle the tool can be run with Java 11. However, we did encounter rare cases where the analysis gets stuck and eventually fails with an `OutOfMemoryError`. This seems to be caused by a [bug in the zip file system in Java 11](https://bugs.openjdk.org/browse/JDK-7143743). We recommend using Java 17 if this is a problem.

It is also possible to add artifacts to `anonymized.shadedetector.Blacklist` to exclude them from the analysis.

## Caching

To (dramatically) speed up subsequent runs, `shadedetector` caches:
1. Maven Central Repo REST API queries, by default in 5 batches of up to 200 results each
2. Candidate clone artifact `pom.xml`s and sources
3. Maven builds and test results of clone POVs

By default, all caching is done under the directory `.cache` in the current directory, but this can be changed with `-cache`.

NOTE: Multiple concurrent invocations of `shadedetector` will cooperate in using the cache safely -- **but only if the cache root directory is on a local filesystem.** NFS and possibly other network-based filesystems lack the guarantees of atomicity needed. A local filesystem will be much faster in any case.

## Final output directory

By default, for each vulnerable cloned artifact, `shadedetector` copies its cached build directory into `<finalDir>/<povLabel>/<safeName>`, where `<finalDir>` is the directory specified with `-vov`, `<povLabel>` is the label for the vulnerability (which defaults to the basename of the path given to `-vul` but can be changed with `-povlabel`) and `<safeName>` is a name constructed from the vulnerable GAV by replacing colons with `..`. For doing large batches of runs, specifying `--finaldirmode SYMLINK` will instead symlink to the cached build dirs, saving disk space.

## Customising / Extending

Several strategies are implemented as pluggable services. I.e. strategies are described via interfaces, with service providers declared in library manifests, see for instance [src/main/resources/META_INF/services](src/main/resources/META_INF/services) for the onboard default providers. Each provider has a unique name that can be used as an argument value in the CLI. All interfaces are defined in `nz.ac.wgtn.shadedetector`. The service is selected by a factory `nz.ac.wgtn.shadedetector.<Service>Factory` that also defines what is being used as the default service provider.

| Service     | Interface   | CLI Argument(s) | Description                                                                                                  | Default |
| ----------- | ----------- | -----------     |--------------------------------------------------------------------------------------------------------------|  ----------- |
| result reporter      | `ResultReporter`  | `-o`,`-o1`,`-o2`,`-o2` | consumes analysis results, e.g. to generate reports                                                          | report results using standard *log4j* logging |
| class selector       | `ClassSelector`  | `-s` | selects the classes from the input artifact to be used to query Maven for potential clones                   | pick 10 classes with the highest number of camel case tokens (i.e. complex class names) |
| clone detector       | `CloneDetector`  | `-c` | the clone detector used to compare two source code files (from the input artifact and a potential clone)     | custom AST-based clone detection that ignores comments and package names in type references |
| consolidation strategy | `ArtifactSearchResultConsolidationStrategy` | `-r` | the strategy used to consolidate artifact sets obtained by REST queries for a single class into a single set | an artifact must appear in at least two sets |

Some services can be customised further by setting properties (corresponding to bean properties in the respective service provider classes). For instance, consider the following arguments setting up output reporting:

```
  -o csv.details?dir=results/details/CVE-2022-45688-commonstext -o1 csv.summary?file=results/summary-CVE-2022-45688-commonstext.csv
```

This sets up two reporters named `csv.details` (corresponding to `resultreporting.anonymized.shadedetector.CSVDetailedResultReporter`) and `csv.summary` (corresponding to `resultreporting.anonymized.shadedetector.CSVSummaryResultReporter`), respectively. This is followed by a configuration consisting of &-separated key-value pairs, setting properties of the respective instance. In this case, the files / folders where reports are to be generated are set.

