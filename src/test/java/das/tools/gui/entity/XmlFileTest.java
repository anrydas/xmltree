package das.tools.gui.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class XmlFileTest {
    @TempDir
    Path dir;

    private String write(String name, byte[] content) throws IOException {
        Path file = dir.resolve(name);
        Files.write(file, content);
        return file.toString();
    }

    private String write(String name, String content) throws IOException {
        return write(name, content.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void rootIsDocumentElementWhenCommentOrDoctypeComesFirst() throws Exception {
        String file = write("a.xml", "<?xml version=\"1.0\"?>\n<!-- c -->\n<!DOCTYPE root>\n<root a=\"1\">\n    <child>v</child>\n</root>\n");
        XmlFile xml = new XmlFile(file);
        assertEquals("root", xml.getRootElement().getTagName());
        assertEquals(1, xml.getAllNodesCount());
    }

    @Test
    void honoursDeclaredEncoding() throws Exception {
        String text = "Привіт";
        String file = write("cp1251.xml", ("<?xml version=\"1.0\" encoding=\"windows-1251\"?>\n<root><c>" + text + "</c></root>")
                .getBytes(Charset.forName("windows-1251")));
        XmlTagInfo child = new XmlTagInfo(new XmlFile(file).getRootElement().getFirstChild());
        assertEquals(text, child.getValue());
    }

    @Test
    void acceptsUtf8Bom() throws Exception {
        byte[] body = "<root><c>x</c></root>".getBytes(StandardCharsets.UTF_8);
        byte[] content = new byte[body.length + 3];
        content[0] = (byte) 0xEF; content[1] = (byte) 0xBB; content[2] = (byte) 0xBF;
        System.arraycopy(body, 0, content, 3, body.length);
        assertEquals("root", new XmlFile(write("bom.xml", content)).getRootElement().getTagName());
    }

    @Test
    void doesNotResolveExternalEntities() throws Exception {
        String secret = write("secret.txt", "TOP-SECRET");
        String file = write("xxe.xml", "<?xml version=\"1.0\"?>\n<!DOCTYPE r [<!ENTITY x SYSTEM \"" +
                Paths.get(secret).toUri() + "\">]>\n<r>&x;</r>");
        try {
            XmlFile xml = new XmlFile(file);
            assertFalse(new XmlTagInfo(xml.getRootElement()).getText().contains("TOP-SECRET"));
        } catch (XmlFileException e) {
            // refusing to parse is fine too, as long as the content is not read
        }
    }

    @Test
    void ignoresExternalDtd() throws Exception {
        String file = write("dtd.xml", "<?xml version=\"1.0\"?>\n<!DOCTYPE r SYSTEM \"http://127.0.0.1:9/none.dtd\">\n<r/>");
        assertEquals("r", new XmlFile(file).getRootElement().getTagName());
    }

    @Test
    void malformedFileThrows() throws Exception {
        String file = write("wrong.txt", "The file for\ntesting\nwrong format file");
        assertThrows(XmlFileException.class, () -> new XmlFile(file));
    }

    @Test
    void missingFileGivesPlaceholder() throws Exception {
        XmlFile xml = new XmlFile(dir.resolve("nope.xml").toString());
        assertEquals(XmlFile.TAG_NO_FILE, xml.getRootElement().getTagName());
        assertEquals(0, xml.getAllNodesCount());
    }

    @Test
    void deepDocumentDoesNotOverflowStack() throws Exception {
        int depth = 50_000;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < depth; i++) sb.append("<a>\n");
        for (int i = 0; i < depth; i++) sb.append("</a>\n");
        XmlFile xml = new XmlFile(write("deep.xml", sb.toString()));
        assertEquals(depth - 1, xml.getAllNodesCount());
    }

    @Test
    void tagTextIsFormattedLazilyAndCached() throws Exception {
        XmlTagInfo tag = new XmlTagInfo(new XmlFile(write("l.xml", "<root><a x=\"1\">v</a></root>")).getRootElement());
        String text = tag.getText();
        assertTrue(text.contains("<a x=\"1\">v</a>"), text);
        assertSame(text, tag.getText());
    }

    @Test
    void noFilePlaceholderHasHelpText() {
        XmlTagInfo tag = new XmlTagInfo(XmlFile.noFile("").getRootElement());
        assertEquals(XmlTagInfo.NO_FILE_TEXT, tag.getText());
    }

    @Test
    void formattingWhitespaceIsDroppedButContentKept() throws Exception {
        String file = write("ws.xml", "<root>  \n\t<a>keep  me</a>\r\n  <b> </b>\n</root>");
        XmlTagInfo root = new XmlTagInfo(new XmlFile(file).getRootElement());
        String text = root.getText().replace("\r", "");
        assertTrue(text.contains("<a>keep  me</a>"), text);
        assertTrue(text.contains("<b> </b>"), text);
        assertFalse(text.contains("\n\n"), text);
    }
}
