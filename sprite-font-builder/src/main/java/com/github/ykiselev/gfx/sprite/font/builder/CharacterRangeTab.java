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

package com.github.ykiselev.gfx.sprite.font.builder;

import com.github.ykiselev.gfx.sprite.font.CharRange;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValueFactory;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public final class CharacterRangeTab implements BuilderTab {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Tab tab = new Tab("Character ranges");

    private final TextArea charRanges = new TextArea("32-126\n1025\n1040-1105\n9650\n9660");

    private final TextField defaultCharacterField = new TextField("?");

    private TextField borderWidthField = new TextField("0");

    private TextField borderHeightField = new TextField("0");

    @Override
    public Tab tab() {
        return tab;
    }

    public CharacterRangeTab() {
        tab.setClosable(false);
        final VBox vbox = new VBox(4.0);
        vbox.setPadding(new Insets(4.0));
        tab.setContent(vbox);
        charRanges.setEditable(true);
        charRanges.setMinHeight(120.0);
        defaultCharacterField.setMaxWidth(30.0);
        borderWidthField.setMaxWidth(30.0);
        borderHeightField.setMaxWidth(30.0);

        vbox.getChildren().addAll(
                new Label("Character ranges:"), charRanges,
                new HBox(
                        4.0,
                        new Label("Default character:"), defaultCharacterField,
                        new Label("Border width:"), borderWidthField,
                        new Label("Border height:"), borderHeightField
                )
        );
    }

    @Override
    public void load(Config state) {
        try {
            charRanges.setText(String.join("\n", state.getStringList("glyph.character-ranges")));
        } catch (Exception ex) {
            logger.error("Failed to load character ranges!", ex);
        }
        try {
            final String value = state.getString("glyph.default-character");
            if (value != null && value.length() != 1) {
                logger.error("Invalid default character: \"{}\"", value);
            } else {
                defaultCharacterField.setText(value);
            }
        } catch (Exception ex) {
            logger.error("Failed to load default character!", ex);
        }
        try {
            borderWidthField.setText(state.getString("glyph.border.width"));
        } catch (Exception ex) {
            logger.error("Failed to load glyph border width!", ex);
        }
        try {
            borderHeightField.setText(state.getString("glyph.border.height"));
        } catch (Exception ex) {
            logger.error("Failed to load glyph border height!", ex);
        }
    }

    @Override
    public Config save(Config config) {
        try {
            return config.withValue("glyph.character-ranges",
                    ConfigValueFactory.fromIterable(Arrays.asList(StringUtils.split(charRanges.getText(), "[\\r\\n]+")))
            ).withValue("glyph.default-character", ConfigValueFactory.fromAnyRef(defaultCharacterField.getText()))
                    .withValue("glyph.border.width", ConfigValueFactory.fromAnyRef(borderWidthField.getText()))
                    .withValue("glyph.border.height", ConfigValueFactory.fromAnyRef(borderHeightField.getText()));
        } catch (Exception ex) {
            logger.error("Failed to save config!", ex);
        }
        return config;
    }

    public char getDefaultCharacter() {
        final String value = defaultCharacterField.getText();
        if (StringUtils.isEmpty(value) || value.length() != 1) {
            throw new IllegalArgumentException("Bad default character: " + value);
        }
        return value.charAt(0);
    }

    public int getGlyphBorderWidth() {
        String value = borderWidthField.getText();
        if (StringUtils.isEmpty(value)) {
            value = "0";
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid glyph border width: " + value);
        }
    }

    public int getGlyphBorderHeight() {
        String value = borderHeightField.getText();
        if (StringUtils.isEmpty(value)) {
            value = "0";
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid glyph border height: " + value);
        }
    }

    private char toChar(int value) {
        if (value < Character.MIN_VALUE || value > Character.MAX_VALUE) {
            throw new IllegalArgumentException("Bad character: " + value);
        }
        return (char) value;
    }

    public List<CharRange> getCharRanges() {
        final String text = charRanges.getText();
        if (StringUtils.isEmpty(text)) {
            return Collections.emptyList();
        }

        final List<CharRange> result = new ArrayList<>();
        final String[] lines = text.split("\\r?\\n|\\r|,");
        for (String line : lines) {
            if (StringUtils.isEmpty(line)) {
                continue;
            }
            final String[] parts = line.split("-");
            if (parts.length == 2) {
                result.add(new CharRange(toChar(Integer.parseInt(parts[0])), toChar(Integer.parseInt(parts[1]))));
            } else if (parts.length == 1) {
                final char start = toChar(Integer.parseInt(parts[0]));

                result.add(new CharRange(start, start));
            } else {
                throw new IllegalArgumentException("Bad range: " + line);
            }
        }

        return result;
    }

}
