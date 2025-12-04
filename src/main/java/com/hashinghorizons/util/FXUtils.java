package com.hashinghorizons.util;

import javafx.scene.paint.Color;

public class FXUtils {
    // Palette (as Color objects)
    public static final Color BG_DARK_1 = Color.web("#1E201E");
    public static final Color BG_DARK_2 = Color.web("#3C3D37");
    public static final Color MUTED = Color.web("#697565");
    public static final Color ACCENT = Color.web("#ECDFCC");

    // utility: convert to hex string if needed
    public static String colorToCss(Color c) {
        return String.format("#%02X%02X%02X",
                (int) (c.getRed() * 255),
                (int) (c.getGreen() * 255),
                (int) (c.getBlue() * 255));
    }
}
