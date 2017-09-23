package com.github.ykiselev.gfx.sprite.font.events;

import com.typesafe.config.Config;

import static java.util.Objects.requireNonNull;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public final class LoadConfig {

    private final Config config;

    public Config config() {
        return config;
    }

    public LoadConfig(Config config) {
        this.config = requireNonNull(config);
    }
}
