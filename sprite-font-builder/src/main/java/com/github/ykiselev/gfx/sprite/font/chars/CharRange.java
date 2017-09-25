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

package com.github.ykiselev.gfx.sprite.font.chars;

import java.util.Comparator;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Created by Uze on 07.01.2015.
 */
public final class CharRange implements Supplier<char[]>, Comparable<CharRange> {

    private static final Comparator<CharRange> COMPARATOR = Comparator.comparing(CharRange::start)
            .thenComparing(CharRange::end);

    private final char start;

    private final char end;

    public char start() {
        return start;
    }

    public char end() {
        return end;
    }

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
    public char[] get() {
        final char[] result = new char[getLength()];
        for (char i = start; i <= end; i++) {
            result[i - start] = i;
        }
        return result;
    }

    @Override
    public int compareTo(CharRange o) {
        return COMPARATOR.compare(this, o);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CharRange charRange = (CharRange) o;
        return start == charRange.start &&
                end == charRange.end;
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }

    @Override
    public String toString() {
        return "CharRange{" +
                "start=" + start +
                ", end=" + end +
                '}';
    }

    /**
     * Tries to merge two ranges.
     *
     * @param r the other range
     * @return the new merged range or {@code null}
     */
    public CharRange join(CharRange r) {
        // before this
        if (r.end < start) {
            return null;
        }
        // after this
        if (r.start > end) {
            return null;
        }
        // this range starts before or with other
        if (start <= r.start) {
            // this range completely covers other
            if (end >= r.end) {
                return this;
            } else {
                return new CharRange(start, r.end);
            }
        }
        return r.join(this);
    }
}
