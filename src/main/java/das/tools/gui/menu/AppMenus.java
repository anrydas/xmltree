package das.tools.gui.menu;

import das.tools.gui.XmlForm;
import das.tools.gui.menu.action.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class AppMenus extends JPopupMenu {
    private static final UIManager.LookAndFeelInfo[] LOOK_AND_FEEL_INFOS;
    private final XmlForm form;
    private final AppActions actions;

    static {
        LOOK_AND_FEEL_INFOS = UIManager.getInstalledLookAndFeels();
    }


    public AppMenus(XmlForm form, AppActions actions) {
        super();
        this.form = form;
        this.actions = actions;
    }

    private void addMenu(AbstractAction action) {
        JMenuItem item = new JMenuItem(action);
        this.add(item);
    }

    public AppMenus getAttrMenu() {
        AppMenus menu = this;
        menu.removeAll();
        menu.addMenu(actions.getCopyTagNameAction());
        if (form.getCurrentTagAttrCount() > 0) {
            menu.addMenu(actions.getCopyAttrNamesAction());
            menu.addMenu(actions.getCopyAttrValuesAction());
        }
        menu.addMenu(actions.getCopyKeyValueAction());
        menu.addMenu(actions.getCopyXmlAction());
        return menu;
    }

    public void getWindowsMenu(JMenu parent) {
        int windowsCount = form.getFramesList().size();
        (parent.add(actions.getCloseThisAction()))
                .setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.CTRL_MASK));
        if (windowsCount > 1) {
            (parent.add(actions.getCloseAllAction()))
                    .setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_MASK));
            parent.addSeparator();
            int n = 1;
            for (JFrame frame : form.getFramesList()) {
                if (frame != form.getOwnFrame()) {
                    JMenuItem mi = parent.add(frame.getTitle());
                    mi.addActionListener(actionEvent -> form.focusWindow(frame));
                    if (n <= 9) {
                        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0 + n -1, InputEvent.CTRL_MASK));
                        n++;
                    }
                }
            }
        }
    }

    public void getThemeMenu(JMenu parent) {
        LookAndFeel current = UIManager.getLookAndFeel();
        ButtonGroup bg = new ButtonGroup();
        for (UIManager.LookAndFeelInfo info : LOOK_AND_FEEL_INFOS) {
            JRadioButtonMenuItem mi = new JRadioButtonMenuItem(info.getName());
            mi.setSelected(info.getName().equals(current.getName()));
            mi.addActionListener(actionEvent -> {
                try {
                    UIManager.setLookAndFeel(info.getClassName());
                    for (JFrame frame : form.getFramesList())
                        if (frame != form.getOwnFrame()) {
                            SwingUtilities.updateComponentTreeUI(frame.getContentPane());
                        }
                    SwingUtilities.updateComponentTreeUI(form.getOwnFrame().getContentPane());
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                         UnsupportedLookAndFeelException e) {
                    throw new RuntimeException(e);
                }
            });
            bg.add(mi);
            parent.add(mi);
        }
    }

    public void getFileMenu(JMenu parent) {
        JMenuItem openItem = parent.add(actions.getOpenFileAction());
        parent.addSeparator();
        JMenuItem exitItem = parent.add(actions.getExitAction());

        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_MASK));
    }
}
