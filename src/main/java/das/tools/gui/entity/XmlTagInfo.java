package das.tools.gui.entity;

import org.w3c.dom.Node;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.util.List;

public class XmlTagInfo {
    static final String NO_FILE_TEXT = "There is no file selected.\nTo select file press 'Open' button\nor\nlaunch the Application with file name as 1st parameter.";
    // DOM is not thread-safe even for reads, and the text is requested from both the EDT and the search worker
    private static final Object DOM_LOCK = new Object();
    private static TransformerFactory transformerFactory;

    private final String tagName;
    private final List<AttrInfo> attributes;
    private final String value;
    private final Node node;
    private String text;

    public XmlTagInfo(Node node){
        this.tagName = node.getNodeName();
        if (!tagName.equals(XmlFile.TAG_NO_FILE)) {
            this.node = node;
            this.attributes = XmlFile.getAttributesList(node);
            Node firstChild = node.getFirstChild();
            this.value = firstChild != null && firstChild.getNodeType() == Node.TEXT_NODE
                    ? firstChild.getTextContent().trim() : "";
        } else {
            this.node = null;
            this.attributes = null;
            this.value = "";
            this.text = NO_FILE_TEXT;
        }
    }

    public String getTagName() {
        return tagName;
    }

    public String getValue() {
        return value;
    }

    public List<AttrInfo> getAttributes() {
        return attributes;
    }

    /**
     * Formatted XML of the tag with its whole subtree. Serialized on first request and cached:
     * computing it eagerly for every node costs memory proportional to file size × depth.
     */
    public String getText() {
        synchronized (DOM_LOCK) {
            if (text == null) {
                text = getSubText(node);
            }
            return text;
        }
    }

    @Override
    public String toString() {
        return this.tagName;
    }

    private static String getSubText(Node node) {
        try {
            if (transformerFactory == null) {
                transformerFactory = TransformerFactory.newInstance();
            }
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            StringWriter out = new StringWriter();
            transformer.transform(new DOMSource(node), new StreamResult(out));
            return out.toString();
        } catch (TransformerException e) {
            return "Can't format the tag: " + e.getMessageAndLocation();
        } catch (StackOverflowError e) {
            // the serializer is recursive; extremely deep documents can't be formatted, but the tree still works
            return "Can't format the tag: the XML is nested too deeply";
        }
    }
}
