package anonymized.shadedetector;

import anonymized.shadedetector.classselectors.SelectAll;
import anonymized.shadedetector.classselectors.SelectClassesFromList;
import anonymized.shadedetector.classselectors.SelectClassesWithComplexNames;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class ClassSelectorFactoryTest {

    @Test
    public void testSelectAll() {
        ClassSelector selector = new ClassSelectorFactory().create("all");
        assertNotNull(selector);
        assertEquals("all",selector.name());
        assertTrue(selector instanceof SelectAll);
    }

    @Test
    public void testSelectClassesWithComplexNames() {
        ClassSelector selector = new ClassSelectorFactory().create("complexnames");
        assertNotNull(selector);
        assertEquals("complexnames",selector.name());
        assertTrue(selector instanceof SelectClassesWithComplexNames);
    }

    @Test
    public void testSelectClassesWithComplexNamesMax42() {
        ClassSelector selector = new ClassSelectorFactory().create("complexnames?maxSize=42");
        assertNotNull(selector);
        assertTrue(selector instanceof SelectClassesWithComplexNames);
        assertEquals("complexnames",selector.name());
        assertTrue(selector instanceof SelectClassesWithComplexNames);
        SelectClassesWithComplexNames selectorX = (SelectClassesWithComplexNames)selector;
        assertEquals(42,selectorX.getMaxSize());
    }

    @Test
    public void testSelectClassesWithComplexNamesMax43() {
        ClassSelector selector = new ClassSelectorFactory().create("complexnames?maxSize=43");
        assertNotNull(selector);
        assertTrue(selector instanceof SelectClassesWithComplexNames);
        assertEquals("complexnames",selector.name());
        assertTrue(selector instanceof SelectClassesWithComplexNames);
        SelectClassesWithComplexNames selectorX = (SelectClassesWithComplexNames)selector;
        assertEquals(43,selectorX.getMaxSize());
    }

    @Test
    public void testSelectClassesFromListDefinedAsString1() {
        ClassSelector selector = new ClassSelectorFactory().create("list?list=Foo");
        assertNotNull(selector);
        assertTrue(selector instanceof SelectClassesFromList);
        assertEquals("list",selector.name());
        assertEquals(List.of("Foo"),((SelectClassesFromList)selector).getClassList());
    }

    @Test
    public void testSelectClassesFromListDefinedAsString2() {
        ClassSelector selector = new ClassSelectorFactory().create("list?list=Foo1,Foo2");
        assertNotNull(selector);
        assertTrue(selector instanceof SelectClassesFromList);
        assertEquals("list",selector.name());
        assertEquals(List.of("Foo1","Foo2"),((SelectClassesFromList)selector).getClassList());
    }

    @Test
    public void testSelectClassesFromListDefinedAsFile() {
        String file = ClassSelectorFactoryTest.class.getClassLoader().getResource("classlist.txt").getFile();
        ClassSelector selector = new ClassSelectorFactory().create("list?file="+file);
        assertNotNull(selector);
        assertTrue(selector instanceof SelectClassesFromList);
        assertEquals("list",selector.name());
        assertEquals(List.of("Transformer","ChainedTransformer","ConstantTransformer","InvokerTransformer","LazyMap"),((SelectClassesFromList)selector).getClassList());
    }

    @Test
    public void testNonExisting() {
        assertThrows(IllegalArgumentException.class, () -> new ClassSelectorFactory().create("foo"));
    }

}
