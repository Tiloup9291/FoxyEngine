package engine.ui;

import engine.input.InputAction;
import engine.input.InputFrame;
import engine.ui.MenuItem;
import java.util.List;

public final class Menu {
    private final String title;
    private final List<MenuItem> items;
    private int selected;
    private final Runnable onBack;

    public Menu(String title, List<MenuItem> items, Runnable onBack) {
        if (title == null) {
            throw new IllegalArgumentException("null title");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("empty menu");
        }
        this.title = title;
        this.items = List.copyOf(items);
        this.onBack = onBack != null ? onBack : () -> {};
    }

    public Menu(String title, List<MenuItem> items) {
        this(title, items, null);
    }

    public String title() {
        return this.title;
    }

    public List<MenuItem> items() {
        return this.items;
    }

    public int selected() {
        return this.selected;
    }

    public MenuItem selectedItem() {
        return this.items.get(this.selected);
    }

    public int size() {
        return this.items.size();
    }

    public void moveUp() {
        this.selected = (this.selected - 1 + this.items.size()) % this.items.size();
    }

    public void moveDown() {
        this.selected = (this.selected + 1) % this.items.size();
    }

    public void activate() {
        this.selectedItem().activate();
    }

    public void back() {
        this.onBack.run();
    }

    public boolean handleFrame(InputFrame frame) {
        boolean down;
        boolean used = false;
        boolean up = frame.pressed(InputAction.LOOK_U) || frame.pressed(InputAction.FWD);
        boolean bl = down = frame.pressed(InputAction.LOOK_D) || frame.pressed(InputAction.BACK);
        if (up && !down) {
            this.moveUp();
            used = true;
        } else if (down && !up) {
            this.moveDown();
            used = true;
        }
        if (frame.pressed(InputAction.MENU_CONFIRM) || frame.pressed(InputAction.JUMP)) {
            this.activate();
            used = true;
        }
        if (frame.pressed(InputAction.PAUSE_TOGGLE)) {
            this.back();
            used = true;
        }
        return used;
    }
}
