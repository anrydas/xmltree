package das.tools.gui;

import das.tools.gui.entity.XmlTagInfo;
import org.w3c.dom.Node;

import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Enumeration;

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

    public static String escapeHtml(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder(s.length());
        for (char c : s.toCharArray()) {
            switch (c) {
                case '<': sb.append("&lt;"); break;
                case '>': sb.append("&gt;"); break;
                case '&': sb.append("&amp;"); break;
                case '"': sb.append("&quot;"); break;
                default: sb.append(c);
            }
        }
        return sb.toString();
    }

    /** Nodes in the subtree including {@code node} itself. */
    public static int getNodesCount(DefaultMutableTreeNode node) {
        int count = 0;
        for (Enumeration<?> e = node.preorderEnumeration(); e.hasMoreElements(); e.nextElement()) {
            count++;
        }
        return count;
    }

    /** Elements in the subtree including {@code rootNode} itself; iterative, so deep documents don't overflow the stack. */
    public static int getNodesCount(Node rootNode) {
        int count = 0;
        Deque<Node> stack = new ArrayDeque<>();
        stack.push(rootNode);
        while (!stack.isEmpty()) {
            Node node = stack.pop();
            count++;
            for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    stack.push(child);
                }
            }
        }
        return count;
    }

    public static int getTagAttrCount(XmlTagInfo tag) {
        int res = 0;
        if (tag != null && tag.getAttributes() != null)
            res = tag.getAttributes().size();
        return res;
    }

    public static void copyToClip(String value) {
        Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
        clip.setContents(new StringSelection(value), null);
    }
}
