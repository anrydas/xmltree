package das.tools.gui.menu;

import das.tools.gui.XmlForm;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class PopupClickListener extends MouseAdapter {
    private final XmlForm form;
    private final AppMenus menus;

    public PopupClickListener(XmlForm form, AppMenus menus) {
        super();
        this.form = form;
        this.menus = menus;
    }

    public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger())
            doPop(e);
    }

    public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger())
            doPop(e);
    }

    private void doPop(MouseEvent e) {
        AppMenus menu = null;
        if(e.getComponent() instanceof JTextPane && !form.isCurrentlyRootSelected()) {
            menu = menus.getAttrMenu();
        }
        if (menu != null)
            menu.show(e.getComponent(), e.getX(), e.getY()); // or e.getXOnScreen() and e.getYOnScreen() if menu have wrong position
    }
}
