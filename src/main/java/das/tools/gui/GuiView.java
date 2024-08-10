package das.tools.gui;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import java.awt.*;

public class GuiView {
    private final XmlForm form;
    private JPanel rootPanel;
    private JToolBar toolbar;
    private JButton btOpen;
    private JTextPane textAttr;
    private JTextArea rawXmlText;
    private JTree tree;
    private JMenuBar menuBar;
    private JMenu fileMenu;
    private JMenu themeMenu;
    private JMenu windowMenu;
    private JScrollPane treeScroll;
    /*public static GuiView getInstance() {
        if (instance == null) {
            instance = new GuiView();
        }
        return instance;
    }*/

    public GuiView(XmlForm form) {
        this.form = form;
        initRoot();
    }

    public void initRoot() {
        initComponents();

        rootPanel = new JPanel();
        rootPanel.setLayout(new BorderLayout());

        JScrollPane textScroll = new JScrollPane(textAttr);
        JScrollPane rawScroll = new JScrollPane(rawXmlText);

        JSplitPane spInfo = new JSplitPane(JSplitPane.VERTICAL_SPLIT, textScroll, rawScroll);
        spInfo.setDividerLocation(100);

        this.treeScroll = new JScrollPane(tree);

        JSplitPane spMain = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScroll, spInfo);
        spMain.setDividerLocation(150);

        rootPanel.add(toolbar, BorderLayout.NORTH);
        rootPanel.add(spMain, BorderLayout.CENTER);
    }

    private void initComponents() {
        textAttr = initTextAttrPane();
        rawXmlText = initRawXmlPane();
        tree = initTree();
        toolbar = initToolbar();
    }

    private JTree initTree() {
        JTree tree = new JTree();
        tree.setRootVisible(true);
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        Object root = model.getRoot();
        while(!model.isLeaf(root)) {
            model.removeNodeFromParent((MutableTreeNode) model.getChild(root,0));
        }
        return tree;
    }

    private JTextPane initTextAttrPane() {
        JTextPane textAttr = new JTextPane();
        textAttr.setContentType("text/html");
        textAttr.setEditable(false);
        return textAttr;
    }

    private JTextArea initRawXmlPane() {
        JTextArea rawXmlText = new JTextArea();
        rawXmlText.setEditable(false);
        return rawXmlText;
    }

    public JToolBar initToolbar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setLayout(new FlowLayout(FlowLayout.LEFT));
        toolbar.add(initOpenButton());
        return toolbar;
    }

    public JButton initOpenButton() {
        btOpen = new JButton("Open");
        return btOpen;
    }

    private JMenuBar initMenus() {
        menuBar = new JMenuBar();
        fileMenu = new JMenu("File");
        themeMenu = new JMenu("Theme");
        windowMenu = new JMenu("Windows");
        menuBar.add(fileMenu);
        menuBar.add(themeMenu);
        menuBar.add(windowMenu);
        return menuBar;
    }

    public JMenuBar getMenuBar() {
        return initMenus();
    }

    public JPanel getRootPanel() {
        return rootPanel;
    }

    public JTextPane getTextAttr() {
        return textAttr;
    }

    public JTextArea getRawXmlText() {
        return rawXmlText;
    }

    public JTree getTree() {
        return tree;
    }

    public JMenu getFileMenu() {
        return fileMenu;
    }

    public JMenu getThemeMenu() {
        return themeMenu;
    }

    public JMenu getWindowMenu() {
        return windowMenu;
    }

    public JButton getBtOpen() {
        return btOpen;
    }
}
