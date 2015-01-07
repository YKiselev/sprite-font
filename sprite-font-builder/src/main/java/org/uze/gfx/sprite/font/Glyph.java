package org.uze.gfx.sprite.font;

/**
 * Created by Uze on 07.01.2015.
 */
public class Glyph {

    private final int x;

    private final int y;

    private final int width;

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public Glyph(int x, int y, int width) {
        this.x = x;
        this.y = y;
        this.width = width;
    }
}
