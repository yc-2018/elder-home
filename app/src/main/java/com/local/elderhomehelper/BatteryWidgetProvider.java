package com.local.elderhomehelper;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.widget.RemoteViews;

public class BatteryWidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        updateAll(context);
    }

    public static void updateAll(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int[] ids = manager.getAppWidgetIds(new ComponentName(context, BatteryWidgetProvider.class));
        for (int id : ids) {
            updateWidget(context, manager, id);
        }
    }

    private static void updateWidget(Context context, AppWidgetManager manager, int appWidgetId) {
        int percent = readBatteryPercent(context);
        int activeSegments = Math.max(1, (int) Math.ceil(percent / 20.0));
        if (percent <= 0) {
            activeSegments = 0;
        }

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_battery);
        views.setTextViewText(R.id.batteryPercent, percent >= 0 ? percent + "%" : "--%");
        int[] segmentIds = new int[]{
                R.id.batterySegment1,
                R.id.batterySegment2,
                R.id.batterySegment3,
                R.id.batterySegment4,
                R.id.batterySegment5
        };
        for (int i = 0; i < segmentIds.length; i++) {
            views.setInt(
                    segmentIds[i],
                    "setBackgroundResource",
                    i < activeSegments ? R.drawable.battery_segment_on : R.drawable.battery_segment_off
            );
        }
        manager.updateAppWidget(appWidgetId, views);
    }

    private static int readBatteryPercent(Context context) {
        BatteryManager batteryManager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
        if (batteryManager == null) {
            return -1;
        }
        int percent = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        if (percent < 0 || percent > 100) {
            return -1;
        }
        return percent;
    }
}
