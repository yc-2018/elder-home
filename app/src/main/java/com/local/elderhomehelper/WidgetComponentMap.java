package com.local.elderhomehelper;

import android.content.ComponentName;
import android.content.Context;

public final class WidgetComponentMap {
    private WidgetComponentMap() {
    }

    public static ComponentName shortcutComponent(Context context, int width, int height) {
        int safeWidth = clamp(width);
        int safeHeight = clamp(height);
        Class<?> cls;
        if (safeWidth == 1 && safeHeight == 1) cls = ShortcutWidget1x1.class;
        else if (safeWidth == 1 && safeHeight == 2) cls = ShortcutWidget1x2.class;
        else if (safeWidth == 1 && safeHeight == 3) cls = ShortcutWidget1x3.class;
        else if (safeWidth == 1 && safeHeight == 4) cls = ShortcutWidget1x4.class;
        else if (safeWidth == 2 && safeHeight == 1) cls = ShortcutWidget2x1.class;
        else if (safeWidth == 2 && safeHeight == 2) cls = ShortcutWidget2x2.class;
        else if (safeWidth == 2 && safeHeight == 3) cls = ShortcutWidget2x3.class;
        else if (safeWidth == 2 && safeHeight == 4) cls = ShortcutWidget2x4.class;
        else if (safeWidth == 3 && safeHeight == 1) cls = ShortcutWidget3x1.class;
        else if (safeWidth == 3 && safeHeight == 2) cls = ShortcutWidget3x2.class;
        else if (safeWidth == 3 && safeHeight == 3) cls = ShortcutWidget3x3.class;
        else if (safeWidth == 3 && safeHeight == 4) cls = ShortcutWidget3x4.class;
        else if (safeWidth == 4 && safeHeight == 1) cls = ShortcutWidget4x1.class;
        else if (safeWidth == 4 && safeHeight == 2) cls = ShortcutWidget4x2.class;
        else if (safeWidth == 4 && safeHeight == 3) cls = ShortcutWidget4x3.class;
        else cls = ShortcutWidget4x4.class;
        return new ComponentName(context, cls);
    }

    private static int clamp(int value) {
        if (value < 1) {
            return 1;
        }
        if (value > 4) {
            return 4;
        }
        return value;
    }
}
