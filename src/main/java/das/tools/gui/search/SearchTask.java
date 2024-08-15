package das.tools.gui.search;

import das.tools.gui.XmlForm;

import javax.swing.*;

public class SearchTask extends SwingWorker<Void, Void> {
    private final XmlForm form;
    private final SearchProcessor searchProcessor;

    public SearchTask(XmlForm form, SearchProcessor searchProcessor) {
        this.form = form;
        this.searchProcessor = searchProcessor;
        this.searchProcessor.setProgressBar(form.getProgressBar());
    }

    @Override
    protected Void doInBackground() throws Exception {
        searchProcessor.doSearch();
        return null;
    }

    @Override
    protected void done() {
        super.done();
        form.searchCompleted();
    }
}
