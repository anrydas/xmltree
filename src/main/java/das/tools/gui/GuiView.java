package das.tools.gui;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class GuiView {
    public static final String FOUND_RESULT_LABEL = "Position: %d of %d";
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
    private JPanel searchPanel;
    private JTextField searchTextField;
    private JButton searchButton;
    private JCheckBox searchInAttrNames;
    private JCheckBox searchInTagNames;
    private JCheckBox searchInTagValues;
    private JCheckBox searchInAttrValues;
    private JLabel foundLabel;
    private JButton previousButton;
    private JButton nextButton;
    private JPanel resultsPanel;
    private JMenu searchMenu;
    private JProgressBar searchProgressBar;

    public GuiView() {
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
        rootPanel.add(searchPanel, BorderLayout.SOUTH);
    }

    private void initComponents() {
        textAttr = initTextAttrPane();
        rawXmlText = initRawXmlPane();
        tree = initTree();
        toolbar = initToolbar();
        searchPanel = initSearchPanel();
    }

    private JTree initTree() {
        JTree tree = new JTree();
        tree.setRootVisible(true);
        tree.setScrollsOnExpand(true);
        tree.setExpandsSelectedPaths(true);
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
        searchMenu = new JMenu("Search");
        windowMenu = new JMenu("Windows");
        themeMenu = new JMenu("Theme");
        menuBar.add(fileMenu);
        menuBar.add(searchMenu);
        menuBar.add(windowMenu);
        menuBar.add(themeMenu);
        return menuBar;
    }

    private JPanel initSearchPanel() {
        searchPanel = new JPanel();
        searchPanel.add(getConditionsPanel());
        searchPanel.add(getResultsPanel());
        searchPanel.setVisible(false);
        return searchPanel;
    }

    private JPanel getConditionsPanel() {
        JPanel pnConditions = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        pnConditions.setLayout(layout);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JPanel pnText = new JPanel();
        JLabel label = new JLabel("Search for:");
        searchTextField = new JTextField("", 20);
        pnText.add(label);
        pnText.add(searchTextField);
        gbc.gridy = 0; gbc.gridx = 0;
        pnConditions.add(pnText, gbc);

        searchButton = new JButton("Search");
        gbc.gridy = 0; gbc.gridx = 2;
        searchButton.setEnabled(false);
        pnConditions.add(searchButton, gbc);

        JPanel pnTags = new JPanel();
        searchInTagNames = new JCheckBox("Tag names");
        searchInTagValues = new JCheckBox("Tag values");
        pnTags.add(searchInTagNames);
        pnTags.add(searchInTagValues);
        gbc.gridy = 1; gbc.gridx = 0;
        pnConditions.add(pnTags, gbc);

        JPanel pnAttrs = new JPanel();
        searchInAttrNames = new JCheckBox("Attr names");
        searchInAttrValues = new JCheckBox("Attr values");
        pnAttrs.add(searchInAttrNames);
        pnAttrs.add(searchInAttrValues);
        gbc.gridy = 2; gbc.gridx = 0;
        pnConditions.add(pnAttrs, gbc);

        JPanel pnAll = new JPanel();
        JCheckBox cbSearchEverywhere = new JCheckBox("Everywhere");
        pnAll.add(cbSearchEverywhere);
        gbc.gridy = 3; gbc.gridx = 0;
        gbc.gridwidth = 2;
        pnConditions.add(pnAll, gbc);
        cbSearchEverywhere.addActionListener((event) -> {
            searchInTagNames.setSelected(cbSearchEverywhere.isSelected());
            searchInTagValues.setSelected(cbSearchEverywhere.isSelected());
            searchInAttrNames.setSelected(cbSearchEverywhere.isSelected());
            searchInAttrValues.setSelected(cbSearchEverywhere.isSelected());
            setSearchButtonEnabled();
        });

        searchInTagNames.addActionListener((e) -> checkBoxClicked(cbSearchEverywhere));
        searchInTagValues.addActionListener((e) -> checkBoxClicked(cbSearchEverywhere));
        searchInAttrNames.addActionListener((e) -> checkBoxClicked(cbSearchEverywhere));
        searchInAttrValues.addActionListener((e) -> checkBoxClicked(cbSearchEverywhere));

        cbSearchEverywhere.doClick();

        searchTextField.addKeyListener(getKeyPressedInSearchFiledListener());

        return pnConditions;
    }

    private KeyListener getKeyPressedInSearchFiledListener() {
        return new KeyListener() {
            @Override
            public void keyTyped(KeyEvent keyEvent) {
                setSearchButtonEnabled();
            }

            @Override
            public void keyPressed(KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() != KeyEvent.VK_ENTER) {
                    setSearchButtonEnabled();
                }
            }

            @Override
            public void keyReleased(KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (searchButton.isEnabled()) {
                        searchButton.doClick();
                    }
                } else {
                    setSearchButtonEnabled();
                }
            }
        };
    }

    private void setSearchButtonEnabled() {
        searchButton.setEnabled(
            searchTextField.getText().length() > 0 && (
                    searchInTagNames.isSelected() ||
                    searchInTagValues.isSelected() ||
                    searchInAttrNames.isSelected() ||
                    searchInAttrValues.isSelected()
            ));
    }

    private void checkBoxClicked(JCheckBox cbEverywhere) {
        cbEverywhere.setSelected(searchInTagNames.isSelected() &&
                searchInTagValues.isSelected() &&
                searchInAttrNames.isSelected() &&
                searchInAttrValues.isSelected());
        setSearchButtonEnabled();
    }

    private JPanel getResultsPanel() {
        resultsPanel = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        resultsPanel.setLayout(layout);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JPanel pnFound = new JPanel();
        foundLabel = new JLabel(String.format(FOUND_RESULT_LABEL, 0, 0));
        pnFound.add(foundLabel);
        gbc.gridy = 0; gbc.gridx = 0;
        resultsPanel.add(pnFound, gbc);
        searchProgressBar = new JProgressBar();
        searchProgressBar.setStringPainted(true);
        //searchProgressBar.setString("Search in progress...");
        gbc.gridy = 1; gbc.gridx = 0;
        resultsPanel.add(searchProgressBar, gbc);
        searchProgressBar.setVisible(false);

        JPanel pnGo = new JPanel();
        previousButton = new JButton("<<");
        nextButton = new JButton(">>");
        pnGo.add(previousButton);
        pnGo.add(nextButton);
        gbc.gridy = 2; gbc.gridx = 0;
        resultsPanel.add(pnGo, gbc);

        return resultsPanel;
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

    public JPanel getSearchPanel() {
        return searchPanel;
    }

    public JTextField getSearchTextField() {
        return searchTextField;
    }

    public JButton getSearchButton() {
        return searchButton;
    }

    public JCheckBox getSearchInAttrNames() {
        return searchInAttrNames;
    }

    public JCheckBox getSearchInTagNames() {
        return searchInTagNames;
    }

    public JCheckBox getSearchInTagValues() {
        return searchInTagValues;
    }

    public JCheckBox getSearchInAttrValues() {
        return searchInAttrValues;
    }

    public JLabel getFoundLabel() {
        return foundLabel;
    }

    public JButton getPreviousButton() {
        return previousButton;
    }

    public JButton getNextButton() {
        return nextButton;
    }

    public JMenu getSearchMenu() {
        return searchMenu;
    }

    public JProgressBar getSearchProgressBar() {
        return searchProgressBar;
    }
}
