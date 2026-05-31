package com.local.elderhomehelper;

import android.content.Context;
import android.content.SharedPreferences;

public final class ShortcutPrefs {
    private static final String PREFS = "shortcut_widgets";
    private static final String PENDING_ID = "pending";
    public static final String TYPE_APP = "app";
    public static final String TYPE_URL = "url";

    private ShortcutPrefs() {
    }

    public static ShortcutData create(String packageName, String activityName, String label, String iconPath) {
        ShortcutData data = new ShortcutData();
        data.type = TYPE_APP;
        data.packageName = packageName;
        data.activityName = activityName;
        data.label = label;
        data.iconPath = iconPath;
        return data;
    }

    public static ShortcutData createUrl(String label, String url, String iconPackageName, String iconActivityName, String iconPath) {
        ShortcutData data = new ShortcutData();
        data.type = TYPE_URL;
        data.label = label;
        data.url = url;
        data.iconPackageName = iconPackageName;
        data.iconActivityName = iconActivityName;
        data.iconPath = iconPath;
        return data;
    }

    public static void savePending(Context context, ShortcutData data) {
        save(context, PENDING_ID, data);
    }

    public static ShortcutData loadPending(Context context) {
        return load(context, PENDING_ID);
    }

    public static void clearPending(Context context) {
        delete(context, PENDING_ID);
    }

    public static void saveForWidget(Context context, int appWidgetId, ShortcutData data) {
        save(context, String.valueOf(appWidgetId), data);
    }

    public static ShortcutData loadForWidget(Context context, int appWidgetId) {
        return load(context, String.valueOf(appWidgetId));
    }

    public static void deleteWidget(Context context, int appWidgetId) {
        delete(context, String.valueOf(appWidgetId));
    }

    private static void delete(Context context, String id) {
        SharedPreferences.Editor editor = prefs(context).edit();
        String prefix = key(id, "");
        editor.remove(prefix + "packageName");
        editor.remove(prefix + "activityName");
        editor.remove(prefix + "label");
        editor.remove(prefix + "iconPath");
        editor.remove(prefix + "type");
        editor.remove(prefix + "url");
        editor.remove(prefix + "iconPackageName");
        editor.remove(prefix + "iconActivityName");
        editor.apply();
    }

    private static void save(Context context, String id, ShortcutData data) {
        prefs(context).edit()
                .putString(key(id, "type"), data.type)
                .putString(key(id, "packageName"), data.packageName)
                .putString(key(id, "activityName"), data.activityName)
                .putString(key(id, "label"), data.label)
                .putString(key(id, "iconPath"), data.iconPath)
                .putString(key(id, "url"), data.url)
                .putString(key(id, "iconPackageName"), data.iconPackageName)
                .putString(key(id, "iconActivityName"), data.iconActivityName)
                .apply();
    }

    private static ShortcutData load(Context context, String id) {
        SharedPreferences sharedPreferences = prefs(context);
        String type = sharedPreferences.getString(key(id, "type"), TYPE_APP);
        String packageName = sharedPreferences.getString(key(id, "packageName"), null);
        String activityName = sharedPreferences.getString(key(id, "activityName"), null);
        String label = sharedPreferences.getString(key(id, "label"), null);
        String iconPath = sharedPreferences.getString(key(id, "iconPath"), null);
        String url = sharedPreferences.getString(key(id, "url"), null);
        String iconPackageName = sharedPreferences.getString(key(id, "iconPackageName"), null);
        String iconActivityName = sharedPreferences.getString(key(id, "iconActivityName"), null);
        if (label == null) {
            return null;
        }
        if (TYPE_URL.equals(type)) {
            if (url == null) {
                return null;
            }
            ShortcutData data = createUrl(label, url, iconPackageName, iconActivityName, iconPath);
            return data;
        }
        if (packageName == null || activityName == null) {
            return null;
        }
        return create(packageName, activityName, label, iconPath);
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    private static String key(String id, String field) {
        return "widget_" + id + "_" + field;
    }

    public static class ShortcutData {
        public String type;
        public String packageName;
        public String activityName;
        public String label;
        public String iconPath;
        public String url;
        public String iconPackageName;
        public String iconActivityName;
    }
}
