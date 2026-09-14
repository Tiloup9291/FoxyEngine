package engine.input;

import static org.junit.jupiter.api.Assertions.*;
import java.awt.event.KeyEvent;
import org.junit.jupiter.api.Test;

/** ActionMapper: AZERTY+US defaults, rebind. */
class ActionMapperTest {

    @Test
    void defaultsAzertyAndUs() {
        ActionMapper m = new ActionMapper();
        assertEquals(InputAction.FWD, m.mapKey(KeyEvent.VK_W));
        assertEquals(InputAction.FWD, m.mapKey(KeyEvent.VK_Z));
        assertEquals(InputAction.LEFT, m.mapKey(KeyEvent.VK_A));
        assertEquals(InputAction.LEFT, m.mapKey(KeyEvent.VK_Q));
        assertEquals(InputAction.JUMP, m.mapKey(KeyEvent.VK_SPACE));
    }

    @Test
    void rebind() {
        ActionMapper m = new ActionMapper();
        m.bind(KeyEvent.VK_F1, InputAction.JUMP);
        assertEquals(InputAction.JUMP, m.mapKey(KeyEvent.VK_F1));
        m.unbind(KeyEvent.VK_F1);
        assertNull(m.mapKey(KeyEvent.VK_F1));
    }
}
