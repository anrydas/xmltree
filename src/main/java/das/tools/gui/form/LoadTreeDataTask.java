package das.tools.gui.form;

import das.tools.gui.XmlForm;
import das.tools.gui.entity.XmlFile;
import das.tools.gui.entity.XmlTagInfo;
import org.w3c.dom.Node;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Builds the tree off the EDT into a detached node; the form gets it in {@link #done()} on the EDT,
 * so the visible tree model is never mutated from a background thread.
 */
public class LoadTreeDataTask extends SwingWorker<DefaultMutableTreeNode, Integer> {
    private final XmlForm form;
    private final XmlFile xmlFile;
    private final ProgressMonitor progressMonitor;

    public LoadTreeDataTask(XmlForm form, XmlFile xmlFile, ProgressMonitor progressMonitor) {
        this.form = form;
        this.xmlFile = xmlFile;
        this.progressMonitor = progressMonitor;
    }

    @Override
    protected DefaultMutableTreeNode doInBackground() {
        DefaultMutableTreeNode top = new DefaultMutableTreeNode(xmlFile.getName());
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(new XmlTagInfo(xmlFile.getRootNode()));
        top.add(root);
        createNodes(root, xmlFile.getRootNode());
        return top;
    }

    /** Iterative, so deep documents don't overflow the stack; children are added in document order. */
    private void createNodes(DefaultMutableTreeNode rootTreeNode, Node rootElement) {
        Deque<DefaultMutableTreeNode> treeNodes = new ArrayDeque<>();
        Deque<Node> elements = new ArrayDeque<>();
        treeNodes.push(rootTreeNode);
        elements.push(rootElement);
        int loadedNodes = 0;
        while (!elements.isEmpty()) {
            DefaultMutableTreeNode parent = treeNodes.pop();
            for (Node n = elements.pop().getFirstChild(); n != null; n = n.getNextSibling()) {
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(new XmlTagInfo(n));
                    parent.add(treeNode);
                    treeNodes.push(treeNode);
                    elements.push(n);
                    publish(++loadedNodes);
                }
            }
        }
    }

    @Override
    protected void process(List<Integer> chunks) {
        progressMonitor.setProgress(chunks.get(chunks.size() - 1));
    }

    @Override
    protected void done() {
        progressMonitor.close();
        try {
            form.treeLoaded(get());
        } catch (InterruptedException | ExecutionException e) {
            Throwable cause = e instanceof ExecutionException ? e.getCause() : e;
            cause.printStackTrace();
            form.treeLoadFailed(cause);
        }
    }
}
