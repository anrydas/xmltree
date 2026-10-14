package das.tools.gui.format;

import das.tools.gui.Utils;
import das.tools.gui.entity.AttrInfo;
import das.tools.gui.entity.XmlTagInfo;

import java.util.Collections;
import java.util.List;

/** Text representations of a tag for the info pane and the clipboard. */
public final class TagFormatter {
    private static final String FMT_FILE_NAME = "File: <b>%s</b> (<i>%s</i>)";
    private static final String FMT_TAG = "<i>tag:</i>&nbsp;<font color='blue' size='5'><b>%s</b></font>%s<br/>";
    private static final String FMT_VALUE = "&nbsp;=&nbsp;\"<font color='green' size='5'><b>%s</b></font>\"";
    private static final String FMT_ATTR = "<i>attr:</i>&nbsp;<font color='red' size='5'><b>%s</b></font>=\"<font color='green' size='5'><b>%s</b></font>\"<br/>";

    private TagFormatter() {
    }

    public static String toHtml(XmlTagInfo tag) {
        String tagValue = tag.getValue();
        StringBuilder sb = new StringBuilder(String.format(FMT_TAG, Utils.escapeHtml(tag.getTagName()),
                Utils.isNotEmpty(tagValue) ? String.format(FMT_VALUE, Utils.escapeHtml(tagValue)) : ""));
        for (AttrInfo a : attributes(tag)) {
            sb.append(String.format(FMT_ATTR, Utils.escapeHtml(a.getName()), Utils.escapeHtml(a.getValue())));
        }
        return sb.toString();
    }

    public static String fileToHtml(String fileName, String fullFileName) {
        return String.format(FMT_FILE_NAME, Utils.escapeHtml(fileName), Utils.escapeHtml(fullFileName));
    }

    /** Attribute names, one per line. */
    public static String attributeNames(XmlTagInfo tag) {
        StringBuilder sb = new StringBuilder();
        for (AttrInfo a : attributes(tag)) {
            sb.append(a.getName()).append("\n");
        }
        return sb.toString();
    }

    /** Attribute values, one per line. */
    public static String attributeValues(XmlTagInfo tag) {
        StringBuilder sb = new StringBuilder();
        for (AttrInfo a : attributes(tag)) {
            sb.append(a.getValue()).append("\n");
        }
        return sb.toString();
    }

    /** {@code tag[=value]} line followed by {@code name=value} line per attribute. */
    public static String keyValue(XmlTagInfo tag) {
        StringBuilder sb = new StringBuilder(tag.getTagName());
        if (Utils.isNotEmpty(tag.getValue())) sb.append("=").append(tag.getValue());
        sb.append("\n");
        for (AttrInfo a : attributes(tag)) {
            sb.append(a.getName()).append("=").append(a.getValue()).append("\n");
        }
        return sb.toString();
    }

    private static List<AttrInfo> attributes(XmlTagInfo tag) {
        return tag.getAttributes() != null ? tag.getAttributes() : Collections.<AttrInfo>emptyList();
    }
}
