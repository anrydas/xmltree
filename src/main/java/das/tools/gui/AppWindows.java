package das.tools.gui;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Application-wide registry of the opened windows (one per XML file). Used on the EDT only. */
public final class AppWindows {
    private static final List<JFrame> FRAMES = new ArrayList<>(5);
    private static final List<Runnable> CHANGE_LISTENERS = new ArrayList<>(5);

    private AppWindows() {
    }

    public static void register(JFrame frame) {
        FRAMES.add(frame);
        fireChanged();
    }

    public static void unregister(JFrame frame) {
        FRAMES.remove(frame);
        fireChanged();
    }

    /** Called after a window is opened or closed. */
    public static void addChangeListener(Runnable listener) {
        CHANGE_LISTENERS.add(listener);
    }

    public static void removeChangeListener(Runnable listener) {
        CHANGE_LISTENERS.remove(listener);
    }

    /** Lets the windows menus pick up a changed window title. */
    public static void fireChanged() {
        for (Runnable listener : new ArrayList<>(CHANGE_LISTENERS)) {
            listener.run();
        }
    }

    public static List<JFrame> getFrames() {
        return Collections.unmodifiableList(new ArrayList<>(FRAMES));
    }
}
