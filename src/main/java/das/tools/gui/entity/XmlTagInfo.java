package das.tools.gui.entity;

import org.w3c.dom.Node;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class XmlTagInfo {
    private String tagName = "";
    private List<AttrInfo> attributes = null;
    private String text = "";
    private String value = "";

    public XmlTagInfo(Node node){
        this.tagName = node.getNodeName();
        if (!tagName.equals(XmlFile.TAG_NO_FILE)) {
            this.attributes = XmlFile.getAttributesList(node);
            this.text = getSubText(node);
            Node firstChild = node.getFirstChild();
            if (firstChild != null && firstChild.getNodeType() == Node.TEXT_NODE) {
                this.value = firstChild.getTextContent().trim();
            }
        } else {
            this.text = "There is no file selected.\nTo select file press 'Open' button\nor\nlaunch the Application with file name as 1st parameter.";
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

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return this.tagName;
    }

    private String getSubText(Node node) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            Source source = new DOMSource(node);
            Result target = new StreamResult(out);
            transformer.transform(source, target);

            return out.toString();
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }
}
