package com.github.ykiselev.gfx.font;

import java.util.Objects;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public final class Glyph {

    private final int character;

    private final int x;

    private final int y;

    private final int width;

    public int character() {
        return character;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public int width() {
        return width;
    }

    public Glyph(int character, int x, int y, int width) {
        this.character = character;
        this.x = x;
        this.y = y;
        this.width = width;
    }

    @Override
    public String toString() {
        return "Glyph{" +
                "character=" + character +
                ", x=" + x +
                ", y=" + y +
                ", width=" + width +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Glyph glyph = (Glyph) o;
        return character == glyph.character &&
                x == glyph.x &&
                y == glyph.y &&
                width == glyph.width;
    }

    @Override
    public int hashCode() {
        return Objects.hash(character, x, y, width);
    }
}