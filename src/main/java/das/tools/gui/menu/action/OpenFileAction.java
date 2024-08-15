package das.tools.gui.menu.action;

import das.tools.gui.XmlForm;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class OpenFileAction extends AbstractAction {
    private final XmlForm form;

    public OpenFileAction(XmlForm form, String caption, Icon icon) {
        super(caption, icon);
        this.form = form;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        form.openFile();
    }
}
