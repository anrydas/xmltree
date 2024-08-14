package das.tools.gui;

import das.tools.gui.entity.*;
import das.tools.gui.menu.PopupClickListener;
import das.tools.gui.menu.AppMenus;
import das.tools.gui.menu.action.AppActions;
import das.tools.gui.search.SearchProcessor;
import das.tools.gui.search.SearchTask;
import das.tools.gui.search.SearchType;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class XmlForm implements TreeSelectionListener/*, PopupMenuAction*/ {
    public static final JFileChooser dlgOpen = new JFileChooser();
    protected static final String FMT_FILE_NAME = "File: <b>%s</b> (<i>%s</i>)";
    protected static final String FMT_TAG = "<i>tag:</i>&nbsp;<font color='blue' size='5'><b>%s</b></font>%s<br/>";
    protected static final String FMT_VALUE = "&nbsp;=&nbsp;\"<font color='green' size='5'><b>%s</b></font>\"";
    protected static final String FMT_ATTR = "<i>attr:</i>&nbsp;<font color='red' size='5'><b>%s</b></font>=\"<font color='green' size='5'><b>%s</b></font>\"<br/>";
    private final AppActions appActions;
    private XmlFile xmlFile = null;
    private String fileName = "";
    private static boolean isFirst = true;
    private static final java.util.List<JFrame> framesList;
    private JFrame ownFrame;
    private final GuiView guiView;
    private final AppMenus appMenus;
    private SearchProcessor searchProcessor;

    static {
        framesList = new ArrayList<>(5);
    }


    public XmlForm(String fileName) {
        this.fileName = fileName;
        appActions = new AppActions(this);
        this.appMenus = new AppMenus(this, appActions);
        guiView = new GuiView();
        guiView.getBtOpen().addActionListener(appActions.getOpenFileAction());
        guiView.getTextAttr().addMouseListener(new PopupClickListener(this, appMenus));
        guiView.getSearchButton().addActionListener((e) -> btSearchClicked());
        guiView.getPreviousButton().addActionListener((e) -> findBackward());
        guiView.getNextButton().addActionListener((e) -> findForward());
        guiView.getSearchProgressBar().addChangeListener(getChangeProgressListener());
        javax.swing.SwingUtilities.invokeLater(() -> {
            openFileDialogExecute();
            createGUI();
            loadDataIntoTree();
        });
    }

    private ChangeListener getChangeProgressListener() {
        return changeEvent -> {
            setSearchResultText(0, searchProcessor.getTotalResults());
        };
    }

    public void openFile() {
        String selectedFile = selectFile();
        if (Utils.isNotEmpty(selectedFile)) {
            new XmlForm(selectedFile);
        }
    }

    public AppMenus getAttrMenu() {
        return appMenus.getAttrMenu();
    }

    private String selectFile() {
        if (guiView.getRootPanel() == null) sleep();
        dlgOpen.setAcceptAllFileFilterUsed(false);
        dlgOpen.setFileFilter(new FilesFilter());
        int returnValue = dlgOpen.showOpenDialog(guiView.getRootPanel());
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            return dlgOpen.getSelectedFile().getAbsolutePath();
        }
        return "";
    }

    private void sleep() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public void createGUI() {
        JFrame frame = new JFrame();
        frame.setMinimumSize(new Dimension(800, 600));
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setApplicationIcon(frame);
        frame.setContentPane(guiView.getRootPanel());
        frame.setJMenuBar(guiView.getMenuBar());
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                framesList.remove(frame);
            }
        });
        framesList.add(frame);
        ownFrame = frame;
        JMenu fileMenu = guiView.getFileMenu();
        appMenus.getFileMenu(fileMenu);
        JMenu searchMenu = guiView.getSearchMenu();
        appMenus.getSearchMenu(searchMenu);
        JMenu windowsMenu = guiView.getWindowMenu();
        windowsMenu.addMenuListener(windowsMenuListener(windowsMenu));
        JMenu themeMenu = guiView.getThemeMenu();
        themeMenu.addMenuListener(themeMenuListener(themeMenu));
        addEscapeKeyAction(frame);
        frame.pack();
        frame.setVisible(true);
    }

    private void addEscapeKeyAction(JFrame frame) {
        KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        JComponent pn = frame.getRootPane();
        pn.getActionMap().put("Escape", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                hideSearchPanel();
            }
        });
        pn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, "Escape");
    }

    private void setApplicationIcon(JFrame frame) {
        URL resource = getClass().getResource("/images/xmltree0.png");
        if (resource != null) {
            frame.setIconImage(Toolkit.getDefaultToolkit().getImage(resource));
        }
    }

    private MenuListener themeMenuListener(JMenu menu) {
        return new MenuListener() {
            @Override
            public void menuSelected(MenuEvent menuEvent) {
                menu.removeAll();
                appMenus.getThemeMenu(menu);
            }

            @Override
            public void menuDeselected(MenuEvent menuEvent) {

            }

            @Override
            public void menuCanceled(MenuEvent menuEvent) {

            }
        };
    }

    private MenuListener windowsMenuListener(JMenu menu) {
        return new MenuListener() {
            @Override
            public void menuSelected(MenuEvent menuEvent) {
                menu.removeAll();
                appMenus.getWindowsMenu(menu);
            }

            @Override
            public void menuDeselected(MenuEvent menuEvent) {

            }

            @Override
            public void menuCanceled(MenuEvent menuEvent) {

            }
        };
    }

    private void loadDataIntoTree() {
        xmlFile = new XmlFile(fileName);
        DefaultMutableTreeNode top = new DefaultMutableTreeNode(xmlFile.getName());
        DefaultMutableTreeNode root =
                new DefaultMutableTreeNode(
                        new XmlTagInfo(xmlFile.getRootElement()));
        top.add(root);
        Thread thread = new Thread() {
            @Override
            public void run() {
                super.run();
                createNodes(root, xmlFile.getRootNode());
                ((JFrame) SwingUtilities.getWindowAncestor(guiView.getRootPanel())).setTitle(String.format("Xml Tree: %s", xmlFile.getName()));
            }
        };
        thread.setDaemon(true);
        thread.start();

        DefaultTreeModel model = new DefaultTreeModel(top);
        JTree tree = guiView.getTree();
        tree.setModel(model);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener(this);
        tree.setShowsRootHandles(true);
    }

    private void openFileDialogExecute() {
        if (isFirst) {
            isFirst = false;
            dlgOpen.setCurrentDirectory(new File(System.getProperty("user.dir")));
            String parameterFile = Main.getAppParameter();
            if (Utils.isNotEmpty(parameterFile)) {
                fileName = parameterFile;
                dlgOpen.setCurrentDirectory(new File(fileName));
            } else {
                fileName = selectFile();
            }
        }
    }

    private void createNodes(DefaultMutableTreeNode node, Node element) {
        DefaultMutableTreeNode treeNode = null;
        NodeList list = xmlFile.getChild(element);
        for (int i = 0; i < list.getLength(); i++) {
            Node n = list.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                XmlTagInfo tagInfo = new XmlTagInfo(n);
                treeNode = new DefaultMutableTreeNode(tagInfo);
                node.add(treeNode);
                createNodes(treeNode, n);
            }
        }
    }

    @Override
    public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)
                guiView.getTree().getLastSelectedPathComponent();
        if (treeNode == null) {
            //Nothing selected.
            return;
        }
        if (treeNode != guiView.getTree().getModel().getRoot()) {
            XmlTagInfo tag = (XmlTagInfo) treeNode.getUserObject();
            String tagValue = tag.getValue();
            StringBuilder tagText = new StringBuilder(String.format(FMT_TAG, tag,
                    Utils.isNotEmpty(tagValue) ? String.format(FMT_VALUE, tagValue) : ""));

            java.util.List<AttrInfo> attrs = tag.getAttributes();
            if (attrs != null) {
                for (AttrInfo a : attrs) {
                    tagText.append(String.format(FMT_ATTR, a.getName(), a.getValue()));
                }
            }
            guiView.getTextAttr().setText(tagText.toString());
            guiView.getRawXmlText().setText(tag.getText());
        } else {
            guiView.getTextAttr().setText(String.format(FMT_FILE_NAME, xmlFile.getFileName(), xmlFile.getFullFileName()));
            guiView.getRawXmlText().setText("");
        }
    }

    public boolean isCurrentlyRootSelected() {
        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)
                guiView.getTree().getLastSelectedPathComponent();
        return treeNode == null || treeNode.isRoot();
    }

    private void copyToClip(String value) {
        Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
        clip.setContents(new StringSelection(value), null);
    }

    private XmlTagInfo getCurrentTag() {
        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)
                guiView.getTree().getLastSelectedPathComponent();
        return (XmlTagInfo) treeNode.getUserObject();
    }

    public int getCurrentTagAttrCount() {
        int res = 0;
        if (getCurrentTag() != null && getCurrentTag().getAttributes() != null)
            res = getCurrentTag().getAttributes().size();
        return res;
    }

    public void copyAttributesNames() {
        XmlTagInfo tag = getCurrentTag();
        java.util.List<AttrInfo> attrs = tag.getAttributes();
        if (attrs != null) {
            StringBuilder sb = new StringBuilder();
            for (AttrInfo attrInfo : attrs) {
                sb.append(attrInfo.getName()).append("\n");
            }
            copyToClip(sb.toString());
        }
    }

    public void copyTagAction() {
        XmlTagInfo tag = getCurrentTag();
        copyToClip(tag.getTagName());
    }

    public void copyAllAsXmlAction() {
        XmlTagInfo tag = getCurrentTag();
        copyToClip(tag.getText());
    }

    public void copyAsKeyValueAction() {
        XmlTagInfo tag = getCurrentTag();
        StringBuilder sb = new StringBuilder();
        sb.append(tag.getTagName());
        if (Utils.isNotEmpty(tag.getValue())) sb.append("=").append(tag.getValue());
        sb.append("\n");
        java.util.List<AttrInfo> attrs = tag.getAttributes();
        if (attrs != null) {
            for (AttrInfo attrInfo : attrs) {
                sb.append(attrInfo.getName()).append("=").append(attrInfo.getValue()).append("\n");
            }
        }
        copyToClip(sb.toString());
    }

    public void copyAttributesValues() {
        XmlTagInfo tag = getCurrentTag();
        java.util.List<AttrInfo> attrs = tag.getAttributes();
        StringBuilder sb = new StringBuilder();
        for (AttrInfo attrInfo : attrs) {
            sb.append(attrInfo.getValue()).append("\n");
        }
        copyToClip(sb.toString());
    }

    public List<JFrame> getFramesList() {
        return framesList;
    }

    public void closeAll() {
        for (JFrame frame : framesList) {
            if (frame != this.ownFrame) frame.dispose();
        }
        ownFrame.dispatchEvent(new WindowEvent(ownFrame, WindowEvent.WINDOW_CLOSING));
    }

    public void closeThis() {
        ownFrame.dispatchEvent(new WindowEvent(ownFrame, WindowEvent.WINDOW_CLOSING));
    }

    public JFrame getOwnFrame() {
        return ownFrame;
    }

    public void focusWindow(JFrame frame) {
        java.awt.EventQueue.invokeLater(() -> {
            frame.toFront();
            frame.repaint();
        });
    }

    public void findIn() {
        JPanel searchPanel = guiView.getSearchPanel();
        boolean isPanelVisible = searchPanel.isVisible();
        if (!isPanelVisible) {
            showSearchPanel();
        } else {
            JTextField textField = guiView.getSearchTextField();
            if (Utils.isNotEmpty(textField.getText())) {
                textField.setText("");
                textField.requestFocus();
                initSearch();
            } else {
                hideSearchPanel();
            }
        }
    }

    private void showSearchPanel() {
        initSearch();
        (guiView.getSearchPanel()).setVisible(true);
        guiView.getSearchTextField().requestFocus();
    }

    private void hideSearchPanel() {
        (guiView.getSearchPanel()).setVisible(false);
        initSearch();
    }

    private void initSearch() {
        searchProcessor = null;
        setSearchResultText(0, 0);
        setFwdBwdControlsEnabled(false);
    }

    private void setSearchResultText(int current, int total) {
        guiView.getFoundLabel().setText(String.format(GuiView.FOUND_RESULT_LABEL, (total > 0) ? current + 1 : 0, total));
    }

    private void btSearchClicked() {
        initSearch();
        String searchString = guiView.getSearchTextField().getText();
        SearchType searchType = new SearchType(guiView.getSearchInTagNames().isSelected(),
                guiView.getSearchInTagValues().isSelected(),
                guiView.getSearchInAttrNames().isSelected(),
                guiView.getSearchInAttrValues().isSelected());
        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) guiView.getTree().getModel().getRoot();
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) guiView.getTree().getLastSelectedPathComponent();
        searchProcessor = new SearchProcessor(searchString, rootNode, selectedNode, searchType);
        searchWithProgress();
    }

    private void searchWithProgress() {
        guiView.getSearchButton().setEnabled(false);
        guiView.getSearchProgressBar().setVisible(true);
        SearchTask task = new SearchTask(this, searchProcessor);
        task.addPropertyChangeListener(progressChange());
        task.execute();
    }

    public void searchCompleted() {
        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) guiView.getTree().getModel().getRoot();
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) guiView.getTree().getLastSelectedPathComponent();
        if (searchProcessor.isNotEmptyResult()) {
            if (selectedNode != null && selectedNode != rootNode) {
                searchProcessor.setCurrentSearchResultValue();
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

    private void setFwdBwdControlsEnabled(boolean value) {
        appActions.getFindForwardAction().setEnabled(value);
        appActions.getFindBackwardAction().setEnabled(value);
        guiView.getPreviousButton().setEnabled(value);
        guiView.getNextButton().setEnabled(value);
    }

    public PropertyChangeListener progressChange() {
        return evt -> {
            if ("progress".equals(evt.getPropertyName())) {
                int progress = (Integer) evt.getNewValue();
                guiView.getSearchProgressBar().setIndeterminate(false);
                guiView.getSearchProgressBar().setValue(progress);
                setSearchResultText(searchProcessor.getCurrentResultPosition(), searchProcessor.getTotalResults());
            }
        };
    }

    public void findForward() {
        if (searchProcessor != null && searchProcessor.isNotEmptyResult()) {
            showResultInTree(searchProcessor.getNextSearchResult());
            setSearchResultText(searchProcessor.getCurrentResultPosition(), searchProcessor.getTotalResults());
            setSelectedXmlText();
        }
    }

    public void findBackward() {
        if (searchProcessor != null && searchProcessor.isNotEmptyResult()) {
            showResultInTree(searchProcessor.getPrevSearchResult());
            setSearchResultText(searchProcessor.getCurrentResultPosition(), searchProcessor. getTotalResults());
            setSelectedXmlText();
        }
    }

    private void setSelectedXmlText() {
        JTextArea text = guiView.getRawXmlText();
        text.requestFocus();
        int start = (int) searchProcessor.getCurrentSearchResultValue().getResultStart();
        if (start > -1) {
            text.setSelectionStart(start);
            text.setSelectionEnd(start + searchProcessor.getCurrentSearchResultValue().getResultLength());
        } else {
            text.setSelectionStart(0);
            text.setSelectionEnd(text.getText().length());
        }
    }

    private void showResultInTree(SearchResult results) {
        TreePath path = new TreePath(results.getTreeNode().getPath());
        JTree tree = guiView.getTree();
        tree.setSelectionPath(path);
        tree.scrollPathToVisible(path);
        tree.expandPath(path);
    }

    public JProgressBar getProgressBar() {
        return guiView.getSearchProgressBar();
    }
}
