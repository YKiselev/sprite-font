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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public class CharRangeTest {

    @Test
    public void shouldJoinConsecutive() throws Exception {
        final CharRange r1 = new CharRange('a', 'b');
        final CharRange r2 = new CharRange('b', 'c');
        final CharRange expected = new CharRange('a', 'c');
        assertEquals(expected, r1.join(r2));
        assertEquals(expected, r2.join(r1));
    }

    @Test
    public void shouldJoinIntersected() throws Exception {
        final CharRange r1 = new CharRange('a', 'c');
        final CharRange r2 = new CharRange('b', 'd');
        final CharRange expected = new CharRange('a', 'd');
        assertEquals(expected, r1.join(r2));
        assertEquals(expected, r2.join(r1));
    }

    @Test
    public void shouldEliminateOtherEqual() throws Exception {
        final CharRange r1 = new CharRange('a', 'b');
        final CharRange r2 = new CharRange('a', 'b');
        assertEquals(r1, r1.join(r2));
        assertEquals(r2, r2.join(r1));
    }

    @Test
    public void shouldEliminateOtherSmaller() throws Exception {
        final CharRange r1 = new CharRange('a', 'd');
        final CharRange r2 = new CharRange('b', 'c');
        assertEquals(r1, r1.join(r2));
        assertEquals(r1, r2.join(r1));
    }

    @Test
    public void shouldNotJoin() throws Exception {
        final CharRange r1 = new CharRange('a', 'b');
        final CharRange r2 = new CharRange('c', 'd');
        assertNull(r1.join(r2));
        assertNull(r2.join(r1));
    }

}