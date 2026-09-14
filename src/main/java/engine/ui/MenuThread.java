package engine.ui;

import engine.input.InputAction;
import engine.input.InputFrame;
import engine.ui.GameState;
import engine.ui.Menu;
import engine.ui.MenuItem;
import java.util.ArrayList;
import java.util.List;

public final class MenuThread {
    private GameState state = GameState.RUNNING;
    private Menu activeMenu;
    private Menu pauseMenu;
    private Menu optionsMenu;
    private final Menu quitMenu;
    private final List<Runnable> quitListeners = new ArrayList<Runnable>();
    private final Runnable onToggleWireframe;
    private final Runnable onToggleSound;
    private final Runnable onCycleBands;

    public MenuThread(Runnable onQuit, Runnable onToggleWireframe, Runnable onToggleSound, Runnable onCycleBands) {
        this.onToggleWireframe = onToggleWireframe;
        this.onToggleSound = onToggleSound;
        this.onCycleBands = onCycleBands;
        if (onQuit != null) {
            this.quitListeners.add(onQuit);
        }
        this.pauseMenu = new Menu("PAUSE", List.of(new MenuItem("Resume", () -> this.setState(GameState.RUNNING)), new MenuItem("Options", () -> this.setState(GameState.OPTIONS)), new MenuItem("Quit", () -> this.setState(GameState.QUIT_CONFIRM))), () -> this.setState(GameState.RUNNING));
        this.optionsMenu = this.buildOptions(false, true, 4);
        this.quitMenu = new Menu("QUIT?", List.of(new MenuItem("No, resume", () -> this.setState(GameState.RUNNING)), new MenuItem("Yes, quit", this::fireQuit)), () -> this.setState(GameState.PAUSED));
    }

    private Menu buildOptions(boolean wireframe, boolean soundOn, int bands) {
        return new Menu("OPTIONS", List.of(new MenuItem("Display: " + (wireframe ? "wireframe" : "solid"), () -> {
            this.onToggleWireframe.run();
            this.refreshOptions();
        }), new MenuItem("Sound: " + (soundOn ? "ON" : "MUTED"), () -> {
            this.onToggleSound.run();
            this.refreshOptions();
        }), new MenuItem("Render bands: " + bands, () -> {
            this.onCycleBands.run();
            this.refreshOptions();
        }), new MenuItem("Back", () -> this.setState(GameState.PAUSED))), () -> this.setState(GameState.PAUSED));
    }

    public void refreshOptions() {
    }

    public void syncOptions(boolean wireframe, boolean soundOn, int bands) {
        int sel = this.optionsMenu != null ? this.optionsMenu.selected() : 0;
        this.optionsMenu = this.buildOptions(wireframe, soundOn, bands);
        for (int i = 0; i < sel && i < this.optionsMenu.size(); ++i) {
            this.optionsMenu.moveDown();
        }
        if (this.state == GameState.OPTIONS) {
            this.activeMenu = this.optionsMenu;
        }
    }

    private void fireQuit() {
        for (Runnable r : this.quitListeners) {
            r.run();
        }
    }

    public GameState state() {
        return this.state;
    }

    public Menu activeMenu() {
        return this.activeMenu;
    }

    public boolean paused() {
        return this.state != GameState.RUNNING;
    }

    public void setState(GameState s) {
        this.state = s;
        this.activeMenu = switch (s) {
            case PAUSED -> this.pauseMenu;
            case OPTIONS -> this.optionsMenu;
            case QUIT_CONFIRM -> this.quitMenu;
            case RUNNING -> null;
        };
    }

    public void togglePause() {
        this.setState(this.paused() ? GameState.RUNNING : GameState.PAUSED);
    }

    public boolean handleFrame(InputFrame frame) {
        if (frame.pressed(InputAction.PAUSE_TOGGLE)) {
            if (this.state == GameState.QUIT_CONFIRM || this.state == GameState.OPTIONS) {
                if (this.activeMenu != null) {
                    this.activeMenu.back();
                }
            } else {
                this.togglePause();
            }
            return this.paused();
        }
        if (this.activeMenu != null) {
            this.activeMenu.handleFrame(frame);
        }
        return this.paused();
    }
}
