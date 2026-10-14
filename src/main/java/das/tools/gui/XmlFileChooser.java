package das.tools.gui;

import das.tools.gui.entity.FilesFilter;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/** One Open File dialog shared by all windows, so it remembers the last used directory. Used on the EDT only. */
public final class XmlFileChooser {
    private static JFileChooser chooser;

    private XmlFileChooser() {
    }

    private static JFileChooser get() {
        if (chooser == null) {
            chooser = new JFileChooser(new File(System.getProperty("user.dir")));
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.setFileFilter(new FilesFilter());
        }
        return chooser;
    }

    /** Next dialog opens in the directory of {@code file} (or in {@code file} itself if it's a directory). */
    public static void setCurrentDirectory(File file) {
        get().setCurrentDirectory(file);
    }

    /** @return absolute path of the selected file, or "" if the dialog was cancelled */
    public static String select(Component parent) {
        JFileChooser dlg = get();
        if (dlg.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
            return dlg.getSelectedFile().getAbsolutePath();
        }
        return "";
    }
}
