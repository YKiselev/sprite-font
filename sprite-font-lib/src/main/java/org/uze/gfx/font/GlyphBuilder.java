package org.uze.gfx.font;

public final class GlyphBuilder {

    private int character;

    private int x;

    private int y;

    private int width = 0;

    public GlyphBuilder withCharacter(int character) {
        this.character = character;
        return this;
    }

    public GlyphBuilder withX(int x) {
        this.x = x;
        return this;
    }

    public GlyphBuilder withY(int y) {
        this.y = y;
        return this;
    }

    public GlyphBuilder withWidth(int width) {
        this.width = width;
        return this;
    }

    public Glyph createGlyph() {
        return new Glyph(character, x, y, width);
    }

    public GlyphBuilder clear() {
        character = x = y = width = 0;
        return this;
    }
}