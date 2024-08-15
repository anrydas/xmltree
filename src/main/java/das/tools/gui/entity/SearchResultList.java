package das.tools.gui.entity;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class SearchResultList {
    private final String searchString;
    private final LinkedList<SearchResult> results;

    public SearchResultList(String searchString) {
        this.searchString = searchString;
        this.results = new LinkedList<>();
    }

    public void putIntoResult(DefaultMutableTreeNode treeNode, long start, int length, boolean startSearch) {
        this.results.add(new SearchResult(treeNode, start, length, startSearch));
    }

    public String getSearchString() {
        return searchString;
    }

    public List<SearchResult> getResults() {
        return results;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SearchResultList)) return false;
        SearchResultList that = (SearchResultList) o;
        return getSearchString().equals(that.getSearchString()) && getResults().equals(that.getResults());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSearchString(), getResults());
    }

    @Override
    public String toString() {
        return "SearchResult{" +
                "searchString='" + searchString + '\'' +
                ", results=" + results +
                '}';
    }
}
