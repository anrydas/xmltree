package das.tools.gui.entity;

public class AttrInfo {
    private String name = "";
    private String value = "";

    public AttrInfo(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}
