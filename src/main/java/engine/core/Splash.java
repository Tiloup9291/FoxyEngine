package engine.core;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

public final class Splash {
    private Splash() {
    }

    public static long durationMs() {
        return 2200L;
    }

    public static void drawBanner(String title, String version) {
        System.out.println("=================================");
        System.out.println("  " + title + " " + version);
        System.out.println("  100% Java - software rasterizer");
        System.out.println("=================================");
    }

    public static void draw(Graphics g, int w, int h, String title, String subtitle, String[] controls) {
        g.setColor(new Color(1053720));
        g.fillRect(0, 0, w, h);
        Font big = new Font("Monospaced", 1, Math.max(18, w / 28));
        Font mid = new Font("Monospaced", 1, 14);
        g.setFont(big);
        FontMetrics fm = g.getFontMetrics();
        g.setColor(new Color(5088255));
        g.drawString(title, (w - fm.stringWidth(title)) / 2, h / 2 - 30);
        g.setFont(mid);
        fm = g.getFontMetrics();
        g.setColor(new Color(16765286));
        g.drawString(subtitle, (w - fm.stringWidth(subtitle)) / 2, h / 2 + 2);
        g.setColor(new Color(7268279));
        int y = h / 2 + 32;
        if (controls != null) {
            for (String c : controls) {
                g.drawString(c, (w - fm.stringWidth(c)) / 2, y);
                y += fm.getHeight() + 2;
            }
        }
    }
}
