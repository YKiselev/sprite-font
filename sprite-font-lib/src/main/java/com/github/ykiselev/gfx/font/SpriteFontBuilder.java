package com.github.ykiselev.gfx.font;

public class SpriteFontBuilder {

    private int fontHeight;

    private int defaultCharacterIndex;

    private int characterWidth = 0;

    private Glyph[] glyphs;

    private byte[] bitmap;

    private int lineInterval;

    private int glyphXBorder;

    private int glyphYBorder;

    public SpriteFontBuilder withFontHeight(int fontHeight) {
        this.fontHeight = fontHeight;
        return this;
    }

    public SpriteFontBuilder withDefaultCharacterIndex(int defaultCharacterIndex) {
        this.defaultCharacterIndex = defaultCharacterIndex;
        return this;
    }

    public SpriteFontBuilder withCharacterWidth(int characterWidth) {
        this.characterWidth = characterWidth;
        return this;
    }

    public SpriteFontBuilder withGlyphs(Glyph[] glyphs) {
        this.glyphs = glyphs;
        return this;
    }

    public SpriteFontBuilder withBitmap(byte[] bitmap) {
        this.bitmap = bitmap;
        return this;
    }

    public SpriteFontBuilder withLineInterval(int lineInterval) {
        this.lineInterval = lineInterval;
        return this;
    }

    public SpriteFontBuilder withGlyphXBorder(int glyphXBorder) {
        this.glyphXBorder = glyphXBorder;
        return this;
    }

    public SpriteFontBuilder withGlyphYBorder(int glyphYBorder) {
        this.glyphYBorder = glyphYBorder;
        return this;
    }

    public SpriteFont createSpriteFont() {
        return new SpriteFont(fontHeight, defaultCharacterIndex, characterWidth, glyphs, bitmap, lineInterval, glyphXBorder, glyphYBorder);
    }
}