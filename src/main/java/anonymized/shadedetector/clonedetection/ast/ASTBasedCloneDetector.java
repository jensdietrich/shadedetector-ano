package anonymized.shadedetector.clonedetection.ast;

import anonymized.shadedetector.CloneDetector;
import anonymized.shadedetector.Utils;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Clone detection based on comparing ASTs.
 * @author jens dietrich
 */
public class ASTBasedCloneDetector implements CloneDetector {

    private static Logger LOGGER = LoggerFactory.getLogger(ASTBasedCloneDetector.class);
    public static final Predicate<Node> IS_RELEVANT_CHILD_NODE = node -> !(node instanceof Comment) && !(node instanceof ImportDeclaration) && !(node instanceof PackageDeclaration);


    @Override
    public String name() {
        return "ast";
    }

    @Override
    public Set<CloneRecord> detect(Path original, Path cloneCandidate) {
        try {
            List<Path> originalJavaSources = Utils.listJavaSources(original,true);
            List<Path> cloneCandidateJavaSources = Utils.listJavaSources(cloneCandidate,true);

            List<Pair<Path,Path>> potentialMatches = new ArrayList<>();
            for (Path originalSource:originalJavaSources) {
                String cuName1 = originalSource.getName(originalSource.getNameCount()-1).toString();
                // skip package-info.java with package meta data
                if (!cuName1.equals("package-info.java")) {
                    for (Path cloneSource : cloneCandidateJavaSources) {
                        String cuName2 = cloneSource.getName(cloneSource.getNameCount() - 1).toString();
                        if (Objects.equals(cuName1, cuName2)) {
                            potentialMatches.add(new Pair<>(originalSource, cloneSource));
                        }
                    }
                }
            }

            LOGGER.info("Analysing {} pairs of java source code",potentialMatches.size());

            return potentialMatches.stream()
                .map(match -> analyseClone(match.a,match.b))
                .collect(Collectors.toSet());
        }
        catch (IOException x) {
            LOGGER.error("Error extracting Java sources from {},{}",original,cloneCandidate,x);
        }
        return Collections.EMPTY_SET;
    }

    static CloneRecord analyseClone(Path path1, Path path2) {
        try {
            CompilationUnit cu1 = StaticJavaParser.parse(path1);
            CompilationUnit cu2 = StaticJavaParser.parse(path2);
            String pck1 = cu1.getPackageDeclaration().isPresent() ? cu1.getPackageDeclaration().get().getNameAsString() : "";
            String pck2 = cu2.getPackageDeclaration().isPresent() ? cu1.getPackageDeclaration().get().getNameAsString() : "";
            // boolean samePackage = Objects.equals(pck1,pck2);
            return new CloneRecord(analyseClone(cu1,cu2)?1.0:0.0,path1,path2);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static boolean analyseClone(Node node1, Node node2) {

        // refactoring might change access to static variable eg Foo.FIELD vs com.example.Foo.FIELD
        // then com.example.Foo.FIELD is a FieldAccessExpr
        if (node1 instanceof FieldAccessExpr && node2 instanceof NameExpr) {
            return analyseCloneForFieldAccess((FieldAccessExpr)node1,(NameExpr)node2);
        }
        else if (node1 instanceof NameExpr && node2 instanceof FieldAccessExpr) {
            return analyseCloneForFieldAccess((FieldAccessExpr)node2,(NameExpr)node1);
        }

        // but normally node types should be the same
        if (node1.getClass() != node2.getClass()) {  // must be of the same kind
            return false;
        }
        if (node1 instanceof ClassOrInterfaceType) {
            return analyseCloneForClassOrInterfaceType((ClassOrInterfaceType)node1,(ClassOrInterfaceType)node2);
        }
        else if (node1 instanceof SimpleName) {
            return analyseCloneForSimpleNames((SimpleName)node1,(SimpleName)node2);
        }
        else if (node1 instanceof LiteralStringValueExpr)  {
            return analyseCloneForLiterals((LiteralStringValueExpr)node1, (LiteralStringValueExpr)node2);
        }
        else if (node1 instanceof BooleanLiteralExpr)  {
            return analyseCloneForLiterals((BooleanLiteralExpr)node1, (BooleanLiteralExpr)node2);
        }
        else if (node1 instanceof BinaryExpr)  {
            return analyseCloneForBinaryExpressions((BinaryExpr)node1, (BinaryExpr)node2);
        }
        else if (node1 instanceof UnaryExpr)  {
            return analyseCloneForUnaryExpressions((UnaryExpr)node1, (UnaryExpr)node2);
        }
        else if (node1 instanceof MethodCallExpr) {
            return analyseCloneForMethodCallExpr((MethodCallExpr)node1,(MethodCallExpr)node2);
        }

        List<Node> relevantChildNodes1 = node1.getChildNodes().stream().filter(IS_RELEVANT_CHILD_NODE).collect(Collectors.toList());
        List<Node> relevantChildNodes2 = node2.getChildNodes().stream().filter(IS_RELEVANT_CHILD_NODE).collect(Collectors.toList());
        return analyseChildNodes(relevantChildNodes1,relevantChildNodes2);
    }

    static boolean analyseCloneForFieldAccess(FieldAccessExpr node1, NameExpr node2) {
       String typeName1 = node1.getNameAsString();
       String typeName2 = node2.getNameAsString();
       return typeName1.equals(typeName2);
    }

    static boolean analyseChildNodes(List<Node> childNodes1,List<Node> childNodes2) {
        if (childNodes1.size()!=childNodes2.size()) {
            return false;
        }
        boolean result = true;
        for (int i=0;i<childNodes1.size();i++) {
            Node childNode1 = childNodes1.get(i);
            Node childNode2 = childNodes2.get(i);
            result = result && analyseClone(childNode1,childNode2);
        }
        return result;
    }

    static boolean analyseCloneForClassOrInterfaceType(ClassOrInterfaceType node1, ClassOrInterfaceType node2) {
        // this is the actual relocation check -- packages (scope) are ignored
        boolean hasScope1 = node1.getScope().isPresent();
        List<Node> relevantChildNodes1 = node1.getChildNodes().stream()
            .filter(child -> !hasScope1 || node1.getScope().get()!=child)
            .collect(Collectors.toList());
        boolean hasScope2 = node2.getScope().isPresent();
        List<Node> relevantChildNodes2 = node2.getChildNodes().stream()
            .filter(child -> !hasScope2 || node2.getScope().get()!=child)
            .collect(Collectors.toList());
        return analyseChildNodes(relevantChildNodes1,relevantChildNodes2);
    }

    static boolean analyseCloneForMethodCallExpr(MethodCallExpr node1, MethodCallExpr node2) {

        boolean hasScope1 = node1.getScope().isPresent();
        List<Node> relevantChildNodes1 = node1.getChildNodes().stream()
            .filter(child -> !hasScope1 || node1.getScope().get()!=child)
            .collect(Collectors.toList());
        boolean hasScope2 = node2.getScope().isPresent();
        List<Node> relevantChildNodes2 = node2.getChildNodes().stream()
            .filter(child -> !hasScope2 || node2.getScope().get()!=child)
            .collect(Collectors.toList());
        return analyseChildNodes(relevantChildNodes1,relevantChildNodes2);
    }



    // compare leaf nodes for equality
    // need to check whether equals methods already do this !
    static boolean analyseCloneForSimpleNames(SimpleName node1, SimpleName node2) {
        return node1.getId().equals(node2.getId());
    }

    static boolean analyseCloneForLiterals(LiteralStringValueExpr node1, LiteralStringValueExpr node2) {
        return node1.getValue().equals(node2.getValue());
    }

    static boolean analyseCloneForLiterals(BooleanLiteralExpr node1, BooleanLiteralExpr node2) {
        return node1.getValue()==node2.getValue();
    }

    static boolean analyseCloneForBinaryExpressions(BinaryExpr node1, BinaryExpr node2) {
        return node1.getOperator() == node2.getOperator() &&
            analyseClone(node1.getLeft(),node2.getLeft())
            && analyseClone(node1.getRight(),node2.getRight())
            ;
    }

    static boolean analyseCloneForUnaryExpressions(UnaryExpr node1, UnaryExpr node2) {
        return node1.getOperator() == node2.getOperator() &&
            analyseClone(node1.getExpression(),node2.getExpression())
            ;
    }


}
