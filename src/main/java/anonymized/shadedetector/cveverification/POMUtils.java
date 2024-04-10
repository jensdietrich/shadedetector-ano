package anonymized.shadedetector.cveverification;

import anonymized.shadedetector.GAV;
import com.google.common.base.Preconditions;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

import javax.xml.XMLConstants;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility to query and manipulate POMs.
 * @author jens dietrich
 */
public class POMUtils {

    public static Document parsePOM (Path pom) throws IOException, JDOMException {
        SAXBuilder sax = new SAXBuilder();
        sax.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        sax.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return sax.build(pom.toFile());
    }

    public static Element findDependency(Path pom, GAV dependency) throws IOException, JDOMException {
        Document doc = parsePOM(pom);
        return findDependency(pom,doc,dependency);
    }

    public static Element findDependency(Path pom, Document doc, GAV dependency) {
        Element root = doc.getRootElement();
        Preconditions.checkArgument(root.getName().equals("project"),"not a valid pom (invalid root): " + pom);
        List<Element> dependenciesRoot = getChildren(root,"dependencies");
        Preconditions.checkArgument(dependenciesRoot.size()>0,"no dependencies found in pom: " + pom);
        List<Element> dependencies = getChildren(dependenciesRoot.get(0),"dependency");
        for (Element d:dependencies) {
            if (
                getChildText(d,"groupId").equals(dependency.getGroupId())
                    && getChildText(d,"artifactId").equals(dependency.getArtifactId())
                    && getChildText(d,"version").equals(dependency.getVersion())
            ) {
                return d;
            }
        }
        return null;
    }

    public static void replaceDependency(Path pom,GAV original, GAV replacement) throws IOException, JDOMException {
        Document doc = parsePOM(pom);
        replaceDependency(pom,doc,original,replacement);
    }

    public static void replaceDependency(Path pom, Document doc, GAV original, GAV replacement) throws IOException {
        Element originalE = findDependency(pom,doc,original);

        Element element = getChild(originalE,"groupId");
        Preconditions.checkState(element!=null,"no groupId element found in cloned dependency");
        element.setText(replacement.getGroupId());

        element = getChild(originalE,"artifactId");
        Preconditions.checkState(element!=null,"no artifactId element found in cloned dependency");
        element.setText(replacement.getArtifactId());

        element = getChild(originalE,"version");
        Preconditions.checkState(element!=null,"no version element found in cloned dependency");
        element.setText(replacement.getVersion());

        XMLOutputter xmlOutputter = new XMLOutputter();
        try (OutputStream out = new FileOutputStream(pom.toFile())) {
            xmlOutputter.output(doc,out);
        }
    }

    public static void replaceCoordinates(Path pom, GAV newCoordinates) throws IOException, JDOMException {
        Document doc = parsePOM(pom);
        replaceCoordinates(pom,doc,newCoordinates);
    }
    public static void replaceCoordinates(Path pom, Document doc, GAV newCoordinates) throws IOException {
        Element root = doc.getRootElement();
        Preconditions.checkArgument(root!=null,"no project root found in pom " + pom);
        Preconditions.checkArgument(root.getName().equals("project"),"no project root found in pom " + pom);

        Element element = getChild(root, "groupId");
        Preconditions.checkState(element!=null,"no groupId element found in pom project root");
        element.setText(newCoordinates.getGroupId());

        element = getChild(root,"artifactId");
        Preconditions.checkState(element!=null,"no artifactId element found in pom project root");
        element.setText(newCoordinates.getArtifactId());

        element = getChild(root,"version");
        Preconditions.checkState(element!=null,"no version element found in pom project root");
        element.setText(newCoordinates.getVersion());

        XMLOutputter xmlOutputter = new XMLOutputter();
        try (OutputStream out = new FileOutputStream(pom.toFile())) {
            xmlOutputter.output(doc,out);
        }
    }

    public static GAV getCoordinates(Path pom, Document doc) {
        Element root = doc.getRootElement();
        Preconditions.checkArgument(root!=null,"no project root found in pom " + pom);
        Preconditions.checkArgument(root.getName().equals("project"),"no project root found in pom " + pom);

        Element elementGroup = getChild(root, "groupId");
        Preconditions.checkState(elementGroup!=null,"no groupId element found in pom project root");

        Element elementArtifact = getChild(root,"artifactId");
        Preconditions.checkState(elementArtifact!=null,"no artifactId element found in pom project root");

        Element elementVersion = getChild(root,"version");
        Preconditions.checkState(elementVersion!=null,"no version element found in pom project root");

        return new GAV(elementGroup.getText(),elementArtifact.getText(),elementVersion.getText());
    }

    public static GAV getCoordinates(Path pom) throws IOException, JDOMException {
        Document doc = parsePOM(pom);
        return getCoordinates(pom,doc);
    }

    // utility for name space - agnostic navigation
    private static Element getChild(Element parent, String name) {
        for (Element child:parent.getChildren()) {
            if (child.getName().equals(name)) {
                return child;
            }
        }
        return null;
    }

    private static String getChildText(Element parent, String name) {
        Element child = getChild(parent,name);
        return child==null?null:child.getText();
    }

    private static List<Element> getChildren(Element parent, String name) {
        return parent.getChildren().stream()
            .filter(child -> child.getName().equals(name))
            .collect(Collectors.toList());
    }


}
