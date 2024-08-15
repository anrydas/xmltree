package das.tools.gui.form;

import das.tools.gui.XmlForm;
import das.tools.gui.entity.XmlFile;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

public class LoadTreeDataTask extends SwingWorker<Void, Void> {
    private final XmlForm form;
    private final XmlFile xmlFile;
    private final DefaultMutableTreeNode root;

    public LoadTreeDataTask(XmlForm form, XmlFile xmlFile, DefaultMutableTreeNode root) {
        this.form = form;
        this.xmlFile = xmlFile;
        this.root = root;
    }

    @Override
    protected Void doInBackground() throws Exception {
        form.createNodes(root, xmlFile.getRootNode());
        form.getAppActions().getSearchAction().setEnabled(false);
        return null;
    }

    @Override
    protected void done() {
        super.done();
        form.getAppActions().getSearchAction().setEnabled(true);
    }
}
