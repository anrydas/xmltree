package das.tools.gui;

import javax.swing.*;
import java.io.File;

public class Main {
    public static void main(String[] args) {
        String fileName = args.length == 1 && new File(args[0]).exists() ? args[0] : "";
        SwingUtilities.invokeLater(() -> new XmlForm(fileName, true));
    }
}
