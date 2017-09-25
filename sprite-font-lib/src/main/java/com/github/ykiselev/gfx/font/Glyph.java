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

package com.github.ykiselev.gfx.font;

import java.util.Objects;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public final class Glyph {

    private final char character;

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

    public Glyph(char character, int x, int y, int width) {
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