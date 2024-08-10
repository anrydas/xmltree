package das.tools.gui.entity;

import das.tools.gui.Utils;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by das on 14.03.2015.
 */
public class XmlFile {
    public static final String TAG_NO_FILE = "no_file";
    private static final String XML_LINARIZATION_REGEX = "(>|&gt;){1,1}(\\t)*([\\n\\r])+(\\s)*(<|&lt;){1,1}";
    private static final String XML_LINARIZATION_REPLACEMENT = "$1$5";

    private String fileName = "";
    private File file = null;
    private Node root = null;

    public XmlFile(String fileName) {
        openNewFile(fileName);
    }

    public void openNewFile(String fileName){
        this.fileName = fileName;
        this.file = new File(fileName);
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = null;
            if(this.file.exists()) {
                String fileContent = linerizeString(new String(Files.readAllBytes(Paths.get(file.getAbsolutePath()))));
                document = db.parse(new ByteArrayInputStream(fileContent.getBytes()));
                this.root = document.getFirstChild();
            } else{
                document = db.newDocument();
                this.root = document.createElement(TAG_NO_FILE);
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
    }

    private static String linerizeString(String xml) {
        return Utils.isNotEmpty(xml) ? xml.trim().replaceAll(XML_LINARIZATION_REGEX, XML_LINARIZATION_REPLACEMENT) : "";
    }

    public Element getRootElement(){
        return (Element) root;
    }

    public Node getRootNode(){
        return root;
    }

    public List<String> getChildListStr(Node node){
        List<String> L = new ArrayList<String>();
        NodeList list = node.getChildNodes();
        for(int i=0; i<list.getLength();i++){
            if(list.item(i).getNodeType() == Node.ELEMENT_NODE) {
                L.add(list.item(i).getNodeName());
            }
        }
        return L;
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

    public String getTagText(Node node){
        return node.getNodeValue();
    }

    public NodeList getChild(Node node){
        return node.getChildNodes();
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
}
