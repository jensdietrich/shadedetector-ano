package anonymized.shadedetector;

import anonymized.shadedetector.cveverification.*;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.common.io.MoreFiles;
import anonymized.shadedetector.clonedetection.ImportTranslationExtractor;
import nz.ac.wgtn.shadedetector.cveverification.*;
import anonymized.shadedetector.pov.PovProject;
import anonymized.shadedetector.pov.PovProjectParser;
import anonymized.shadedetector.resultreporting.CombinedResultReporter;
import anonymized.shadedetector.resultreporting.ProgressReporter;
import org.apache.commons.cli.*;
import org.jdom2.JDOMException;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessResult;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.*;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * CLI main class.
 * @author jens dietrich
 */
public class Main {


    private static Logger LOGGER = LoggerFactory.getLogger(Main.class);
    public static final String TEST_LOG = ".mvn-test.log";

    private static ClassSelectorFactory CLASS_SELECTOR_FACTORY = new ClassSelectorFactory();
    private static CloneDetectorFactory CLONE_DETECTOR_FACTORY = new CloneDetectorFactory();
    private static ArtifactSearchResultConsolidationStrategyFactory CONSOLIDATION_STRATEGY_FACTORY = new ArtifactSearchResultConsolidationStrategyFactory();
    private static ResultReporterFactory RESULT_REPORTER_FACTORY = new ResultReporterFactory();

    private static final String DEFAULT_GENERATED_VERIFICATION_PROJECT_GROUP_NAME = "foo";
    private static final String DEFAULT_GENERATED_VERIFICATION_PROJECT_VERSION = "0.0.1";

    private static final String DEFAULT_PROGRESS_STATS_NAME = "stats.log";
    private static final int DEFAULT_MAX_SEARCH_CLASSES = 10;
    private static final int DEFAULT_MIN_CLONED_CLASSES = 11;   // Was originally "> 10", comparison is now ">="
    private static final String CACHE_BUILD_NAME = "build";

    enum FinalDirMode { COPY, SYMLINK, OLD_UNSAFE_MOVE_AND_RETEST };
    private static final FinalDirMode DEFAULT_FINAL_DIR_MODE = FinalDirMode.COPY;

    // resources will be copied into verification projects instantiated for clones
    private static final String[] SCA_SCRIPTS = {
            "/run-owasp-dependencycheck.sh",
            "/run-snyk.sh"
    };

    public static void main (String[] args) throws ParseException {
        Options options = new Options();
        options.addOption("g", "group",true, "the Maven group id of the artifact queried for clones (default read from PoV's pov-project.json)");
        options.addOption("a", "artifact",true, "the Maven artifact id of the artifact queried for clones (default read from PoV's pov-project.json)");
        // @TODO - in the future, we could generalise this to look for version ranges , allow wildcards etc
        options.addOption("v", "version",true, "the Maven version of the artifact queried for clones (default read from PoV's pom.xml)");

        // we need a little language here to pass parameters, such as list:class1,class2
        // needs default
        options.addOption("s", "classselector",true, "the strategy used to select classes (optional, default is \"" + CLASS_SELECTOR_FACTORY.getDefault().name() + "\")");
        options.addOption("o", "output",true, "the component used to process and report results (optional, default is \"" + RESULT_REPORTER_FACTORY.getDefault().name() + "\")");
        options.addOption("o1", "output1",true, "an additional component used to process and report results");
        options.addOption("o2", "output2",true, "an additional component used to process and report results");
        options.addOption("o3", "output3",true, "an additional component used to process and report results");
        options.addOption("c","clonedetector",true,"the clone detector to be used (optional, default is \"" + CLONE_DETECTOR_FACTORY.getDefault().name() + "\")");
        options.addOption("r","resultconsolidation",true,"the query result consolidation strategy to be used (optional, default is \"" + CONSOLIDATION_STRATEGY_FACTORY.getDefault().name() + "\")");

        options.addRequiredOption("vul","vulnerabilitydemo",true,"a folder containing a Maven project that verifies a vulnerability in the original library with test(s), and can be used as a template to verify the presence of the vulnerability in a clone; values for -g, -a, -v and -sig are read from any contained pov-project.json");
        options.addRequiredOption("vov","vulnerabilityoutput_final",true,"the root folder where for each clone, a project created in the build cache folder will be copied/symlinked/moved if verification succeeds (i.e. if the vulnerability is shown to be present)");
        options.addOption("vg","vulnerabilitygroup",true,"the group name used in the projects generated to verify the presence of a vulnerability (default is \"" + DEFAULT_GENERATED_VERIFICATION_PROJECT_GROUP_NAME + "\")");
        options.addOption("vv","vulnerabilityversion",true,"the version used in the projects generated to verify the presence of a vulnerability (default is \"" + DEFAULT_GENERATED_VERIFICATION_PROJECT_VERSION + "\")");

        options.addOption("env","testenvironment",true,"a property file defining environment variables used when running tests on generated projects used to verify vulnerabilities, for instance, this can be used to set the Java version");
        options.addOption("ps","stats",true,"the file to which progress stats will be written (default is \"" + DEFAULT_PROGRESS_STATS_NAME + "\")");
        options.addOption("l","log",true,"a log file name (optional, if missing logs will only be written to console)");
        options.addOption("cache", "cachedir", true, "path to root of cache folder hierarchy (default is \"" + Cache.getRoot() +"\")");

        options.addOption("sig","vulnerabilitysignal",true,"the test signal indicating that the vulnerability is present, must be of one of: " + Stream.of(TestSignal.values()).map(v -> v.name()).collect(Collectors.joining(",")) + " (default read from testSignalWhenVulnerable in PoV's pov-project.json)");

        options.addOption("fc", "filterclassnames", true, "a regex restricting the class names to be considered (non-matching class names will be discarded). For debugging.");
        options.addOption("fa", "filterartifacts", true, "a regex restricting the artifact GAVs to be considered (non-matching GAVs will be discarded). For debugging.");

        options.addOption("pl", "povlabel", true, "the label for this PoV (output will go under a subdir having this name; default is the basename of the path specified with -vul)");

        options.addOption("msc", "maxsearchclasses", true, "the maximum number of class names to search via the REST API per candidate (optional, default is " + DEFAULT_MAX_SEARCH_CLASSES + ")");
        options.addOption("bc", "batchcount", true, "the number of by-class REST API search query batches per candidate (optional, default is " + ArtifactSearch.BATCHES + ")");
        options.addOption("bs", "batchsize", true, "the maximum number of rows requested in each by-class REST API search query batch (optional, default is " + ArtifactSearch.ROWS_PER_BATCH + ")");
        options.addOption("mcc", "minclonedclasses", true, "the minimum number of classes detected as clones needed to trigger compilation and testing (optional, default is " + DEFAULT_MIN_CLONED_CLASSES + ")");
        options.addOption("fdm", "finaldirmode", true, "how to construct the contents of the final directory specified with -vov (optional, one of " + Stream.of(FinalDirMode.values()).map(v -> v.name()).collect(Collectors.joining(", ")) + "; default is " + DEFAULT_FINAL_DIR_MODE + ")");

        CommandLineParser parser = new DefaultParser();

        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        }
        catch (MissingOptionException x) {
            LOGGER.error(x.getMessage(),x);
            printHelp(options);
            System.exit(1);
        }

        if (cmd.hasOption("log")) {
            String logFile = cmd.getOptionValue("log");
            LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
            PatternLayoutEncoder ple = new PatternLayoutEncoder();
            ple.setPattern("%date %level [%thread] %logger{10} [%file:%line] %msg%n");
            ple.setContext(lc);
            ple.start();
            FileAppender<ILoggingEvent> fileAppender = new FileAppender<>();
            fileAppender.setFile(logFile);
            fileAppender.setEncoder(ple);
            fileAppender.setContext(lc);
            fileAppender.start();
            Logger rootLogger = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
            ((ch.qos.logback.classic.Logger)rootLogger).addAppender(fileAppender);
            LOGGER.info("file log appender set up, log file is: " + new File(logFile).getAbsolutePath());
        }

        if (cmd.hasOption("cachedir")) {
            String cacheDir = cmd.getOptionValue("cachedir");
            Cache.setRoot(new File(cacheDir));
            LOGGER.info("set cache root dir to {}", cacheDir);
        }

        // see whether vulnerability verification is available
        Path verificationProjectTemplateFolder = null;
        if (cmd.hasOption("vulnerabilitydemo")) {
            verificationProjectTemplateFolder = Path.of(cmd.getOptionValue("vulnerabilitydemo"));
            try {
                checkVerificationProject(verificationProjectTemplateFolder);
                LOGGER.info("vulnerability verification project is valid");
            }
            catch (Exception x) {
                LOGGER.error("vulnerability verification project is not valid");
            }
        }

        String groupId = null;
        String artifactId = null;
        String version = null;
        TestSignal expectedTestSignal = null;
        String povLabel = cmd.getOptionValue("povlabel", verificationProjectTemplateFolder.getFileName().toString());
        LOGGER.info("PoV label is '{}'", povLabel);
        // Get defaults from PoV metadata

        Properties defaultEnvironFromJdkVersion = new Properties();
        File povMetadataFile = verificationProjectTemplateFolder.resolve("pov-project.json").toFile();
        if (povMetadataFile.exists()) {
            try {
                LOGGER.info("Reading PoV metadata from {}", povMetadataFile.getAbsolutePath());
                PovProject povMetaData = PovProjectParser.parse(povMetadataFile);
                expectedTestSignal = povMetaData.getTestSignalWhenVulnerable();
                String[] tokens = povMetaData.getArtifact().split(":");
                String groupIdFromMetadata = tokens[0];
                String artifactIdFromMetadata = tokens[1];

                // pov-project.json stores the complete set of vulnerableVersions according to the external DB entry, but NOT
                // the specific version the PoV project repros the vuln on (i.e., depends on). That needs to be extracted from its pom.xml.
                // Here we assume it's the only dependency with matching groupId and artifactId mentioned in pom.xml.
                try {
                    List<MVNDependency> possibleArtifactsUnderTest = POMAnalysis.getMatchingDependencies(verificationProjectTemplateFolder.resolve("pom.xml").toFile(), dep -> dep.getGroupId().equals(groupIdFromMetadata) && dep.getArtifactId().equals(artifactIdFromMetadata));
                    String versionFromMetadata = null;
                    if (possibleArtifactsUnderTest.size() != 1) {
                        LOGGER.error("Found {} dependency artifacts in PoV matching {}:{}, was expecting 1", possibleArtifactsUnderTest.size(), groupIdFromMetadata, artifactIdFromMetadata);
                        // Fall through. version will remain null unless specified by user with -v
                    }
                    else {
                        versionFromMetadata = possibleArtifactsUnderTest.get(0).getVersion();
                    }
                    groupId = groupIdFromMetadata;
                    artifactId = artifactIdFromMetadata;
                    version = versionFromMetadata;
                    LOGGER.info("Read {}:{}:{}, testSignalWhenVulnerable={} from PoV metadata (version came from pom.xml)", groupIdFromMetadata, artifactIdFromMetadata, versionFromMetadata, expectedTestSignal);
                }
                catch (Exception e) {
                    LOGGER.error("Exception while reading pom.xml to extract version", e);
                    System.exit(1);
                }

                @Nullable String jdkVersion = povMetaData.getJdkVersion();
                if (jdkVersion != null) {
                    String javaHome = Utils.getJavaHomeForJdkVersion(jdkVersion);
                    LOGGER.info("Setting JAVA_HOME={} based on jdkVersion={} from pov-project.json", javaHome, jdkVersion);
                    defaultEnvironFromJdkVersion.setProperty("JAVA_HOME", javaHome);
                }
            } catch (FileNotFoundException e) {
                LOGGER.error("Error instantiating test signal from pov meta data");
            }
        }
        // Command-line arguments to -g, -a, -v, -sig override values read from pov-project.json
        groupId = cmd.getOptionValue("group", groupId);
        if (groupId == null) {
            LOGGER.error("Group ID could not be read from pov-project.json metadata, so must be specified with -g or --group");
            System.exit(1);
        }
        artifactId = cmd.getOptionValue("artifact", artifactId);
        if (artifactId == null) {
            LOGGER.error("Artifact ID could not be read from pov-project.json metadata, so must be specified with -a or --artifact");
            System.exit(1);
        }
        version = cmd.getOptionValue("version", version);
        if (version == null) {
            LOGGER.error("Version could not be read from pov-project.json metadata, so must be specified with -v or --version");
            System.exit(1);
        }
        GAV gav = new GAV(groupId,artifactId,version);
        LOGGER.info("PoV template GAV: {}", gav.asString());

        CloneDetector cloneDetector = instantiateOptional(CLONE_DETECTOR_FACTORY,cmd,"clone detector","clonedetector");
        ClassSelector classSelector = instantiateOptional(CLASS_SELECTOR_FACTORY,cmd,"class selector","classselector");
        ArtifactSearchResultConsolidationStrategy resultConsolidationStrategy = instantiateOptional(CONSOLIDATION_STRATEGY_FACTORY,cmd,"result consolidation strategy","resultconsolidation");
        ResultReporter firstResultReporter = instantiateOptional(RESULT_REPORTER_FACTORY,cmd,"result reporter","output");

        List<ResultReporter> resultReporters = new ArrayList<>();
        resultReporters.add(firstResultReporter);
        if (cmd.hasOption("output1")) {
            resultReporters.add(instantiateOptional(RESULT_REPORTER_FACTORY,cmd,"result reporter","output1"));
        }
        if (cmd.hasOption("output2")) {
            resultReporters.add(instantiateOptional(RESULT_REPORTER_FACTORY,cmd,"result reporter","output2"));
        }
        if (cmd.hasOption("output3")) {
            resultReporters.add(instantiateOptional(RESULT_REPORTER_FACTORY,cmd,"result reporter","output3"));
        }

        Properties testEnviron = new Properties(defaultEnvironFromJdkVersion);
        if (cmd.hasOption("testenvironment")) {
            String testEnvironDef = cmd.getOptionValue("testenvironment");
            Path testEnvironFile = Path.of(testEnvironDef);
            Preconditions.checkArgument(Files.exists(testEnvironFile),"test environment file not found: " + testEnvironFile);
            try (Reader reader = Files.newBufferedReader(testEnvironFile)) {
                testEnviron.load(reader);
                LOGGER.error("test environment loaded from {}",testEnvironFile);
            } catch (IOException e) {
                LOGGER.error("cannot load test environment from {}",testEnvironFile,e);
                throw new RuntimeException(e);
            }
        }

        FinalDirMode finalDirMode = FinalDirMode.valueOf(cmd.getOptionValue("finaldirmode", DEFAULT_FINAL_DIR_MODE.name()).toUpperCase());
        LOGGER.info("Final dir processing mode: {}", finalDirMode);

        ResultReporter resultReporter = resultReporters.size()==1 ?
            resultReporters.get(0) :
            new CombinedResultReporter(resultReporters);

        File progressStats = new File(DEFAULT_PROGRESS_STATS_NAME);
        if (cmd.hasOption("stats")) {
            progressStats = new File(cmd.getOptionValue("stats"));
        }
        LOGGER.error("progress stats will be written to {}",progressStats.getAbsolutePath());
        ProgressReporter progressReporter = new ProgressReporter(progressStats); // TODO make configurable


        // find artifact
        List<Artifact> allVersions = null;
        Artifact artifact = null;
        try {
            // note: fetching artifacts for all versions could be postponed
            ArtifactSearchResponse response = ArtifactSearch.findVersions(groupId,artifactId);
            allVersions = response.getBody().getArtifacts();
            final String finalVersion = version;    // To make the compiler happy compiling a lambda
            artifact = allVersions.stream()
                .filter(a -> a.getVersion().equals(finalVersion))
                .findFirst().orElse(null);

        } catch (ArtifactSearchException e) {
            LOGGER.error("cannot fetch artifacts for "+groupId+":"+artifactId,e);
        }
        if (allVersions==null || allVersions.size()==0 || artifact==null) {
            LOGGER.error("cannot locate artifacts for {}:{}",groupId,artifactId);
            System.exit(1);
        }

        // find sources
        Path originalSources = null;
        try {
            originalSources = Utils.extractFromZipToTempDir(FetchResources.fetchSources(artifact));
        } catch (IOException e) {
            LOGGER.error("cannot fetch sources for " + gav.asString(),e);
        }

        if (originalSources==null) {
            LOGGER.error("no sources available for sources for {}",artifact.getId());
            System.exit(1);
        }

        // The main reason to specify -filterclassnames or -filterartifacts is to save time while debugging.
        Optional<String> filterClassNamesOption = Optional.ofNullable(cmd.getOptionValue("filterclassnames"));
        LOGGER.info("Restrict search to class names matching: {}", filterClassNamesOption.map(s -> "/" + s + "/").orElse("(any)"));
        Predicate<String> classNamePredicate = filterClassNamesOption
                .map((Function<String, Predicate<String>>) regex -> (className -> className.matches(regex)))
                .orElse(className -> true); // Default to keeping all class names

        Optional<String> filterArtifactsOption = Optional.ofNullable(cmd.getOptionValue("filterartifacts"));
        LOGGER.info("Restrict cloned artifacts to GAVs matching: {}", filterArtifactsOption.map(s -> "/" + s + "/").orElse("(any)"));
        Predicate<String> gavPredicate = filterArtifactsOption
                .map((Function<String, Predicate<String>>) regex -> (gavAsString -> gavAsString.matches(regex)))
                .orElse(gavAsString -> true); // Default to keeping all matching artifact GAVs

        int maxClassesUsedForSearch = Optional.ofNullable(cmd.getOptionValue("maxsearchclasses")).map(Integer::parseInt).orElse(DEFAULT_MAX_SEARCH_CLASSES);
        LOGGER.info("Maximum number of class names to search for: {}", maxClassesUsedForSearch);
        int classSearchBatchCount = Optional.ofNullable(cmd.getOptionValue("batchcount")).map(Integer::parseInt).orElse(ArtifactSearch.BATCHES);
        LOGGER.info("By-class REST API search batch count: {}", classSearchBatchCount);
        int classSearchMaxResultsInEachBatch = Optional.ofNullable(cmd.getOptionValue("batchsize")).map(Integer::parseInt).orElse(ArtifactSearch.ROWS_PER_BATCH);
        LOGGER.info("By-class REST API search batch size: {}", classSearchMaxResultsInEachBatch);
        int minClonedClasses = Optional.ofNullable(cmd.getOptionValue("minclonedclasses")).map(Integer::parseInt).orElse(DEFAULT_MIN_CLONED_CLASSES);
        LOGGER.info("Minimum number of classes detected as clones needed to trigger compilation and testing: {}", minClonedClasses);

        // find all potentially matching artifacts
        Map<String,ArtifactSearchResponse> matches = null;
        try {
            matches = ArtifactSearch.findShadingArtifacts(originalSources, classSelector, classNamePredicate, gavPredicate, maxClassesUsedForSearch, classSearchBatchCount, classSearchMaxResultsInEachBatch);
        }
        catch (Exception e) {
            LOGGER.error("cannot fetch artifacts with matching classes from {}",gav,e);
        }

        Set<Artifact> allMatches = matches.values().stream().flatMap(response -> response.getBody().getArtifacts().stream()).collect(Collectors.toSet());
        progressReporter.artifactsProcessed(ProcessingStage.QUERY_RESULTS,allMatches);

        // consolidate results
        LOGGER.info("{} potential matches found",allMatches.size());
        List<Artifact> consolidatedMatches = resultConsolidationStrategy.consolidate(matches);
        LOGGER.info("matched consolidated to {}",consolidatedMatches.size());

        progressReporter.artifactsProcessed(ProcessingStage.CONSOLIDATED_QUERY_RESULTS,consolidatedMatches);

        // eliminate matches with dependency -- those are likely to be detected by existing checkers
        List<Artifact> candidates = new ArrayList<>();
        List<Artifact> candidatesWithValidPom = new ArrayList<>();

        for (Artifact match:consolidatedMatches) {
            try {
                if (!POMAnalysis.references(match, artifact.getGroupId(), artifact.getArtifactId())) {
                    candidates.add(match);
                }

                candidatesWithValidPom.add(match);
            } catch (Exception e) {
                LOGGER.info("Error fetching or analysing pom for {}",match.getId(),e);
            }
        }
        LOGGER.info("{} potential matches have declared dependency on {}:{}, will be excluded from further analysis", candidatesWithValidPom.size() - candidates.size(), artifact.getGroupId(), artifact.getArtifactId());
        LOGGER.info("{} potential matches detected without declared dependency on {}:{}, will be analysed for clones", candidates.size(), artifact.getGroupId(), artifact.getArtifactId());
        LOGGER.info("dependency analysis failed for {} artifacts", consolidatedMatches.size() - candidatesWithValidPom.size());
        progressReporter.artifactsProcessed(ProcessingStage. VALID_POM, candidatesWithValidPom);
        progressReporter.artifactsProcessed(ProcessingStage. NO_DEPENDENCY_TO_VULNERABLE, candidates);

        // run clone detection

        try {
            resultReporter.startReporting(artifact,originalSources);
        }
        catch (IOException x) {
            LOGGER.error("error initialising result reporting",x);
        }

        // set up signal (may already have been read from pov-projects.json)
        String vulnerabilitySignalAsString = cmd.getOptionValue("vulnerabilitysignal");
        if (vulnerabilitySignalAsString != null) {
            vulnerabilitySignalAsString = vulnerabilitySignalAsString.toUpperCase();
            if (!vulnerabilitySignalAsString.equals("AUTO")) { // "auto" is now the default; ignore here for backcompat
                expectedTestSignal = TestSignal.valueOf(vulnerabilitySignalAsString); // will throw illegal argument exception if no such constant exists
            }
        }

        if (expectedTestSignal == null) {
            LOGGER.error("could not determine testSignalWhenVulnerable from the pov-project.json metadata, and --vulnerabilitysignal was not specified");
            System.exit(1);
        }

        LOGGER.info("test signal is {}",expectedTestSignal);
        assert expectedTestSignal != null;

        Path verificationProjectInstancesFolderFinal = null;
        if (cmd.hasOption("vulnerabilityoutput_final")) {
            verificationProjectInstancesFolderFinal = Path.of(cmd.getOptionValue("vulnerabilityoutput_final"));
            if (!Files.exists(verificationProjectInstancesFolderFinal)) {
                try {
                    Files.createDirectories(verificationProjectInstancesFolderFinal);
                } catch (IOException e) {
                    throw new RuntimeException("cannot create folder " + verificationProjectInstancesFolderFinal,e);
                }
            }
        }

        Path buildCacheFolder = null;
        try {
            buildCacheFolder = Cache.getCache(CACHE_BUILD_NAME).toPath().resolve(getEnvPathComponent(testEnviron)).resolve(povLabel).toAbsolutePath();
        } catch (Exception x) {
            throw new RuntimeException("Could not hash environment contents", x);
        }
        LOGGER.info("verified projects will be symlinked from {} to cached built projects under {}", verificationProjectInstancesFolderFinal, buildCacheFolder);
        assert verificationProjectInstancesFolderFinal!=null;


        String verificationProjectGroupName = DEFAULT_GENERATED_VERIFICATION_PROJECT_GROUP_NAME;
        if (cmd.hasOption("vulnerabilitygroup")) {
            verificationProjectGroupName = cmd.getOptionValue("vulnerabilitygroup");
        }
        LOGGER.info("verification projects will be use group name {}",verificationProjectGroupName);

        String verificationProjectVersion= DEFAULT_GENERATED_VERIFICATION_PROJECT_VERSION;
        if (cmd.hasOption("vulnerabilityversion")) {
            verificationProjectVersion = cmd.getOptionValue("vulnerabilityversion");
        }
        LOGGER.info("verification projects will be use version {}",verificationProjectVersion);


        // sets mainly used to produce stats later
        Set<Artifact> sourcesFetched = new HashSet<>();
        Set<Artifact> sourcesExpanded = new HashSet<>();
        Set<Artifact> sourcesHaveJavaFiles = new HashSet<>();
        Set<Artifact> cloneDetected = new HashSet<>();
        Set<Artifact> compiledSuccessfully = new HashSet<>();
        Set<Artifact> surefireReportsGenerated = new HashSet<>();
        Set<Artifact> vulnerabilityConfirmed = new HashSet<>();
        Set<Artifact> shaded = new HashSet<>();

        for (Artifact match:candidates) {
            LOGGER.info("analysing whether artifact {} matches",match.getId());
            ResultReporter.VerificationState state = ResultReporter.VerificationState.NONE;
            Set<CloneDetector.CloneRecord> cloneAnalysesResults = Set.of();
            List<Path> sources = List.of();
            boolean packagesHaveChangedInClone = false; // indicates shading with packages being renamed

            if (Blacklist.contains(match)) {
                LOGGER.warn("Skipping blacklisted artifact: " + match.asGAV().asString());
            }
            else {
                try {
                    Path srcJar = FetchResources.fetchSources(match);
                    sourcesFetched.add(match);
                    Path src = Utils.extractFromZipToTempDir(srcJar);
                    sourcesExpanded.add(match);
                    sources = Utils.listJavaSources(src, true);
                    if (sources.isEmpty()) {
                        LOGGER.error("Source for {} contains no .java files", match.getId());
                    } else {
                        sourcesHaveJavaFiles.add(match);
                        cloneAnalysesResults = cloneDetector.detect(originalSources, src);

                        LOGGER.info("Reporting results for " + match.getId());

                        if (cloneAnalysesResults.size() >= minClonedClasses) {
                            cloneDetected.add(match);
                            LOGGER.info("generating project to verifify vulnerability for " + match);
                            String verificationProjectArtifactName = match.toString().replace(":", "__");
                            LOGGER.info("\tgroupId: " + verificationProjectGroupName);
                            LOGGER.info("\tartifactId: " + verificationProjectArtifactName);
                            LOGGER.info("\tversion: " + verificationProjectVersion);
                            Path verificationProjectFolderStaged = buildCacheFolder.resolve(verificationProjectArtifactName);
                            LOGGER.info("\tproject folder: " + verificationProjectFolderStaged);

                            Map importTranslations = ImportTranslationExtractor.computeImportTranslations(originalSources, src, cloneAnalysesResults);

                            MoreFiles.createParentDirectories(verificationProjectFolderStaged);
                            MVNProjectCloner.CloneResult result = MVNProjectCloner.cloneMvnProject(
                                    verificationProjectTemplateFolder,
                                    verificationProjectFolderStaged,
                                    gav,
                                    match.asGAV(),
                                    new GAV(verificationProjectGroupName, verificationProjectArtifactName, verificationProjectVersion),
                                    importTranslations,
                                    testEnviron
                            );
                            packagesHaveChangedInClone = result.isRenamedImports();
                            if (packagesHaveChangedInClone) {
                                shaded.add(match);
                            }

                            if (result.isCompiled()) {
                                state = ResultReporter.VerificationState.COMPILED;
                                compiledSuccessfully.add(match);
                            }
                            if (result.isTested()) { // override
                                state = ResultReporter.VerificationState.TESTED;
                            }

                            if (result.isTested()) {
                                boolean vulnerabilityIsPresent = isVulnerabilityPresent(expectedTestSignal, verificationProjectFolderStaged, () -> surefireReportsGenerated.add(match));
                                if (vulnerabilityIsPresent) {
                                    vulnerabilityConfirmed.add(match);
                                    Path verificationProjectFolderFinal = verificationProjectInstancesFolderFinal.resolve(povLabel).resolve(verificationProjectArtifactName);
                                    switch (finalDirMode) {
                                        case COPY: {
                                            // Fairly light and fast, and creates full directories ready to be added to a GitHub repo
                                            LOGGER.info("\tVuln verified! Copying cached project build from {} to {}", verificationProjectFolderStaged, verificationProjectFolderFinal);
                                            try {
                                                MVNProjectCloner.copyMvnProject(verificationProjectFolderStaged, verificationProjectFolderFinal);
                                            } catch (IOException x) {
                                                LOGGER.error("Could not copy verified project from {} to final dir {}", verificationProjectFolderStaged, verificationProjectFolderFinal, x);
                                            }
                                            break;
                                        }

                                        case SYMLINK: {
                                            // Light and fast -- good for large batches of runs
                                            LOGGER.info("\tVuln verified! Creating a symlink at {} to the cached result at {}", verificationProjectFolderFinal, verificationProjectFolderStaged);
                                            try {
                                                MoreFiles.createParentDirectories(verificationProjectFolderFinal);
                                                Files.createSymbolicLink(verificationProjectFolderFinal, verificationProjectFolderStaged);
                                            } catch (IOException x) {
                                                LOGGER.error("Could not create final symlink from {} to {}", verificationProjectFolderFinal, verificationProjectFolderStaged, x);
                                            }
                                            break;
                                        }

                                        case OLD_UNSAFE_MOVE_AND_RETEST: {
                                            // The original behaviour. Unsafe since other processes may be concurrently reading from the cached build.
                                            // Also it deletes the build cache entry, so future runs will have to build it again.
                                            LOGGER.info("\tmoving verified project folder from {} to {}", verificationProjectFolderStaged, verificationProjectFolderFinal);
                                            MVNProjectCloner.moveMvnProject(verificationProjectFolderStaged, verificationProjectFolderFinal);

                                            // re-test to create surefire reports
                                            LOGGER.error("running build test on final project {}", verificationProjectFolderFinal);
                                            Path buildLog = verificationProjectFolderFinal.resolve(TEST_LOG);
                                            try {
                                                ProcessResult pr = MVNExe.mvnTest(verificationProjectFolderFinal, testEnviron);
                                                String out = pr.outputUTF8();
                                                Files.write(buildLog, List.of(out));

                                                boolean vulnerabilityIsPresentInFinal = isVulnerabilityPresent(expectedTestSignal, verificationProjectFolderFinal, () -> surefireReportsGenerated.add(match));
                                                if (!vulnerabilityIsPresentInFinal) {
                                                    LOGGER.error("error testing final project {} -- vulnerability was present in staging but not in final", verificationProjectFolderFinal);
                                                }
                                            } catch (Exception x) {
                                                LOGGER.error("error testing final project {}", verificationProjectFolderFinal, x);
                                                String stacktrace = Utils.printStacktrace(x);
                                                Files.write(buildLog, List.of(stacktrace));
                                            }
                                            break;
                                        }
                                    }
                                }

                            }
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("cannot fetch sources for artifact {}", match.toString(), e);
                } finally {
                    try {
                        resultReporter.report(artifact, match, sources, cloneAnalysesResults, state, packagesHaveChangedInClone);
                    } catch (IOException e) {
                        LOGGER.error("error reporting", e);
                    }
                }
            }
        }

        progressReporter.artifactsProcessed(ProcessingStage.SOURCES_FETCHED, sourcesFetched);
        progressReporter.artifactsProcessed(ProcessingStage.SOURCES_EXPANDED, sourcesExpanded);
        progressReporter.artifactsProcessed(ProcessingStage.SOURCES_HAVE_JAVA_FILES, sourcesHaveJavaFiles);
        progressReporter.artifactsProcessed(ProcessingStage.CLONE_DETECTED,cloneDetected);
        progressReporter.artifactsProcessed(ProcessingStage.POV_INSTANCE_COMPILED,compiledSuccessfully);
        progressReporter.artifactsProcessed(ProcessingStage.POV_INSTANCE_SUREFIRE_REPORTS_GENERATED, surefireReportsGenerated);
        progressReporter.artifactsProcessed(ProcessingStage.POV_INSTANCE_VULNERABILITY_CONFIRMED,vulnerabilityConfirmed);
        progressReporter.artifactsProcessed(ProcessingStage.POV_INSTANCE_VULNERABILITY_CONFIRMED_SHADED, Sets.intersection(vulnerabilityConfirmed,shaded));

        try {
            progressReporter.endReporting();
            LOGGER.info("finished progress reporting, results written to {}",progressReporter.getOutput().getAbsolutePath());
        }
        catch (IOException x) {
            LOGGER.error("error finishing progress reporting",x);
        }

        try {
            resultReporter.endReporting(artifact);
        }
        catch (IOException x) {
            LOGGER.error("error finishing result reporting",x);
        }
    }

    private static boolean isVulnerabilityPresent(TestSignal expectedTestSignal, Path verificationProjectFolder, Runnable testResultsParsedCallback) throws IOException, JDOMException {
        Path surefireReports = verificationProjectFolder.resolve("target/surefire-reports");

        if (Files.exists(surefireReports)) {
            SurefireUtils.TestResults testResults = SurefireUtils.parseSurefireReports(surefireReports);
            testResultsParsedCallback.run();
            boolean vulnerabilityIsPresent = testResults.assertExpectedOutcome(expectedTestSignal);
            LOGGER.info("tests in {}: {} passed, {} failed, {} errors, {} skipped -> vuln is {}sent",
                    verificationProjectFolder,
                    testResults.getTestCount() - (testResults.getFailureCount() + testResults.getErrorCount() + testResults.getSkippedCount()),
                    testResults.getFailureCount(),
                    testResults.getErrorCount(),
                    testResults.getSkippedCount(),
                    vulnerabilityIsPresent ? "pre" : "ab"
            );
            return vulnerabilityIsPresent;
        }

        LOGGER.warn("no surefire reports found in {}, will assume that tests have not passed", verificationProjectFolder);
        return false;
    }


    // check the vulnerability verification project (template)
    private static void checkVerificationProject(Path verificationProjectTemplateFolder) throws Exception {
        Preconditions.checkArgument(Files.exists(verificationProjectTemplateFolder),"project folder missing: " + verificationProjectTemplateFolder);
        Preconditions.checkArgument(Files.isDirectory(verificationProjectTemplateFolder),"folder expected here: " + verificationProjectTemplateFolder);
        Path pom = verificationProjectTemplateFolder.resolve("pom.xml");
        Preconditions.checkArgument(Files.exists(pom),"not a Maven project (pom.xml missing): " + verificationProjectTemplateFolder);

        try {
            POMUtils.parsePOM(pom);
        }
        catch (Exception x) {
            throw new RuntimeException("Not a valid pom: " + pom,x);
        }

        ProcessResult buildResult = null;
        try {
            buildResult = MVNExe.mvnCleanCompile(verificationProjectTemplateFolder);
        }
        catch (Exception x) {
            throw new RuntimeException("Project cannot be build: " + verificationProjectTemplateFolder,x);
        }
        if (buildResult.getExitValue()!=0) {
            List<String> buildLog = MVNExe.extractOutput(buildResult);
            LOGGER.warn("build output starts here -----------");
            for (String line:buildLog) {
                LOGGER.warn(line);
            }
            LOGGER.warn("build output ends here -----------");
            throw new RuntimeException("Project cannot be build, check logs for output: " + verificationProjectTemplateFolder);
        }

        // TODO could verify that tests fail here !

    }

    private static <T extends NamedService> T instantiateOptional(AbstractServiceLoaderFactory<T> factory, CommandLine cmd, String description, String key) {
        T service = cmd.hasOption(key)
            ? factory.create(cmd.getOptionValue(key))
            : factory.getDefault();
        assert service!=null;
        LOGGER.info("using {}: {}",description,service.name());
        return service;
    }

    private static String getEnvPathComponent(Properties properties) throws IOException, NoSuchAlgorithmException {
        return "env-" + Utils.md5HashProperties(properties);
    }

    private static void printHelp(Options options) {
        String header = "Arguments:\n\n";
        String footer = "\nPlease report issues at https://github.com/jensdietrich/shading-study/issues/";
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(150);
        formatter.printHelp("java -cp <classpath> Main", header, options, footer, true);
    }
}
