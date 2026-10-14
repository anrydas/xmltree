package das.tools.gui.search;

import das.tools.gui.GuiView;
import das.tools.gui.Utils;
import das.tools.gui.entity.SearchResult;
import das.tools.gui.menu.action.AppActions;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

/** Search panel behaviour of one window: running the search and navigating its results. */
public class SearchController {
    private final GuiView guiView;
    private final AppActions appActions;
    private SearchProcessor searchProcessor;

    public SearchController(GuiView guiView, AppActions appActions) {
        this.guiView = guiView;
        this.appActions = appActions;
        guiView.getSearchButton().addActionListener((e) -> startSearch());
        guiView.getPreviousButton().addActionListener((e) -> findBackward());
        guiView.getNextButton().addActionListener((e) -> findForward());
    }

    /** Shows the panel; if it's already shown, clears the text, or hides the panel when the text is empty. */
    public void toggle() {
        JPanel searchPanel = guiView.getSearchPanel();
        if (!searchPanel.isVisible()) {
            showPanel();
        } else {
            JTextField textField = guiView.getSearchTextField();
            if (Utils.isNotEmpty(textField.getText())) {
                textField.setText("");
                textField.requestFocus();
                reset();
            } else {
                hidePanel();
            }
        }
    }

    private void showPanel() {
        reset();
        guiView.getSearchPanel().setVisible(true);
        guiView.getSearchTextField().requestFocus();
    }

    public void hidePanel() {
        guiView.getSearchPanel().setVisible(false);
        reset();
    }

    private void reset() {
        searchProcessor = null;
        setSearchResultText(0, 0);
        setFwdBwdControlsEnabled(false);
    }

    private void setSearchResultText(int current, int total) {
        guiView.getFoundLabel().setText(String.format(GuiView.FOUND_RESULT_LABEL, (total > 0) ? current + 1 : 0, total));
    }

    private void startSearch() {
        reset();
        String searchString = guiView.getSearchTextField().getText();
        SearchType searchType = new SearchType(guiView.getSearchInTagNames().isSelected(),
                guiView.getSearchInTagValues().isSelected(),
                guiView.getSearchInAttrNames().isSelected(),
                guiView.getSearchInAttrValues().isSelected());
        searchProcessor = new SearchProcessor(searchString, getRootNode(), getSelectedNode(), searchType);
        guiView.getSearchButton().setEnabled(false);
        guiView.getSearchProgressBar().setVisible(true);
        new SearchTask(this, searchProcessor, guiView.getSearchProgressBar()).execute();
    }

    void searchCompleted() {
        DefaultMutableTreeNode selectedNode = getSelectedNode();
        if (searchProcessor.isNotEmptyResult()) {
            if (selectedNode != null && selectedNode != getRootNode() && searchProcessor.setCurrentSearchResultValue()) {
                showResultInTree(searchProcessor.getCurrentSearchResultValue());
            } else {
                findForward();
            }
            setFwdBwdControlsEnabled(true);
        }
        setSearchResultText(searchProcessor.getCurrentResultPosition(), searchProcessor.getTotalResults());
        guiView.getSearchButton().setEnabled(true);
        guiView.getSearchProgressBar().setVisible(false);
    }

    JComponent getDialogParent() {
        return guiView.getRootPanel();
    }

    private DefaultMutableTreeNode getRootNode() {
        return (DefaultMutableTreeNode) guiView.getTree().getModel().getRoot();
    }

    private DefaultMutableTreeNode getSelectedNode() {
        return (DefaultMutableTreeNode) guiView.getTree().getLastSelectedPathComponent();
    }

    private void setFwdBwdControlsEnabled(boolean value) {
        appActions.getFindForwardAction().setEnabled(value);
        appActions.getFindBackwardAction().setEnabled(value);
        guiView.getPreviousButton().setEnabled(value);
        guiView.getNextButton().setEnabled(value);
    }

    public void findForward() {
        doFind(true);
    }

    public void findBackward() {
        doFind(false);
    }

    private void doFind(boolean forward) {
        if (searchProcessor != null && searchProcessor.isNotEmptyResult()) {
            if (forward) {
                showResultInTree(searchProcessor.getNextSearchResult());
            } else {
                showResultInTree(searchProcessor.getPrevSearchResult());
            }
            setSearchResultText(searchProcessor.getCurrentResultPosition(), searchProcessor.getTotalResults());
            setFindPositionText();
        }
    }

    private void setFindPositionText() {
        JTextArea text = guiView.getRawXmlText();
        text.requestFocus();
        SearchResult result = searchProcessor.getCurrentSearchResultValue();
        int start = (int) result.getResultStart();
        if (start > -1) {
            text.setSelectionStart(start);
            text.setSelectionEnd(start + result.getResultLength());
        } else {
            text.setSelectionStart(0);
            text.setSelectionEnd(text.getText().length());
        }
    }

    private void showResultInTree(SearchResult result) {
        TreePath path = new TreePath(result.getTreeNode().getPath());
        JTree tree = guiView.getTree();
        tree.setSelectionPath(path);
        tree.scrollPathToVisible(path);
        tree.expandPath(path);
    }
}
