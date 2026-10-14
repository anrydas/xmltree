package das.tools.gui.search;

import das.tools.gui.entity.SearchResult;
import das.tools.gui.entity.XmlFile;
import das.tools.gui.entity.XmlTagInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.w3c.dom.Node;

import javax.swing.tree.DefaultMutableTreeNode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class SearchProcessorTest {
    private static final SearchType EVERYWHERE = new SearchType(true, true, true, true);

    @TempDir
    Path dir;

    private DefaultMutableTreeNode tree(String xml) throws Exception {
        Path file = dir.resolve("t.xml");
        Files.write(file, xml.getBytes(StandardCharsets.UTF_8));
        Node root = new XmlFile(file.toString()).getRootNode();
        DefaultMutableTreeNode top = new DefaultMutableTreeNode("t.xml");
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(new XmlTagInfo(root));
        top.add(rootNode);
        add(rootNode, root);
        return top;
    }

    private void add(DefaultMutableTreeNode parent, Node element) {
        for (Node n = element.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                DefaultMutableTreeNode child = new DefaultMutableTreeNode(new XmlTagInfo(n));
                parent.add(child);
                add(child, n);
            }
        }
    }

    private static DefaultMutableTreeNode lastLeaf(DefaultMutableTreeNode top) {
        return top.getLastLeaf();
    }

    @Test
    void findsMatchPositionRegardlessOfCase() throws Exception {
        DefaultMutableTreeNode top = tree("<root><item name=\"alpha\"/></root>");
        SearchProcessor p = new SearchProcessor("ALPHA", top, null, EVERYWHERE);
        p.doSearch();
        assertTrue(p.isNotEmptyResult());
        SearchResult r = p.getNextSearchResult();
        assertTrue(r.getResultStart() >= 0, "match position must be found in the text");
    }

    @Test
    void noStartPositionWhenAllMatchesAreBeforeSelectedNode() throws Exception {
        DefaultMutableTreeNode top = tree("<root><target/><other/><last/></root>");
        DefaultMutableTreeNode selected = lastLeaf(top);
        SearchProcessor p = new SearchProcessor("target", top, selected, new SearchType(true, false, false, false));
        p.doSearch();
        assertTrue(p.isNotEmptyResult());
        assertFalse(p.setCurrentSearchResultValue());
        assertNotNull(p.getNextSearchResult(), "navigation must still work by wrapping around");
    }

    @Test
    void reportsProgressForEveryNode() throws Exception {
        DefaultMutableTreeNode top = tree("<root><a/><b><c/></b></root>");
        SearchProcessor p = new SearchProcessor("zzz", top, null, EVERYWHERE);
        int[] last = {0};
        p.setProgressListener(v -> last[0] = v);
        p.doSearch();
        assertEquals(4, last[0]);
    }

    @Test
    void resultsAreInDocumentOrder() throws Exception {
        DefaultMutableTreeNode top = tree("<root><item><item/></item><x/><item/></root>");
        SearchProcessor p = new SearchProcessor("item", top, null, new SearchType(true, false, false, false));
        p.doSearch();
        assertEquals(3, p.getTotalResults());
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) top.getFirstChild();
        DefaultMutableTreeNode first = (DefaultMutableTreeNode) root.getChildAt(0);
        assertSame(first, p.getNextSearchResult().getTreeNode(), "parent comes before its child");
        assertSame(first.getFirstChild(), p.getNextSearchResult().getTreeNode());
        assertSame(root.getChildAt(2), p.getNextSearchResult().getTreeNode());
    }

    @Test
    void startsFromSelectedNode() throws Exception {
        DefaultMutableTreeNode top = tree("<root><item/><sel/><item/></root>");
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) top.getFirstChild();
        SearchProcessor p = new SearchProcessor("item", top, (DefaultMutableTreeNode) root.getChildAt(1),
                new SearchType(true, false, false, false));
        p.doSearch();
        assertTrue(p.setCurrentSearchResultValue());
        assertSame(root.getChildAt(2), p.getCurrentSearchResultValue().getTreeNode());
    }

    @Test
    void progressMaximumMatchesNodesSearched() throws Exception {
        DefaultMutableTreeNode top = tree("<root><a/><b><c/></b></root>");
        assertEquals(4, new SearchProcessor("x", top, null, EVERYWHERE).getAllNodesCount());
    }

    @Test
    void levenshtein() {
        assertEquals(0, LevenshteinDistance.calculateDynamic("abc", "abc"));
        assertEquals(3, LevenshteinDistance.calculateDynamic("", "abc"));
        assertEquals(3, LevenshteinDistance.calculateDynamic("kitten", "sitting"));
    }
}
