package das.tools.gui.search;

public class SearchType {
    private static final int SEARCH_IN_TAG_NAMES   = 0b0001;
    private static final int SEARCH_IN_TAG_VALUES  = 0b0010;
    private static final int SEARCH_IN_ATTR_NAMES  = 0b0100;
    private static final int SEARCH_IN_ATTR_VALUES = 0b1000;
    private static final int SEARCH_EVERYWHERE = 0b1111;

    private int type = 0;

    public SearchType(boolean isTagNames, boolean isTagValues, boolean isAttrNames, boolean isAttrValues) {
        if (isTagNames) type = type | SEARCH_IN_TAG_NAMES;
        if (isTagValues) type = type | SEARCH_IN_TAG_VALUES;
        if (isAttrNames) type = type | SEARCH_IN_ATTR_NAMES;
        if (isAttrValues) type = type | SEARCH_IN_ATTR_VALUES;
    }

    public boolean isSearchInTagName() {
        return (type & SEARCH_IN_TAG_NAMES) == SEARCH_IN_TAG_NAMES;
    }

    public boolean isSearchInTagValues() {
        return (type & SEARCH_IN_TAG_VALUES) == SEARCH_IN_TAG_VALUES;
    }

    public boolean isSearchInAttrNames() {
        return (type & SEARCH_IN_ATTR_NAMES) == SEARCH_IN_ATTR_NAMES;
    }

    public boolean isSearchInAttrValues() {
        return (type & SEARCH_IN_ATTR_VALUES) == SEARCH_IN_ATTR_VALUES;
    }

    public boolean isSearchEverywhere() {
        return (type & SEARCH_EVERYWHERE) == SEARCH_EVERYWHERE;
    }
}
