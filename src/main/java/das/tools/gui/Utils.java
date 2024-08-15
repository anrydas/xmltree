package das.tools.gui;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.File;

public class Utils {
    public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');
        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }

    public static boolean isNotEmpty(String s) {
        return s != null && !"".equals(s);
    }

    public static boolean isEven(long num) {
        return (num & 1) == 0;
    }

    public static int getNodesCount(DefaultMutableTreeNode node) {
        int count = 1;
        for (int i = 0; i < node.getChildCount(); i++) {
            count = count + getNodesCount((DefaultMutableTreeNode) node.getChildAt(i));
        }
        return count;
    }

    public static int getNodesCount(Node rootNode) {
        int count = 1;
        NodeList childNodes = rootNode.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            if (item.getNodeType() == Node.ELEMENT_NODE) {
                count = count + getNodesCount(item);
            }
        }
        return count;
    }
}
