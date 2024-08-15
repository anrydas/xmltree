package das.tools.gui.entity;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Objects;

public class SearchResult {
    private final DefaultMutableTreeNode treeNode;
    private final long resultStart;
    private final int resultLength;
    private boolean startSearchPositionHere;

    public SearchResult(DefaultMutableTreeNode treeNode, long start, int length, boolean startSearch) {
        this.treeNode = treeNode;
        resultStart = start;
        resultLength = length;
        startSearchPositionHere = startSearch;
    }

    public DefaultMutableTreeNode getTreeNode() {
        return treeNode;
    }

    public long getResultStart() {
        return resultStart;
    }

    public int getResultLength() {
        return resultLength;
    }

    public boolean isStartSearchPositionHere() {
        return startSearchPositionHere;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SearchResult)) return false;
        SearchResult that = (SearchResult) o;
        return getTreeNode().equals(that.getTreeNode());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTreeNode());
    }

    @Override
    public String toString() {
        return "SearchResults{" +
                "treeNode=" + treeNode +
                ", resultStart=" + resultStart +
                ", resultLength=" + resultLength +
                ", startSearchPositionHere=" + startSearchPositionHere +
                '}';
    }
}
