package engine.ui;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/** Menu: navigation, wrap, activate, back. */
class MenuTest {

    private static Menu menu(boolean[] acted) {
        return new Menu("M", java.util.List.of(
                new MenuItem("a", () -> acted[0] = true),
                new MenuItem("b", () -> {}),
                new MenuItem("c", () -> {})));
    }

    @Test
    void moveWraps() {
        Menu m = menu(new boolean[1]);
        assertEquals(0, m.selected());
        m.moveUp();
        assertEquals(2, m.selected());
        m.moveDown();
        assertEquals(0, m.selected());
    }

    @Test
    void activateRunsAction() {
        boolean[] acted = {false};
        menu(acted).activate();
        assertTrue(acted[0]);
    }

    @Test
    void rejectsEmpty() {
        assertThrows(IllegalArgumentException.class, () -> new Menu("M", java.util.List.of()));
        assertThrows(IllegalArgumentException.class, () -> new MenuItem("", () -> {}));
    }
}
