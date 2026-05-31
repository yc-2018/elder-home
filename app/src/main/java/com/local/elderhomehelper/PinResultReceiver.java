package com.local.elderhomehelper;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PinResultReceiver extends BroadcastReceiver {
    public static final String ACTION_SHORTCUT_PINNED = "com.local.elderhomehelper.SHORTCUT_PINNED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!ACTION_SHORTCUT_PINNED.equals(intent.getAction())) {
            return;
        }

        int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            return;
        }

        ShortcutPrefs.ShortcutData pending = ShortcutPrefs.loadPending(context);
        if (pending == null) {
            return;
        }

        ShortcutPrefs.saveForWidget(context, appWidgetId, pending);
        ShortcutPrefs.clearPending(context);
        ShortcutWidgetProvider.updateWidget(context, AppWidgetManager.getInstance(context), appWidgetId);
    }
}
