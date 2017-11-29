# Sprite Font Builder
## Synopsis

This is a Java FX based utility which allows you to convert any installed Font into Sprite Font, where Sprite Font is a structure consisting of png image containing glyphs (rasterized symbols) and technical information about glyphs bounding rectangles, font height, etc.

# How to use
## To build sprite font
Check out and build application using Maven. It uses Spring Boot plugin to package dependencies into single jar. Run jar as usual (Windows syntax here)
```text
java.exe -jar sprite-font-builder-<x.x>.jar
```
You should see window like this

![Font Settings tab](https://img-fotki.yandex.ru/get/762837/6633071.1d/0_a5712_50da7ad8_orig)

Here you see Font Settings tab where user can select font name from a list of available fonts, font weight, posture and size. Also there is a text box to enter sample text which will be rendered above (using selected attributes). If no text selected then font name is used as sample text.

Next tab gives a user ability to select character ranges for resulting sprite font.

![Character Ranges tab](https://img-fotki.yandex.ru/get/484172/6633071.1d/0_a5713_58fbfcdd_orig)

If user needs only a subset of all the characters in particular font, he or she can decrease resulting sprite size by selecting character range(s) of interest. Each range is inclusive and occupies one line in a list box. For example here user specified that he needs this character ranges:
1. From 32 to 126 inclusive.
2. 1025 - one char range.
3. From 1040 to 1105 inclusive.
4. 9650 - one char range.
5. 9660 - one char range.

Under the ranges list box user can select `Default character` - the character to print in the cases when the user need to show glyph not available in sprite font (it's merely a hint for user code really) and glyph's border width and height. 
`Border width` is amount of pixels to add from left and right to glyph bounding rectangle, while `Border height` is amount of pixels to add on top and bottom of the glyph bounding rectangle. This two values are used during painting font glyphs to font sprite.

And finally Bitmap tab showing the font sprite rendered using specified settings.

![Bitmap tab](https://img-fotki.yandex.ru/get/480022/6633071.1d/0_a5711_8a593a4b_orig)

Note: there is no special button like "rebuild bitmap" because program rebuilds it automatically when user selects Bitmap tab (if there were no bitmap rasterized before or some settings has changed) of selects Save As command from File menu (same rules as for Bitmap tab applied). 

Produced bitmap dimensions are always power of two (for example - 256x128) and it's width to height ratio limit is one (ratio → 1).

Next step is to export sprite font to file on disk. Use File-Save As command to save result as
1. Sprite font file (*.sf) - this will save complete sprite font objects including bitmap and additional information, needed to render glyphs in user program. It's Java Serialized form of class `com.github.ykiselev.gfx.font.SpriteFont` (more on the class below)
2. Sprite as a png picture (*.png) - this will save only sprite itself without additional information like glyph rectangles, etc.
3. Sprite font description (*.json) - this will save only additional information without sprite bitmap.

## To use sprite font
Add maven dependency to your project
```xml
<dependency>
    <groupId>com.github.ykiselev</groupId>
    <artifactId>sprite-font-lib</artifactId>
    <version>${sprite-font-lib.version}</version>
</dependency>
```
Put sprite font file in your program module's resource folder (for example resources/fonts/sample.sf). 

Deserialize binary data into instance of class `com.github.ykiselev.gfx.font.SpriteFont`:
```java
final SpriteFont result;
try (InputStream is = new FileInputStream("fonts/sample.sf"); ObjectInputStream ois = new ObjectInputStream(is)) {
    result = (SpriteFont) ois.readObject();
}
```
And use instance of `SpriteFont` class to extract bitmap and glyphs: 
```java
public final class SpriteFont implements Serializable {

    private final int fontHeight;

    private final int defaultCharacter;

    // If greater than 0, font is fixed pitch font
    private final int characterWidth;

    private final GlyphRange[] glyphs;

    // sprite, png image
    private final byte[] image;

    // Left and right glyph border size
    private final int glyphXBorder;

    // Top and bottom border size
    private final int glyphYBorder;
    
    // constructor and methods skipped
}
```
Then you can create for example OpenGL texture from bitmap. Glyphs array can be turned into Map<Character,GlGlyph> where GlGlyph is a user class containig texture coordinates for each glyph.

# License
This project is licensed under the Apache License, Version 2.0.