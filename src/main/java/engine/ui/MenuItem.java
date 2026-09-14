package engine.ui;

import java.util.ArrayList;
import java.util.List;

public final class MenuItem {
    private final String label;
    private final Runnable action;

    public MenuItem(String label, Runnable action) {
        if (label == null || label.isBlank()) {
            throw new IllegalArgumentException("empty label");
        }
        if (action == null) {
            throw new IllegalArgumentException("null action");
        }
        this.label = label;
        this.action = action;
    }

    public String label() {
        return this.label;
    }

    public void activate() {
        this.action.run();
    }

    public String toString() {
        return this.label;
    }

    public static List<MenuItem> of(Object ... labelAndAction) {
        if (labelAndAction.length % 2 != 0) {
            throw new IllegalArgumentException("label/action pairs");
        }
        ArrayList<MenuItem> out = new ArrayList<MenuItem>(labelAndAction.length / 2);
        for (int i = 0; i < labelAndAction.length; i += 2) {
            out.add(new MenuItem((String)labelAndAction[i], (Runnable)labelAndAction[i + 1]));
        }
        return out;
    }
}
