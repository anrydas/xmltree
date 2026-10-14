package das.tools.gui.search;

import javax.swing.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class SearchTask extends SwingWorker<Void, Integer> {
    private final SearchController controller;
    private final SearchProcessor searchProcessor;
    private final JProgressBar progressBar;

    SearchTask(SearchController controller, SearchProcessor searchProcessor, JProgressBar progressBar) {
        this.controller = controller;
        this.searchProcessor = searchProcessor;
        this.progressBar = progressBar;
        progressBar.setMinimum(0);
        progressBar.setMaximum(searchProcessor.getAllNodesCount());
        progressBar.setValue(0);
        // Progress is published from the worker thread and applied to the bar on the EDT in process()
        this.searchProcessor.setProgressListener(this::publish);
    }

    @Override
    protected Void doInBackground() {
        searchProcessor.doSearch();
        return null;
    }

    @Override
    protected void process(List<Integer> chunks) {
        progressBar.setValue(chunks.get(chunks.size() - 1));
    }

    @Override
    protected void done() {
        try {
            get();
        } catch (InterruptedException | ExecutionException e) {
            Throwable cause = e instanceof ExecutionException ? e.getCause() : e;
            cause.printStackTrace();
            JOptionPane.showMessageDialog(controller.getDialogParent(), String.valueOf(cause),
                    "Search error", JOptionPane.ERROR_MESSAGE);
        }
        controller.searchCompleted();
    }
}
