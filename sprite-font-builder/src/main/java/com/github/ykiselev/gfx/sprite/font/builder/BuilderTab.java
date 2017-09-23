package com.github.ykiselev.gfx.sprite.font.builder;

import com.typesafe.config.Config;
import javafx.scene.control.Tab;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public interface BuilderTab {

    Tab tab();

    void load(Config config);

    Config save(Config config);

}
