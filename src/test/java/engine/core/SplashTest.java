package engine.core;

import static org.junit.jupiter.api.Assertions.*;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import org.junit.jupiter.api.Test;

/** Splash screen: draws non-black pixels, positive duration. */
class SplashTest {

    @Test
    void splashRendersWithoutCrash() {
        Splash.drawBanner("JAVA 3D ENGINE", "v1.0");
        BufferedImage img = new BufferedImage(400, 300, BufferedImage.TYPE_INT_RGB);
        Graphics g = img.getGraphics();
        Splash.draw(g, 400, 300, "JAVA 3D ENGINE", "v1.0 - playable demo",
                new String[]{"ZQSD move", "SPACE jump", "G explosion", "N level", "ESC menu"});
        g.dispose();
        int[] px = img.getRGB(0, 0, 400, 300, null, 0, 400);
        int nonBlack = 0;
        for (int c : px) if ((c & 0xFFFFFF) != 0) nonBlack++;
        assertTrue(nonBlack > 1000, "splash empty, nonBlack=" + nonBlack);
    }

    @Test
    void positiveDuration() {
        assertTrue(Splash.durationMs() > 0);
    }
}
