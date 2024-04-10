package anonymized.shadedetector;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a dependency.
 * @author jens dietrich
 */
public class MVNDependency {

    public static MVNDependency from(Node xmlNode) {
        MVNDependency dep = new MVNDependency();
        NodeList children = xmlNode.getChildNodes();
        for (int i=0;i<children.getLength();i++) {
            Node node = children.item(i);
            if (node instanceof Element) {
                if (node.getNodeName().equals("groupId")) {
                    dep.groupId = node.getTextContent().trim();
                }
                else if (node.getNodeName().equals("artifactId")) {
                    dep.artifactId = node.getTextContent();
                }
                else if (node.getNodeName().equals("scope")) {
                    dep.scope = node.getTextContent().trim();
                }
                else if (node.getNodeName().equals("version")) {
                    dep.version = node.getTextContent().trim();
                }
            }
        }
        return dep;
    }

    public MVNDependency() {
    }

    public MVNDependency(String groupId, String artifactId, String version, String scope) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.scope = scope;
    }

    public MVNDependency(String groupId, String artifactId) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = null;
        this.scope = null;
    }

    public static List<MVNDependency> from(NodeList xmlNodes) {
        List<MVNDependency> deps = new ArrayList<>(xmlNodes.getLength());
        for (int i=0;i<xmlNodes.getLength();i++) {
            Node node = xmlNodes.item(i);
            MVNDependency dep = from(node);
            deps.add(dep);
        }
        return deps;
    }

    private String groupId = null;
    private String artifactId = null;
    private String version = null;
    private String scope = null;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public boolean equalsSomeVersionOf(MVNDependency that, boolean ignoreScope) {
        return Objects.equals(groupId, that.groupId) &&
                Objects.equals(artifactId, that.artifactId) &&
                (ignoreScope || Objects.equals(scope,that.scope));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MVNDependency that = (MVNDependency) o;
        return Objects.equals(groupId, that.groupId) &&
                Objects.equals(artifactId, that.artifactId) &&
                Objects.equals(version, that.version) &&
                Objects.equals(scope, that.scope);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, artifactId, version, scope);
    }

    @Override
    public String toString() {
        return "POMDependency{" +
                "groupId='" + groupId + '\'' +
                ", artifactId='" + artifactId + '\'' +
                ", version='" + version + '\'' +
                ", scope='" + scope + '\'' +
                '}';
    }
}
