package das.tools.gui.search;

import das.tools.gui.Utils;
import das.tools.gui.entity.AttrInfo;
import das.tools.gui.entity.SearchResult;
import das.tools.gui.entity.SearchResultList;
import das.tools.gui.entity.XmlTagInfo;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

public class SearchProcessor {

    private static final int MAX_DISTANCE_OF_LENGTH_PERCENT = 30;
    private final SearchResultList searchResult;
    private final DefaultMutableTreeNode rootTreeNode;
    private final DefaultMutableTreeNode selectedTreeNode;
    private boolean isSearchCompleted;
    private final SearchType searchType;
    private int currentResultPosition;
    private SearchResult currentSearchResultValue = null;
    private boolean isAfterStartSearchPosition;
    private int currentProgress;
    private JProgressBar progressBar = null;

    public SearchProcessor(String searchString, DefaultMutableTreeNode rootNode, DefaultMutableTreeNode selectedNode, SearchType searchType) {
        searchResult = new SearchResultList(searchString);
        this.rootTreeNode = rootNode;
        this.selectedTreeNode = selectedNode;
        this.searchType = searchType;
        this.currentResultPosition = -1;
        isAfterStartSearchPosition = false;
    }

    public SearchResultList getSearchResult() {
        if (!isSearchCompleted) {
            doSearch();
        }
        return searchResult;
    }

    public void doSearch() {
        currentProgress = 0;
        if (progressBar != null) {
            progressBar.setMinimum(0);
            progressBar.setMaximum(getAllNodesCount());
            progressBar.setValue(0);
        }
        searchInHierarchy(rootTreeNode);
        isSearchCompleted = true;
    }

    private int getAllNodesCount() {
        return Utils.getNodesCount(rootTreeNode);
    }

    public int getCurrentProgress() {
        return currentProgress;
    }

    private void searchInHierarchy(DefaultMutableTreeNode node) {
        int childCount = node.getChildCount();
        for (int i = 0; i < childCount; i++) {
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);
            if (childNode == selectedTreeNode) {
                isAfterStartSearchPosition = true;
            }
            if (childNode.getChildCount() > 0) {
                searchInHierarchy(childNode);
            }
            searchInNode(childNode);
            currentProgress++;
            if (progressBar != null) {
                progressBar.setValue(currentProgress);
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
            searchResult.putIntoResult(node, tagInfo.getText().toLowerCase().indexOf(searchString, pos), stringLength,
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

    public void setCurrentSearchResultValue() {
        for (int i = 0; i < searchResult.getResults().size(); i++) {
            if (searchResult.getResults().get(i).isStartSearchPositionHere()) {
                currentResultPosition = i;
                currentSearchResultValue = searchResult.getResults().get(currentResultPosition);
                return;
            }
        }
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

    public void setProgressBar(JProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    @Override
    public String toString() {
        return "SearchProcessor{" +
                "searchResult=" + searchResult +
                ", rootTreeNode=" + rootTreeNode +
                ", selectedTreeNode=" + selectedTreeNode +
                ", isSearchCompleted=" + isSearchCompleted +
                ", searchType=" + searchType +
                ", currentResultPosition=" + currentResultPosition +
                ", currentSearchResultValue=" + currentSearchResultValue +
                ", isAfterStartSearchPosition=" + isAfterStartSearchPosition +
                ", currentProgress=" + currentProgress +
                ", progressBar=" + progressBar +
                '}';
    }
}
