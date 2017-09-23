package com.github.ykiselev.gfx.sprite.font.events;

import com.github.ykiselev.gfx.sprite.font.CharRange;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public final class BuildSprite {

    private final List<CharRange> ranges;

    private final char defaultCharacter;

    private final int glyphXBorder;

    private final int glyphYBorder;

    public List<CharRange> ranges() {
        return ranges;
    }

    public char defaultCharacter() {
        return defaultCharacter;
    }

    public int glyphXBorder() {
        return glyphXBorder;
    }

    public int glyphYBorder() {
        return glyphYBorder;
    }

    public BuildSprite(List<CharRange> ranges, char defaultCharacter, int glyphXBorder, int glyphYBorder) {
        this.ranges = requireNonNull(ranges);
        this.defaultCharacter = defaultCharacter;
        this.glyphXBorder = glyphXBorder;
        this.glyphYBorder = glyphYBorder;
    }
}
