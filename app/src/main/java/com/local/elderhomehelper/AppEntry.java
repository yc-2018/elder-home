package com.local.elderhomehelper;

import android.graphics.drawable.Drawable;

public class AppEntry {
    public final String label;
    public final String packageName;
    public final String activityName;
    public final Drawable icon;

    public AppEntry(String label, String packageName, String activityName, Drawable icon) {
        this.label = label;
        this.packageName = packageName;
        this.activityName = activityName;
        this.icon = icon;
    }
}
