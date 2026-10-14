package das.tools.gui.entity;

import das.tools.gui.Utils;
import org.w3c.dom.*;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by das on 14.03.2015.
 */
public class XmlFile {
    public static final String TAG_NO_FILE = "no_file";
    // Collapses line breaks between tags inside text (e.g. escaped XML) so it is not shown as a ragged block
    private static final Pattern TEXT_LINEARIZATION_PATTERN = Pattern.compile(">\\t*[\\n\\r]+\\s*<");
    private static final String TEXT_LINEARIZATION_REPLACEMENT = "><";

    // Default handler prints "[Fatal Error]" to stderr; errors are reported via exceptions instead
    private static final ErrorHandler SILENT_ERROR_HANDLER = new ErrorHandler() {
        @Override
        public void warning(SAXParseException e) {
        }

        @Override
        public void error(SAXParseException e) throws SAXException {
            throw e;
        }

        @Override
        public void fatalError(SAXParseException e) throws SAXException {
            throw e;
        }
    };

    private final String fileName;
    private final File file;
    private final Node root;
    private final int allNodesCount;

    /**
     * Parses the file. A missing file (or empty name) gives a placeholder root tagged {@link #TAG_NO_FILE}.
     *
     * @throws XmlFileException if the file exists but can't be read or isn't well-formed XML
     */
    public XmlFile(String fileName) throws XmlFileException {
        this.fileName = fileName;
        this.file = new File(fileName);
        this.root = file.isFile() ? parse(file) : createNoFileRoot();
        this.allNodesCount = Utils.getNodesCount(root) - 1;
    }

    private XmlFile(String fileName, Node root) {
        this.fileName = fileName;
        this.file = new File(fileName);
        this.root = root;
        this.allNodesCount = 0;
    }

    /** Placeholder used when there is nothing (valid) to show. */
    public static XmlFile noFile(String fileName) {
        return new XmlFile(fileName, createNoFileRoot());
    }

    private static Node parse(File file) throws XmlFileException {
        try {
            // Parsing from the file (not a decoded String) lets the parser honour BOM and the encoding declaration
            Document document = newDocumentBuilder().parse(file);
            Element rootElement = document.getDocumentElement();
            removeFormattingWhitespace(rootElement);
            return rootElement;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new XmlFileException(e.getLocalizedMessage(), e);
        }
    }

    private static Node createNoFileRoot() {
        try {
            return newDocumentBuilder().newDocument().createElement(TAG_NO_FILE);
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("XML parser is not available", e);
        }
    }

    private static DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        // Deferred DOM expands nodes lazily, i.e. mutates on read; the tree is read from several threads
        dbf.setFeature("http://apache.org/xml/features/dom/defer-node-expansion", false);
        // A viewer must not fetch anything: no external DTDs (network hangs) and no external entities (XXE)
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
        dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        dbf.setXIncludeAware(false);
        dbf.setExpandEntityReferences(false);
        DocumentBuilder db = dbf.newDocumentBuilder();
        db.setErrorHandler(SILENT_ERROR_HANDLER);
        return db;
    }

    /**
     * Drops whitespace-only text containing line breaks (source indentation) so the indenting
     * serializer in {@link XmlTagInfo} produces clean output.
     */
    static void removeFormattingWhitespace(Node root) {
        Deque<Node> stack = new ArrayDeque<>();
        stack.push(root);
        while (!stack.isEmpty()) {
            Node node = stack.pop();
            Node child = node.getFirstChild();
            while (child != null) {
                Node next = child.getNextSibling();
                if (child instanceof Text) {
                    String data = ((Text) child).getData();
                    if (data.trim().isEmpty() && (data.indexOf('\n') >= 0 || data.indexOf('\r') >= 0)) {
                        node.removeChild(child);
                    } else {
                        ((Text) child).setData(TEXT_LINEARIZATION_PATTERN.matcher(data).replaceAll(TEXT_LINEARIZATION_REPLACEMENT));
                    }
                } else if (child.getNodeType() == Node.ELEMENT_NODE) {
                    stack.push(child);
                }
                child = next;
            }
        }
    }

    public Element getRootElement(){
        return (Element) root;
    }

    public Node getRootNode(){
        return root;
    }

    public static List<AttrInfo> getAttributesList(Node node){
        List<AttrInfo> list = null;
        if(node.hasAttributes()){
            NamedNodeMap map = node.getAttributes();
            list = new ArrayList<>(map.getLength());
            for(int i=0; i<map.getLength(); i++){
                list.add(new AttrInfo(map.item(i).getNodeName(),map.item(i).getNodeValue()));
            }
        }
        return list;
    }

    public String getFileName() {
        return fileName;
    }

    public String getName() {
        return file.getName();
    }

    public String getFullFileName() {
        return file.getAbsolutePath();
    }

    public int getAllNodesCount() {
        return allNodesCount;
    }
}
