package das.tools.gui.format;

import das.tools.gui.entity.XmlFile;
import das.tools.gui.entity.XmlTagInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class TagFormatterTest {
    @TempDir
    Path dir;

    private XmlTagInfo tag(String xml) throws Exception {
        Path file = dir.resolve("t.xml");
        Files.write(file, xml.getBytes(StandardCharsets.UTF_8));
        return new XmlTagInfo(new XmlFile(file.toString()).getRootElement());
    }

    @Test
    void keyValue() throws Exception {
        assertEquals("item=text\na=1\nb=2\n", TagFormatter.keyValue(tag("<item a=\"1\" b=\"2\">text</item>")));
        assertEquals("item\n", TagFormatter.keyValue(tag("<item/>")));
    }

    @Test
    void attributeNamesAndValues() throws Exception {
        XmlTagInfo t = tag("<item a=\"1\" b=\"2\"/>");
        assertEquals("a\nb\n", TagFormatter.attributeNames(t));
        assertEquals("1\n2\n", TagFormatter.attributeValues(t));
        assertEquals("", TagFormatter.attributeValues(tag("<item/>")));
    }

    @Test
    void htmlIsEscaped() throws Exception {
        String html = TagFormatter.toHtml(tag("<item a=\"x&lt;y&amp;z\">1 &lt; 2</item>"));
        assertTrue(html.contains("x&lt;y&amp;z"), html);
        assertTrue(html.contains("1 &lt; 2"), html);
        assertFalse(html.contains("x<y"), html);
    }
}
