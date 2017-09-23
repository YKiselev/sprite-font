package com.github.ykiselev.gfx.font;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public final class SpriteFont implements Serializable {

    private final int fontHeight;

    private final int defaultCharacterIndex;

    // If greater than 0, font is fixed pitch font
    private final int characterWidth;

    private final Glyph[] glyphs;

    private final byte[] bitmap;

    // Interval in pixels between text lines, i.e. line height = fontHeight + lineInterval
    private final int lineInterval;

    // Left and right glyph border size
    private final int glyphXBorder;

    // Top and bottom border size
    private final int glyphYBorder;

    public int fontHeight() {
        return fontHeight;
    }

    public int defaultCharacterIndex() {
        return defaultCharacterIndex;
    }

    public int characterWidth() {
        return characterWidth;
    }

    public Glyph[] glyphs() {
        return glyphs;
    }

    public byte[] bitmap() {
        return bitmap;
    }

    public int lineInterval() {
        return lineInterval;
    }

    public int glyphXBorder() {
        return glyphXBorder;
    }

    public int glyphYBorder() {
        return glyphYBorder;
    }

    public SpriteFont(int fontHeight, int defaultCharacterIndex, int characterWidth, Glyph[] glyphs, byte[] bitmap, int lineInterval, int glyphXBorder, int glyphYBorder) {
        this.fontHeight = fontHeight;
        this.defaultCharacterIndex = defaultCharacterIndex;
        this.characterWidth = characterWidth;
        this.glyphs = glyphs;
        this.bitmap = bitmap;
        this.lineInterval = lineInterval;
        this.glyphXBorder = glyphXBorder;
        this.glyphYBorder = glyphYBorder;
    }

    private Object writeReplace() throws ObjectStreamException {
        return new SpriteFontReplacement(this);
    }

    @Override
    public String toString() {
        return "SpriteFont{" +
                "fontHeight=" + fontHeight +
                ", defaultCharacterIndex=" + defaultCharacterIndex +
                ", characterWidth=" + characterWidth +
                ", lineInterval=" + lineInterval +
                ", glyphXBorder=" + glyphXBorder +
                ", glyphYBorder=" + glyphYBorder +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpriteFont that = (SpriteFont) o;
        return fontHeight == that.fontHeight &&
                defaultCharacterIndex == that.defaultCharacterIndex &&
                characterWidth == that.characterWidth &&
                lineInterval == that.lineInterval &&
                glyphXBorder == that.glyphXBorder &&
                glyphYBorder == that.glyphYBorder &&
                Arrays.equals(glyphs, that.glyphs) &&
                Arrays.equals(bitmap, that.bitmap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fontHeight, defaultCharacterIndex, characterWidth, glyphs, bitmap, lineInterval, glyphXBorder, glyphYBorder);
    }
}

final class SpriteFontReplacement implements Serializable {

    private static final long serialVersionUID = -4007521747869723305L;

    private int fontHeight;

    private int defaultCharacterIndex;

    private int characterWidth;

    private int[] glyphs;

    private byte[] bitmap;

    private int lineInterval;

    private int glyphXBorder;

    private int glyphYBorder;

    SpriteFontReplacement(SpriteFont font) {
        fontHeight = font.fontHeight();
        defaultCharacterIndex = font.defaultCharacterIndex();
        characterWidth = font.characterWidth();
        glyphs = toArray(font.glyphs());
        bitmap = font.bitmap();
        lineInterval = font.lineInterval();
        glyphXBorder = font.glyphXBorder();
        glyphYBorder = font.glyphYBorder();
    }

    Object readResolve() throws ObjectStreamException {
        return new SpriteFontBuilder()
                .withFontHeight(fontHeight)
                .withDefaultCharacterIndex(defaultCharacterIndex)
                .withCharacterWidth(characterWidth)
                .withGlyphs(toGlyphs(glyphs))
                .withBitmap(bitmap)
                .withLineInterval(lineInterval)
                .withGlyphXBorder(glyphXBorder)
                .withGlyphYBorder(glyphYBorder)
                .createSpriteFont();
    }

    private Glyph[] toGlyphs(int[] data) {
        final Glyph[] result = new Glyph[data.length >> 2];
        for (int i = 0; i < result.length; i++) {
            final int k = i * 4;
            result[i] = new Glyph(
                    data[k],
                    data[k + 1],
                    data[k + 2],
                    data[k + 3]
            );
        }
        return result;
    }

    private int[] toArray(Glyph[] glyphs) {
        final int[] data = new int[4 * glyphs.length];
        for (int i = 0; i < glyphs.length; i++) {
            final Glyph glyph = glyphs[i];
            final int k = i * 4;
            data[k] = glyph.character();
            data[k + 1] = glyph.x();
            data[k + 2] = glyph.y();
            data[k + 3] = glyph.width();
        }
        return data;
    }
}