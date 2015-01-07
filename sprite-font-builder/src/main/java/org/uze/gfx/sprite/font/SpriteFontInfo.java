package org.uze.gfx.sprite.font;

/**
 * Created by Uze on 08.01.2015.
 */
public class SpriteFontInfo {

    private final char[] characters;

    private final int defaultCharacterIndex;

    private final Glyph[] glyphs;

    private final boolean isFixedPitch;

    private final int fontHeight;

    private final int characterWidth;

    public int getDefaultCharacterIndex() {
        return defaultCharacterIndex;
    }

    public boolean isFixedPitch() {
        return isFixedPitch;
    }

    public int getFontHeight() {
        return fontHeight;
    }

    public int getCharacterWidth() {
        return characterWidth;
    }

    public SpriteFontInfo(char[] characters, int defaultCharacterIndex, Glyph[] glyphs, boolean isFixedPitch, int fontHeight, int characterWidth) {
        this.characters = characters;
        this.defaultCharacterIndex = defaultCharacterIndex;
        this.glyphs = glyphs;
        this.isFixedPitch = isFixedPitch;
        this.fontHeight = fontHeight;
        this.characterWidth = characterWidth;
    }
}
