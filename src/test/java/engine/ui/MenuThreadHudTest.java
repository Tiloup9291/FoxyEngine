package engine.ui;

import static org.junit.jupiter.api.Assertions.*;
import engine.input.InputAction;
import engine.input.InputFrame;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** MenuThread state machine + HUD render without crash. */
class MenuThreadHudTest {

    private static MenuThread menus(boolean[] quit, int[] toggles) {
        return new MenuThread(() -> quit[0] = true,
                () -> toggles[0]++, () -> toggles[1]++, () -> toggles[2]++);
    }

    private static InputFrame frame(InputAction pressed) {
        Map<InputAction, Boolean> held = new EnumMap<>(InputAction.class);
        if (pressed != null) held.put(pressed, true);
        Set<InputAction> p = pressed != null ? EnumSet.of(pressed) : EnumSet.noneOf(InputAction.class);
        return new InputFrame(held, p, 0, 0, 0, 0, 0, false, Set.of());
    }

    @Test
    void pauseThenResumeViaMenu() {
        MenuThread mt = menus(new boolean[1], new int[3]);
        mt.handleFrame(frame(InputAction.PAUSE_TOGGLE));
        mt.handleFrame(frame(InputAction.MENU_CONFIRM));
        assertEquals(GameState.RUNNING, mt.state());
        assertNull(mt.activeMenu());
    }

    @Test
    void optionsAndBack() {
        MenuThread mt = menus(new boolean[1], new int[3]);
        mt.handleFrame(frame(InputAction.PAUSE_TOGGLE));
        mt.handleFrame(frame(InputAction.BACK));
        mt.handleFrame(frame(InputAction.MENU_CONFIRM));
        assertEquals(GameState.OPTIONS, mt.state());
        mt.handleFrame(frame(InputAction.PAUSE_TOGGLE));
        assertEquals(GameState.PAUSED, mt.state());
    }

    @Test
    void quitConfirm() {
        boolean[] quit = {false};
        MenuThread mt = menus(quit, new int[3]);
        mt.setState(GameState.QUIT_CONFIRM);
        mt.handleFrame(frame(InputAction.BACK));
        mt.handleFrame(frame(InputAction.MENU_CONFIRM));
        assertTrue(quit[0]);
    }

    @Test
    void optionsActionsFire() {
        int[] toggles = new int[3];
        MenuThread mt = menus(new boolean[1], toggles);
        mt.setState(GameState.OPTIONS);
        mt.handleFrame(frame(InputAction.MENU_CONFIRM));
        assertEquals(1, toggles[0]);
        mt.syncOptions(true, false, 2);
    }

    @Test
    void hudRendersWithoutCrash() {
        Hud hud = new Hud();
        hud.setStatus("status");
        hud.setSub("sub-line");
        hud.setInfo("info");
        hud.setFps("FPS: 60");
        hud.setHealth(0.7f);
        hud.pushMessage("Sound on!");
        hud.pushMessage("Welcome");
        assertEquals(2, hud.messageCount());
        BufferedImage img = new BufferedImage(320, 200, BufferedImage.TYPE_INT_RGB);
        Graphics g = img.getGraphics();
        hud.drawHud(g, 320, 200);
        Menu m = new Menu("PAUSE", java.util.List.of(new MenuItem("Resume", () -> {})));
        hud.drawMenu(g, 320, 200, m);
        g.dispose();
    }
}
