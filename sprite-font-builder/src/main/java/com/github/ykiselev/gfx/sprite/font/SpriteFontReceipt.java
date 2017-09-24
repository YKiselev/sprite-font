/*
 * Copyright 2017 Yuriy Kiselev (uze@yandex.ru)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.ykiselev.gfx.sprite.font;

import javafx.scene.text.Font;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
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
        return new FontRasterizer(
                font,
                chars,
                Arrays.binarySearch(chars, defaultCharacter),
                glyphXBorder,
                glyphYBorder
        ).build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpriteFontReceipt that = (SpriteFontReceipt) o;
        return defaultCharacter == that.defaultCharacter &&
                glyphXBorder == that.glyphXBorder &&
                glyphYBorder == that.glyphYBorder &&
                Objects.equals(font, that.font) &&
                Objects.equals(ranges, that.ranges);
    }

    @Override
    public int hashCode() {
        return Objects.hash(font, ranges, defaultCharacter, glyphXBorder, glyphYBorder);
    }

    @Override
    public String toString() {
        return "SpriteFontReceipt{" +
                "font=" + font +
                ", ranges=" + ranges +
                ", defaultCharacter=" + defaultCharacter +
                ", glyphXBorder=" + glyphXBorder +
                ", glyphYBorder=" + glyphYBorder +
                '}';
    }
}
