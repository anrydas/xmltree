package das.tools.gui.entity;

import das.tools.gui.Utils;

import javax.swing.filechooser.FileFilter;
import java.io.File;

public class FilesFilter extends FileFilter {
    public static final String FILE_EXTENSION_XML = "xml";
    public static final String FILE_EXTENSION_XSD = "xsd";

    @Override
    public boolean accept(File f) {
        if(f.isDirectory()){
            return true;
        }
        String ext = Utils.getExtension(f);
        if(ext != null){
            return ext.equalsIgnoreCase(FILE_EXTENSION_XML) ||
                    ext.equalsIgnoreCase(FILE_EXTENSION_XSD);
        }
        return false;
    }

    @Override
    public String getDescription() {
        return "XML or XSD files (*.xml,*.xsd)";
    }
}
