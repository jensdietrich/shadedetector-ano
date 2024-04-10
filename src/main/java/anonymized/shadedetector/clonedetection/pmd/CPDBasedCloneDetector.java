package anonymized.shadedetector.clonedetection.pmd;

import anonymized.shadedetector.CloneDetector;
import com.github.javaparser.JavaToken;
import com.github.javaparser.TokenRange;
import com.github.javaparser.ast.CompilationUnit;
import com.google.common.io.RecursiveDeleteOption;
import net.lingala.zip4j.ZipFile;
import net.sourceforge.pmd.cpd.*;
import com.github.javaparser.JavaParser;
import com.google.common.io.MoreFiles;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.io.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Implementation that uses PMD/CPD clone detection provided with PMD.
 * use overlap coefficient as measure of simi
 * @author shawn
 */
public class CPDBasedCloneDetector implements CloneDetector {
    static int  MIN_TOKENS = 10;;
    public static final File TMP = new File(".tmp");

    @Override
    public Set<CloneRecord> detect(Path original, Path cloneCandidate) {
        Set<CloneRecord> result = new HashSet<>();

        CPDConfiguration configuration = new CPDConfiguration();
        configuration.setMinimumTileSize(MIN_TOKENS);
        configuration.setLanguage(new JavaLanguage());
        CPD cpd = new CPD(configuration);

        try {

            if (TMP.exists()) {
                MoreFiles.deleteRecursively(TMP.toPath(), RecursiveDeleteOption.ALLOW_INSECURE);
            }

            TMP.mkdirs();
            ZipFile originalZip = new ZipFile(original.toFile());
            ZipFile candidateZip = new ZipFile(cloneCandidate.toFile());

            originalZip.extractAll(TMP.getAbsolutePath()+"/original");
            candidateZip.extractAll(TMP.getAbsolutePath()+"/candidate");
            cpd.addRecursively(TMP);

        } catch (Exception e) {
            e.printStackTrace();
        }
        cpd.go();

        Iterator<Match> matches = cpd.getMatches();
        Map<ImmutablePair<String, String>,Integer> matchMap = new HashMap<>();

        while (matches.hasNext()) {
            String originalFilename = null;
            String candidateFilename = null;
            Match match = matches.next();
            if (match.getFirstMark().getFilename().contains(TMP.getAbsolutePath() + "/original"))
                originalFilename = match.getFirstMark().getFilename();
            if (match.getSecondMark().getFilename().contains(TMP.getAbsolutePath() + "/original"))
                originalFilename = match.getSecondMark().getFilename();
            if (match.getFirstMark().getFilename().contains(TMP.getAbsolutePath() + "/candidate"))
                candidateFilename = match.getFirstMark().getFilename();
            if (match.getSecondMark().getFilename().contains(TMP.getAbsolutePath() + "/candidate"))
                candidateFilename = match.getSecondMark().getFilename();
            if (originalFilename == null || candidateFilename == null)
                continue;
            ImmutablePair<String, String> key = new ImmutablePair<>(originalFilename, candidateFilename);
            if (matchMap.containsKey(key)) {
                int tokens = matchMap.get(key);
                matchMap.put(key, tokens + match.getTokenCount());
            } else {
                matchMap.put(key, match.getTokenCount());
            }
        }

        JavaParser parser = new JavaParser();

        Set<ImmutablePair<String, String>> keys = matchMap.keySet();

        for(ImmutablePair<String, String> key: keys) {
            String originalFilename = key.getLeft();
            String candidateFilename = key.getRight();
            try {
                CompilationUnit node = parser.parse(new File(originalFilename)).getResult().orElse(null);;
                TokenRange javaTokens = node.getTokenRange().get();
                Iterator<JavaToken> iterator = javaTokens.iterator();
                int originalTokenCount = 0;
                while(iterator.hasNext()) {
                    iterator.next();
                    originalTokenCount++;
                }
                node = parser.parse(new File(originalFilename)).getResult().orElse(null);;
                javaTokens = node.getTokenRange().get();
                iterator = javaTokens.iterator();

                int candidateTokenCount = 0;
                while(iterator.hasNext()) {
                    iterator.next();
                    candidateTokenCount++;
                }
                double min = Math.min(originalTokenCount, candidateTokenCount);
                CloneRecord cr = new CloneRecord(matchMap.get(key)/min, Paths.get(originalFilename), Paths.get(candidateFilename));
                result.add(cr);
            } catch (Exception e) {
            }
        }

        return result;
    }

    @Override
    public String name() {
        return "CPD";
    }
}
