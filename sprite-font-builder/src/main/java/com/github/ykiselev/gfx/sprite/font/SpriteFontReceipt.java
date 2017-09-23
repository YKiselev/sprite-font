package com.github.ykiselev.gfx.sprite.font;

import javafx.scene.text.Font;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public final class SpriteFontReceipt {

    private final Font font;

    private final List<CharRange> ranges;

    private final char defaultCharacter;

    private final int glyphXBorder;

    private final int glyphYBorder;

    public SpriteFontReceipt(Font font, List<CharRange> ranges, char defaultCharacter, int glyphXBorder, int glyphYBorder) {
        this.font = font;
        this.ranges = ranges;
        this.defaultCharacter = defaultCharacter;
        this.glyphXBorder = glyphXBorder;
        this.glyphYBorder = glyphYBorder;
    }

    public SpriteFontAndImage build() {
        final Set<Character> uniqueCharacters = ranges.stream()
                .map(CharRange::get)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        uniqueCharacters.add(defaultCharacter);
        final char[] chars = new char[uniqueCharacters.size()];
        int i = 0;
        for (Character ch : uniqueCharacters) {
            chars[i] = ch;
            i++;
        }
        Arrays.sort(chars);
        final int defaultCharacterIndex = Arrays.binarySearch(chars, defaultCharacter);
        return new FontRasterizer(font, chars, defaultCharacterIndex, glyphXBorder, glyphYBorder).build();
    }

}
