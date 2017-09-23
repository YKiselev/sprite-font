package com.github.ykiselev.gfx.sprite.font;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Created by Uze on 07.01.2015.
 */
public final class CharRange implements Supplier<List<Character>> {

    private final char start;

    private final char end;

    public int getLength() {
        return end - start + 1;
    }

    public CharRange(char start, char end) {
        if (start > end) {
            throw new IllegalArgumentException("start > end");
        }
        this.start = start;
        this.end = end;
    }

    @Override
    public List<Character> get() {
        final List<Character> result = new ArrayList<>(getLength());
        for (char i = start; i <= end; i++) {
            result.add(i);
        }
        return result;
    }
}
