package das.tools.gui;

import das.tools.gui.entity.XmlFile;
import das.tools.gui.entity.XmlFileException;
import das.tools.gui.entity.XmlTagInfo;
import das.tools.gui.form.LoadTreeDataTask;
import das.tools.gui.format.TagFormatter;
import das.tools.gui.menu.AppMenus;
import das.tools.gui.menu.PopupClickListener;
import das.tools.gui.menu.action.AppActions;
import das.tools.gui.search.SearchController;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URL;

/** One application window showing one XML file. Must be created and used on the EDT. */
public class XmlForm implements TreeSelectionListener {
    private final AppActions appActions;
    private final GuiView guiView;
    private final AppMenus appMenus;
    private final SearchController searchController;
    private final JFrame ownFrame;
    private String fileName;
    private XmlFile xmlFile = null;

    /**
     * @param fileName      file to show; may be empty
     * @param isStartWindow the first window of the application: asks for a file when {@code fileName} is empty
     */
    public XmlForm(String fileName, boolean isStartWindow) {
        this.fileName = fileName;
        appActions = new AppActions(this);
        appMenus = new AppMenus(this, appActions);
        guiView = new GuiView();
        guiView.getBtOpen().addActionListener(appActions.getOpenFileAction());
        guiView.getTextAttr().addMouseListener(new PopupClickListener(this, appMenus));
        searchController = new SearchController(guiView, appActions);
        if (isStartWindow) {
            selectStartFile();
        }
        ownFrame = createGUI();
        loadDataIntoTree();
    }

    private void selectStartFile() {
        if (Utils.isNotEmpty(fileName)) {
            XmlFileChooser.setCurrentDirectory(new File(fileName).getAbsoluteFile());
        } else {
            fileName = XmlFileChooser.select(null);
        }
    }

    public void openFile() {
        String selectedFile = XmlFileChooser.select(ownFrame);
        if (Utils.isNotEmpty(selectedFile)) {
            new XmlForm(selectedFile, false);
        }
    }

    public AppMenus getAttrMenu() {
        return appMenus.getAttrMenu();
    }

    private JFrame createGUI() {
        JFrame frame = new JFrame();
        frame.setMinimumSize(new Dimension(800, 600));
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setApplicationIcon(frame);
        frame.setContentPane(guiView.getRootPanel());
        frame.setJMenuBar(guiView.getMenuBar());
        appMenus.getFileMenu(guiView.getFileMenu());
        appMenus.getSearchMenu(guiView.getSearchMenu());
        // Rebuilt whenever the set of windows changes, so its hotkeys (Ctrl+F4, Ctrl+0..8) work without opening the menu
        JMenu windowsMenu = guiView.getWindowMenu();
        Runnable rebuildWindowsMenu = () -> {
            windowsMenu.removeAll();
            appMenus.getWindowsMenu(windowsMenu, frame);
        };
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent windowEvent) {
                AppWindows.removeChangeListener(rebuildWindowsMenu);
                AppWindows.unregister(frame);
            }
        });
        AppWindows.addChangeListener(rebuildWindowsMenu);
        AppWindows.register(frame);
        JMenu themeMenu = guiView.getThemeMenu();
        themeMenu.addMenuListener(rebuildOnSelect(themeMenu, () -> appMenus.getThemeMenu(themeMenu)));
        addEscapeKeyAction(frame);
        frame.pack();
        frame.setVisible(true);
        return frame;
    }

    private void addEscapeKeyAction(JFrame frame) {
        KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        JComponent pn = frame.getRootPane();
        pn.getActionMap().put("Escape", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                searchController.hidePanel();
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

    /** Menu whose items depend on the current state (opened windows, theme) is rebuilt every time it opens. */
    private static MenuListener rebuildOnSelect(JMenu menu, Runnable fill) {
        return new MenuListener() {
            @Override
            public void menuSelected(MenuEvent menuEvent) {
                menu.removeAll();
                fill.run();
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
        xmlFile = openXmlFile(fileName);
        ownFrame.setTitle(String.format("Xml Tree: %s", xmlFile.getName()));
        AppWindows.fireChanged();
        JTree tree = guiView.getTree();
        tree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode(xmlFile.getName())));
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener(this);
        tree.setShowsRootHandles(true);
        appActions.getSearchAction().setEnabled(false);
        ProgressMonitor loadingFileMonitor = new ProgressMonitor(ownFrame, "Loading XML structure...", "", 0, xmlFile.getAllNodesCount());
        new LoadTreeDataTask(this, xmlFile, loadingFileMonitor).execute();
    }

    private XmlFile openXmlFile(String fileName) {
        try {
            return new XmlFile(fileName);
        } catch (XmlFileException e) {
            JOptionPane.showMessageDialog(ownFrame, e.getMessage(),
                    String.format("Error open file '%s'", fileName),
                    JOptionPane.ERROR_MESSAGE);
            return XmlFile.noFile(fileName);
        }
    }

    public void treeLoaded(DefaultMutableTreeNode top) {
        guiView.getTree().setModel(new DefaultTreeModel(top));
        appActions.getSearchAction().setEnabled(true);
    }

    public void treeLoadFailed(Throwable cause) {
        JOptionPane.showMessageDialog(ownFrame, String.valueOf(cause),
                String.format("Error loading file '%s'", fileName),
                JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode treeNode = getSelectedNode();
        if (treeNode == null) {
            return;
        }
        if (treeNode != guiView.getTree().getModel().getRoot()) {
            XmlTagInfo tag = (XmlTagInfo) treeNode.getUserObject();
            guiView.getTextAttr().setText(TagFormatter.toHtml(tag));
            guiView.getRawXmlText().setText(tag.getText());
        } else {
            guiView.getTextAttr().setText(TagFormatter.fileToHtml(xmlFile.getFileName(), xmlFile.getFullFileName()));
            guiView.getRawXmlText().setText("");
        }
    }

    private DefaultMutableTreeNode getSelectedNode() {
        return (DefaultMutableTreeNode) guiView.getTree().getLastSelectedPathComponent();
    }

    public boolean isCurrentlyRootSelected() {
        DefaultMutableTreeNode treeNode = getSelectedNode();
        return treeNode == null || treeNode.isRoot();
    }

    private XmlTagInfo getCurrentTag() {
        return (XmlTagInfo) getSelectedNode().getUserObject();
    }

    public int getCurrentTagAttrCount() {
        return Utils.getTagAttrCount(getCurrentTag());
    }

    public void copyAttributesNames() {
        Utils.copyToClip(TagFormatter.attributeNames(getCurrentTag()));
    }

    public void copyAttributesValues() {
        Utils.copyToClip(TagFormatter.attributeValues(getCurrentTag()));
    }

    public void copyTagAction() {
        Utils.copyToClip(getCurrentTag().getTagName());
    }

    public void copyAllAsXmlAction() {
        Utils.copyToClip(getCurrentTag().getText());
    }

    public void copyAsKeyValueAction() {
        Utils.copyToClip(TagFormatter.keyValue(getCurrentTag()));
    }

    public void closeAll() {
        for (JFrame frame : AppWindows.getFrames()) {
            if (frame != this.ownFrame) frame.dispose();
        }
        closeThis();
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
        searchController.toggle();
    }

    public void findForward() {
        searchController.findForward();
    }

    public void findBackward() {
        searchController.findBackward();
    }
}
