package das.tools.gui.menu.action;

import das.tools.gui.XmlForm;

public class AppActions {
    private final CloseThisWindowsMenuAction closeThisAction;
    private final CloseAllWindowsMenuAction closeAllAction;
    private final CloseAllWindowsMenuAction exitAction;
    private final OpenFileAction openFileAction;
    private final CopyTagAction copyTagNameAction;
    private final CopyAttrNamesAction copyAttrNamesAction;
    private final CopyAttrValuesAction copyAttrValuesAction;
    private final CopyAsKeyValueAction copyKeyValueAction;
    private final CopyAsXmlAction copyXmlAction;
    private final FindInAction searchAction;
    private final FindForwardAction findForwardAction;
    private final FindBackwardAction findBackwardAction;

    public AppActions(XmlForm form) {
        openFileAction = new OpenFileAction(form, "Open", null);
        closeThisAction = new CloseThisWindowsMenuAction(form, "Close this", null);
        exitAction = new CloseAllWindowsMenuAction(form, "Exit", null);
        closeAllAction = new CloseAllWindowsMenuAction(form, "Close All", null);
        copyTagNameAction = new CopyTagAction(form, "Copy tag name", null);
        copyAttrNamesAction = new CopyAttrNamesAction(form, "Copy attribute(s) Name(s)", null);
        copyAttrValuesAction = new CopyAttrValuesAction(form, "Copy attribute(s) Value(s)", null);
        copyKeyValueAction = new CopyAsKeyValueAction(form, "Copy All as 'key=value'", null);
        copyXmlAction = new CopyAsXmlAction(form, "Copy All as XML", null);
        searchAction = new FindInAction(form, "Find in...", null);
        findForwardAction = new FindForwardAction(form, "Find next", null);
        findBackwardAction = new FindBackwardAction(form, "Find prev", null);
    }

    public CloseThisWindowsMenuAction getCloseThisAction() {
        return closeThisAction;
    }

    public CloseAllWindowsMenuAction getCloseAllAction() {
        return closeAllAction;
    }

    public CloseAllWindowsMenuAction getExitAction() {
        return exitAction;
    }

    public OpenFileAction getOpenFileAction() {
        return openFileAction;
    }

    public CopyTagAction getCopyTagNameAction() {
        return copyTagNameAction;
    }

    public CopyAttrNamesAction getCopyAttrNamesAction() {
        return copyAttrNamesAction;
    }

    public CopyAttrValuesAction getCopyAttrValuesAction() {
        return copyAttrValuesAction;
    }

    public CopyAsKeyValueAction getCopyKeyValueAction() {
        return copyKeyValueAction;
    }

    public CopyAsXmlAction getCopyXmlAction() {
        return copyXmlAction;
    }

    public FindInAction getSearchAction() {
        return searchAction;
    }

    public FindForwardAction getFindForwardAction() {
        return findForwardAction;
    }

    public FindBackwardAction getFindBackwardAction() {
        return findBackwardAction;
    }
}
