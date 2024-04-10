package anonymized.shadedetector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Utilities related to the POM. In particular, this is used to establish whether
 * dependencies exist.
 * @author jens dietrich
 */
public class POMAnalysis {

    private static Logger LOGGER = LoggerFactory.getLogger(POMAnalysis.class);

    public static boolean hasDependency (Artifact artifact, GAV dependency) throws Exception {
        Predicate<MVNDependency> condition = d ->
            d.getGroupId().equals(dependency.getGroupId())
            && d.getArtifactId().equals(dependency.getArtifactId())
            && d.getVersion().equals(dependency.getVersion());
        return hasDependency(artifact,condition);
    }

    // this can be used to look for a dependency of any version
    public static boolean hasDependency (Artifact artifact, String groupId, String artifactId) throws Exception {
        Predicate<MVNDependency> condition = d -> d.getGroupId().equals(groupId) && d.getArtifactId().equals(artifactId);
        return hasDependency(artifact,condition);
    }

    public static boolean hasDependency (Path pom, String groupId, String artifactId) throws Exception {
        Predicate<MVNDependency> condition = d -> d.getGroupId().equals(groupId) && d.getArtifactId().equals(artifactId);
        return hasDependency(pom,condition);
    }


    static boolean hasDependency (Artifact artifact, Predicate<MVNDependency> condition) throws Exception {
        Path pom = FetchResources.fetchPOM(artifact);
        return hasDependency(pom,condition);
    }

    static boolean hasDependency (Path pom, Predicate<MVNDependency> condition) throws Exception {
        List<MVNDependency> dependencies = getDependencies(pom.toFile());
        return dependencies.stream().anyMatch(condition);
    }

    public static List<MVNDependency> getMatchingDependencies(File pom, Predicate<MVNDependency> condition) throws Exception {
        return getDependencies(pom).stream().filter(condition).collect(Collectors.toList());
    }

    public static boolean hasGroupAndArtifactId(Artifact artifact,String groupId, String artifactId) throws Exception {
        Path pom = FetchResources.fetchPOM(artifact);
        return hasGroupAndArtifactId(pom,groupId,artifactId);
    }

    public static boolean hasGroupAndArtifactId(Path pom,String groupId, String artifactId) throws Exception {
        String pomArtifactId = getElementText(pom,"/project/artifactId");
        String pomGroupId = getElementText(pom,"/project/groupId");
        if (pomGroupId==null) {
            // check whether this has been inherited from parent
            pomGroupId = getElementText(pom,"/project/parent/groupId");
        }
        return Objects.equals(pomArtifactId,artifactId) && Objects.equals(pomGroupId,groupId);

    }

    private static String getElementText(Path xml, String xpath) throws Exception {
        NodeList aNode = Utils.evalXPath(xml.toFile(), xpath);
        if (aNode.getLength()>0) {
            return aNode.item(0).getTextContent();
        }
        return null;
    }

    public static List<MVNDependency> getDependencies(File pom) throws Exception {
        // @TODO support inheriting versions when using <dependencyManagement>
        NodeList nodeList1 = Utils.evalXPath(pom, "/project/dependencies/dependency");
        NodeList nodeList2 = Utils.evalXPath(pom, "//dependencyManagement/dependencies/dependency");
        NodeList mergedList = new NodeList() {
            @Override
            public Node item(int index) {
                return index < nodeList1.getLength() ? nodeList1.item(index) : nodeList2.item(index-nodeList1.getLength());
            }

            @Override
            public int getLength() {
                return nodeList1.getLength() + nodeList2.getLength();
            }
        };
        return MVNDependency.from(mergedList);
    }

    public static boolean shadePluginReferences(Artifact artifact, String groupId, String artifactId) throws Exception {
        Path pom = FetchResources.fetchPOM(artifact);
        return shadePluginReferences(pom,groupId,artifactId);
    }


        /**
         * We are looking here for explicit references, not just wildcard patterns including artifacts we are dependent on.
         * I.e. this is not to confirm which artifacts are included, but an additional source of information for
         * artifacts a pom depends on which were missed in the dependency analysis.
         */
    public static boolean shadePluginReferences(Path pom, String groupId, String artifactId) throws Exception {

        //  path starts with // -- plugin might be defined inside profiles
        NodeList nodes = Utils.evalXPath(pom.toFile(), "//plugins/plugin[artifactId='maven-shade-plugin']//includes/include");
        for (int i=0;i<nodes.getLength();i++) {
            String value = nodes.item(i).getTextContent();
            int groupIdIndex = value.indexOf(groupId);
            int artifactIdIndex = value.indexOf(artifactId);
            if (groupIdIndex>-1 && artifactIdIndex>groupIdIndex) {
                return true;
            }
        }
        return false;
    }

    public static boolean references(Path pom,String groupId,String artifactId) throws Exception {
        return hasDependency(pom,groupId,artifactId) || shadePluginReferences(pom,groupId,artifactId);
    }

    public static Path getParentPom (Path pom,GAV childGAV) throws Exception {
        String parentGroupId = getElementText(pom,"/project/parent/groupId");
        if (parentGroupId==null) {
            return null;
        }
        String parentArtifactId = getElementText(pom,"/project/parent/artifactId");
        if (parentArtifactId==null) {
            return null;
        }
        String parentVersion = getElementText(pom,"/project/parent/version");
        if (parentVersion==null) {
            return null;
        }
        else {
            // if this is a variable, use same version as child
            if (parentVersion.startsWith("${")) {
                parentVersion = childGAV.getVersion();
            }
        }

        GAV parentGAV = new GAV(parentGroupId, parentArtifactId, parentVersion);
        LOGGER.info("trying to fetch parent pom {} for child {}",parentGAV.asString(),childGAV.asString());
        return FetchResources.fetchPOM(parentGAV);
    }

    public static boolean references(Artifact artifact,String groupId,String artifactId) throws Exception {
        Path pom = FetchResources.fetchPOM(artifact);
        boolean hasReference =  hasDependency(pom,groupId,artifactId) || shadePluginReferences(pom,groupId,artifactId) || hasGroupAndArtifactId(pom,groupId,artifactId);
        if (!hasReference) {
            Path parentPOM = getParentPom(pom,artifact.asGAV());
            if (parentPOM != null) {
                hasReference = hasDependency(parentPOM, groupId, artifactId) || shadePluginReferences(parentPOM, groupId, artifactId) || hasGroupAndArtifactId(parentPOM, groupId, artifactId);
            }
        }
        return hasReference;
    }

}
