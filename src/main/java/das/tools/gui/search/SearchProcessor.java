package das.tools.gui.search;

import das.tools.gui.Utils;
import das.tools.gui.entity.AttrInfo;
import das.tools.gui.entity.SearchResult;
import das.tools.gui.entity.SearchResultList;
import das.tools.gui.entity.XmlTagInfo;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Enumeration;
import java.util.function.IntConsumer;

public class SearchProcessor {

    private static final int MAX_DISTANCE_OF_LENGTH_PERCENT = 30;
    private final SearchResultList searchResult;
    private final DefaultMutableTreeNode rootTreeNode;
    private final DefaultMutableTreeNode selectedTreeNode;
    private final SearchType searchType;
    private int currentResultPosition;
    private SearchResult currentSearchResultValue = null;
    private boolean isAfterStartSearchPosition;
    private int currentProgress;
    private IntConsumer progressListener = null;

    public SearchProcessor(String searchString, DefaultMutableTreeNode rootNode, DefaultMutableTreeNode selectedNode, SearchType searchType) {
        searchResult = new SearchResultList(searchString);
        this.rootTreeNode = rootNode;
        this.selectedTreeNode = selectedNode;
        this.searchType = searchType;
        this.currentResultPosition = -1;
        isAfterStartSearchPosition = false;
    }

    public void doSearch() {
        currentProgress = 0;
        searchInHierarchy(rootTreeNode);
    }

    /** Number of tag nodes to search, i.e. the final progress value. */
    public int getAllNodesCount() {
        return Utils.getNodesCount(rootTreeNode) - 1;
    }

    /** Visits nodes in document order (parent before its children), so "next" moves down the tree. */
    private void searchInHierarchy(DefaultMutableTreeNode root) {
        Enumeration<?> nodes = root.preorderEnumeration();
        nodes.nextElement(); // the root itself holds the file name, not a tag
        while (nodes.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodes.nextElement();
            if (node == selectedTreeNode) {
                isAfterStartSearchPosition = true;
            }
            searchInNode(node);
            currentProgress++;
            if (progressListener != null) {
                progressListener.accept(currentProgress);
            }
        }
    }

    private void searchInNode(DefaultMutableTreeNode node) {
        int searchStringPosition = 0;
        XmlTagInfo tagInfo = (XmlTagInfo) node.getUserObject();
        int searchLength = searchResult.getSearchString().length();
        if (searchType.isSearchInTagName()) {
            searchInWord(tagInfo.getTagName(), node, searchStringPosition);
            searchStringPosition = searchStringPosition + searchLength;
        }
        if (searchType.isSearchInTagValues() && Utils.isNotEmpty(tagInfo.getValue())) {
            searchInWord(tagInfo.getValue(), node, searchStringPosition);
            searchStringPosition = searchStringPosition + searchLength;
        }
        if (searchType.isSearchInAttrNames() || searchType.isSearchInAttrValues()) {
            if (tagInfo.getAttributes() != null && tagInfo.getAttributes().size() > 0) {
                for (AttrInfo attrInfo : tagInfo.getAttributes()) {
                    if (searchType.isSearchInAttrNames()) {
                        searchInWord(attrInfo.getName(), node, searchStringPosition);
                        searchStringPosition = searchStringPosition + searchLength;
                    }
                    if (searchType.isSearchInAttrValues()) {
                        searchInWord(attrInfo.getValue(), node, searchStringPosition);
                        searchStringPosition = searchStringPosition + searchLength;
                    }
                }
            }
        }
    }

    private void searchInWord(String currentWord, DefaultMutableTreeNode node, int pos) {
        String searchString = searchResult.getSearchString();
        int stringLength = searchString.length();
        if (currentWord.toLowerCase().contains(searchString.toLowerCase()) ||
                (distanceOfLengthPercent(currentWord, searchString, stringLength) <= MAX_DISTANCE_OF_LENGTH_PERCENT)) {
            XmlTagInfo tagInfo = (XmlTagInfo) node.getUserObject();
            searchResult.putIntoResult(node, tagInfo.getText().toLowerCase().indexOf(searchString.toLowerCase(), pos), stringLength,
                    selectedTreeNode != null && isAfterStartSearchPosition);
        }
    }

    private int distanceOfLengthPercent(String currentWord, String searchString, int stringLength) {
        return LevenshteinDistance.calculateDynamic(searchString.toLowerCase(), currentWord.toLowerCase()) * 100 / stringLength;
    }

    public boolean isNotEmptyResult() {
        return searchResult != null && searchResult.getResults() != null && searchResult.getResults().size() > 0;
    }

    public SearchResult getNextSearchResult(/*boolean wrap*/) {
        if (currentResultPosition < searchResult.getResults().size() - 1) {
            currentResultPosition++;
        } else {
            currentResultPosition = 0;
        }
        currentSearchResultValue = searchResult.getResults().get(currentResultPosition);
        return currentSearchResultValue;
    }

    public SearchResult getPrevSearchResult(/*boolean wrap*/) {
        if (currentResultPosition > 0) {
            currentResultPosition--;
        } else {
            currentResultPosition = searchResult.getResults().size() - 1;
        }
        currentSearchResultValue = searchResult.getResults().get(currentResultPosition);
        return currentSearchResultValue;
    }

    /**
     * Positions on the first result at or after the node selected when the search started.
     *
     * @return false if there is no such result (all matches are before the selected node)
     */
    public boolean setCurrentSearchResultValue() {
        for (int i = 0; i < searchResult.getResults().size(); i++) {
            if (searchResult.getResults().get(i).isStartSearchPositionHere()) {
                currentResultPosition = i;
                currentSearchResultValue = searchResult.getResults().get(currentResultPosition);
                return true;
            }
        }
        return false;
    }

    public SearchResult getCurrentSearchResultValue() {
        return currentSearchResultValue;
    }

    public int getCurrentResultPosition() {
        return currentResultPosition;
    }

    public int getTotalResults() {
        return isNotEmptyResult() ? searchResult.getResults().size() : 0;
    }

    /** Called from the searching thread with the number of processed nodes. */
    public void setProgressListener(IntConsumer progressListener) {
        this.progressListener = progressListener;
    }

    @Override
    public String toString() {
        return "SearchProcessor{" +
                "searchResult=" + searchResult +
                ", rootTreeNode=" + rootTreeNode +
                ", selectedTreeNode=" + selectedTreeNode +
                ", searchType=" + searchType +
                ", currentResultPosition=" + currentResultPosition +
                ", currentSearchResultValue=" + currentSearchResultValue +
                ", isAfterStartSearchPosition=" + isAfterStartSearchPosition +
                ", currentProgress=" + currentProgress +
                ", progressListener=" + progressListener +
                '}';
    }
}
