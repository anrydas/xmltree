package das.tools.gui;

import java.io.File;

public class Main {
    private static String[] appArgs;

    public static void main(String[] args) {
        appArgs = args;
        newForm(getAppParameter());
    }

    public static void newForm(String fileName){
        new XmlForm(fileName);
    }

    public static String getAppParameter() {
        if (appArgs.length == 1 && (new File(appArgs[0]).exists())) {
            return appArgs[0];
        } else {
            return "";
        }
    }
}
