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
