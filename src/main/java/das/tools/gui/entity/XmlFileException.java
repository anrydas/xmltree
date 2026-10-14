package das.tools.gui.entity;

/** The XML file exists but can't be read or parsed. */
public class XmlFileException extends Exception {
    public XmlFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
