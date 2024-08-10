package das.tools.gui;

import java.io.File;

public class Utils {
    public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');
        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }

    public static boolean isNotEmpty(String s) {
        return s != null && !"".equals(s);
    }

    public static boolean isEven(long num) {
        return (num & 1) == 0;
    }
}
