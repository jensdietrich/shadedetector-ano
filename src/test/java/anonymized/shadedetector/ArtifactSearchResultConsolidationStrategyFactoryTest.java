package anonymized.shadedetector;

import anonymized.shadedetector.resultsetconsolidation.ArtifactOccursInAllResultSets;
import anonymized.shadedetector.resultsetconsolidation.ArtifactOccursInAnyResultSet;
import anonymized.shadedetector.resultsetconsolidation.ArtifactOccursInMoreThanOneResultSets;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ArtifactSearchResultConsolidationStrategyFactoryTest {

    @Test
    public void testSelectAll() {
        ArtifactSearchResultConsolidationStrategy strategy = new ArtifactSearchResultConsolidationStrategyFactory().create("all");
        assertNotNull(strategy);
        assertEquals("all",strategy.name());
        assertTrue(strategy instanceof ArtifactOccursInAllResultSets);
    }

    @Test
    public void testSelectAny() {
        ArtifactSearchResultConsolidationStrategy strategy = new ArtifactSearchResultConsolidationStrategyFactory().create("any");
        assertNotNull(strategy);
        assertEquals("any",strategy.name());
        assertTrue(strategy instanceof ArtifactOccursInAnyResultSet);
    }

    @Test
    public void testSelectMoreThanOne() {
        ArtifactSearchResultConsolidationStrategy strategy = new ArtifactSearchResultConsolidationStrategyFactory().create("moreThanOne");
        assertNotNull(strategy);
        assertEquals("moreThanOne",strategy.name());
        assertTrue(strategy instanceof ArtifactOccursInMoreThanOneResultSets);
    }

    @Test
    public void testNonExisting() {
        assertThrows(IllegalArgumentException.class, () -> new ArtifactSearchResultConsolidationStrategyFactory().create("foo"));
    }

}
