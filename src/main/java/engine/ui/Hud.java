package engine.ui;

import engine.ui.Menu;
import engine.ui.MenuItem;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

public final class Hud {
    public static final Color TEXT = new Color(0, 255, 136);
    public static final Color DIM = new Color(0, 200, 110);
    public static final Color WARN = new Color(255, 209, 102);
    public static final Color DANGER = new Color(239, 71, 111);
    public static final Color PANEL = new Color(0, 0, 0, 170);
    private Font font = new Font("Monospaced", 1, 14);
    private Font bigFont = new Font("Monospaced", 1, 22);
    private final Deque<String> messages = new ArrayDeque<String>();
    private int maxMessages = 4;
    private float health = 1.0f;
    private String statusLine = "";
    private String subLine = "";
    private String infoLine = "";
    private String fpsLine = "";
    private boolean crosshair = true;

    public void setHealth(float h) {
        this.health = Math.max(0.0f, Math.min(1.0f, h));
    }

    public float health() {
        return this.health;
    }

    public void setStatus(String s) {
        this.statusLine = s != null ? s : "";
    }

    public void setSub(String s) {
        this.subLine = s != null ? s : "";
    }

    public void setInfo(String s) {
        this.infoLine = s != null ? s : "";
    }

    public void setFps(String s) {
        this.fpsLine = s != null ? s : "";
    }

    public void setCrosshair(boolean c) {
        this.crosshair = c;
    }

    public synchronized void pushMessage(String msg) {
        this.messages.addLast(msg);
        while (this.messages.size() > this.maxMessages) {
            this.messages.removeFirst();
        }
    }

    public synchronized int messageCount() {
        return this.messages.size();
    }

    public synchronized void clearMessages() {
        this.messages.clear();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void drawHud(Graphics g, int w, int h) {
        g.setFont(this.font);
        g.setColor(Color.GREEN);
        FontMetrics fm = g.getFontMetrics();
        int y = fm.getAscent() + 8;
        if (!this.statusLine.isEmpty()) {
            g.drawString(this.statusLine, 12, y);
            y += fm.getHeight();
        }
        if (!this.subLine.isEmpty()) {
            g.drawString(this.subLine, 12, y);
            y += fm.getHeight();
        }
        if (!this.infoLine.isEmpty()) {
            g.drawString(this.infoLine, 12, y);
            y += fm.getHeight();
        }
        if (!this.fpsLine.isEmpty()) {
            g.setColor(DIM);
            g.drawString(this.fpsLine, 12, y);
        }
        int bw = 180;
        int bh = 12;
        int bx = 12;
        int by = h - bh - 12;
        g.setColor(Color.DARK_GRAY);
        g.fillRect(bx, by, bw, bh);
        g.setColor(this.health > 0.3f ? TEXT : DANGER);
        g.fillRect(bx, by, (int)((float)bw * this.health), bh);
        g.setColor(Color.BLACK);
        g.drawRect(bx, by, bw, bh);
        if (this.crosshair) {
            int cx = w / 2;
            int cy = h / 2;
            int r = 6;
            g.setColor(TEXT);
            g.drawLine(cx - r, cy, cx + r, cy);
            g.drawLine(cx, cy - r, cx, cy + r);
        }
        Hud hud = this;
        synchronized (hud) {
            int my = h - 30;
            Iterator<String> it = this.messages.descendingIterator();
            g.setColor(WARN);
            while (it.hasNext()) {
                String m = it.next();
                g.drawString(m, w - fm.stringWidth(m) - 12, my);
                my -= fm.getHeight();
            }
        }
    }

    public void drawMenu(Graphics g, int w, int h, Menu menu) {
        g.setColor(PANEL);
        g.fillRect(0, 0, w, h);
        FontMetrics big = g.getFontMetrics(this.bigFont);
        FontMetrics fm = g.getFontMetrics(this.font);
        List<MenuItem> items = menu.items();
        int titleW = big.stringWidth(menu.title());
        int maxItem = 0;
        for (MenuItem it : items) {
            maxItem = Math.max(maxItem, fm.stringWidth(it.label()));
        }
        int boxW = Math.max(titleW, maxItem + 40) + 60;
        int boxH = big.getHeight() + items.size() * (fm.getHeight() + 4) + 50;
        int bx = (w - boxW) / 2;
        int by = (h - boxH) / 2;
        g.setColor(new Color(10, 14, 20, 230));
        g.fillRect(bx, by, boxW, boxH);
        g.setColor(TEXT);
        g.drawRect(bx, by, boxW, boxH);
        g.setFont(this.bigFont);
        g.drawString(menu.title(), bx + (boxW - titleW) / 2, by + big.getAscent() + 16);
        g.setFont(this.font);
        int iy = by + big.getHeight() + 28;
        for (int i = 0; i < items.size(); ++i) {
            boolean sel = i == menu.selected();
            g.setColor(sel ? WARN : DIM);
            g.drawString((sel ? "> " : "  ") + items.get(i).label(), bx + 30, iy);
            iy += fm.getHeight() + 4;
        }
        g.setColor(DIM);
        String hint = "Z/S or arrows: navigate - Enter: confirm - ESC: back";
        g.drawString(hint, bx + (boxW - fm.stringWidth(hint)) / 2, by + boxH - 12);
    }
}
