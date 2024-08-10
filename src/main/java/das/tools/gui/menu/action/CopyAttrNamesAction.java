package das.tools.gui.menu.action;

import das.tools.gui.XmlForm;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class CopyAttrNamesAction extends AbstractAction {
    private final XmlForm form;

    public CopyAttrNamesAction(XmlForm form, String caption, Icon icon/*, String desc, Integer mnemonic*/) {
        super(caption, icon);
        this.form = form;
        /*putValue(SHORT_DESCRIPTION, desc);
        putValue(MNEMONIC_KEY, mnemonic);*/
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        form.copyAttributesNames();
    }
}
