package com.local.elderhomehelper;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.appwidget.AppWidgetProvider;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.File;

public class ShortcutWidgetProvider extends AppWidgetProvider {
    public static final String ACTION_OPEN_SHORTCUT = "com.local.elderhomehelper.OPEN_SHORTCUT";
    public static final String EXTRA_WIDGET_ID = "widget_id";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId, getClass());
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            ShortcutPrefs.deleteWidget(context, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (ACTION_OPEN_SHORTCUT.equals(intent.getAction())) {
            int widgetId = intent.getIntExtra(EXTRA_WIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            openShortcut(context, widgetId);
        }
    }

    public static void updateWidget(Context context, AppWidgetManager manager, int appWidgetId) {
        updateWidget(context, manager, appWidgetId, receiverClassForWidget(context, manager, appWidgetId));
    }

    public static void updateWidget(Context context, AppWidgetManager manager, int appWidgetId, Class<?> receiverClass) {
        ShortcutPrefs.ShortcutData data = ShortcutPrefs.loadForWidget(context, appWidgetId);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_app_shortcut);

        if (data == null) {
            views.setTextViewText(R.id.shortcutName, "点我设置");
            views.setImageViewResource(R.id.shortcutIcon, R.drawable.ic_launcher_foreground);
        } else {
            views.setTextViewText(R.id.shortcutName, data.label);
            Bitmap bitmap = loadIcon(context, data);
            if (bitmap != null) {
                views.setImageViewBitmap(R.id.shortcutIcon, bitmap);
            } else {
                views.setImageViewResource(R.id.shortcutIcon, R.drawable.ic_launcher_foreground);
            }
        }

        Intent clickIntent = new Intent(context, receiverClass);
        clickIntent.setAction(ACTION_OPEN_SHORTCUT);
        clickIntent.putExtra(EXTRA_WIDGET_ID, appWidgetId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId,
                clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        views.setOnClickPendingIntent(R.id.appShortcutRoot, pendingIntent);
        manager.updateAppWidget(appWidgetId, views);
    }

    public static void updateAll(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        for (Class<?> cls : allShortcutProviders()) {
            int[] ids = manager.getAppWidgetIds(new ComponentName(context, cls));
            for (int id : ids) {
                updateWidget(context, manager, id, cls);
            }
        }
    }

    private static void openShortcut(Context context, int appWidgetId) {
        ShortcutPrefs.ShortcutData data = ShortcutPrefs.loadForWidget(context, appWidgetId);
        if (data == null) {
            Intent configureIntent = new Intent(context, ShortcutConfigureActivity.class);
            configureIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            configureIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            context.startActivity(configureIntent);
            return;
        }

        Intent launchIntent;
        if (ShortcutPrefs.TYPE_URL.equals(data.type)) {
            launchIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(data.url));
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                context.startActivity(launchIntent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(context, "没有应用能打开这个 URL", Toast.LENGTH_LONG).show();
            }
            return;
        } else {
            launchIntent = new Intent(Intent.ACTION_MAIN);
            launchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            launchIntent.setComponent(new ComponentName(data.packageName, data.activityName));
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        }

        PackageManager packageManager = context.getPackageManager();
        if (launchIntent.resolveActivity(packageManager) == null) {
            Toast.makeText(context, "这个应用可能已卸载", Toast.LENGTH_LONG).show();
            return;
        }
        context.startActivity(launchIntent);
    }

    private static Bitmap loadIcon(Context context, ShortcutPrefs.ShortcutData data) {
        if (data.iconPath != null) {
            File file = new File(data.iconPath);
            if (file.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                if (bitmap != null) {
                    return bitmap;
                }
            }
        }

        String iconPackage = data.iconPackageName != null ? data.iconPackageName : data.packageName;
        String iconActivity = data.iconActivityName != null ? data.iconActivityName : data.activityName;
        if (iconPackage == null || iconActivity == null) {
            return null;
        }

        try {
            Drawable drawable = context.getPackageManager().getActivityIcon(
                    new ComponentName(iconPackage, iconActivity)
            );
            Bitmap bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    private static Class<?> receiverClassForWidget(Context context, AppWidgetManager manager, int appWidgetId) {
        AppWidgetProviderInfo info = manager.getAppWidgetInfo(appWidgetId);
        if (info != null && info.provider != null) {
            try {
                return Class.forName(info.provider.getClassName());
            } catch (ClassNotFoundException ignored) {
            }
        }
        return ShortcutWidget1x1.class;
    }

    private static Class<?>[] allShortcutProviders() {
        return new Class<?>[]{
                ShortcutWidget1x1.class, ShortcutWidget1x2.class, ShortcutWidget1x3.class, ShortcutWidget1x4.class,
                ShortcutWidget2x1.class, ShortcutWidget2x2.class, ShortcutWidget2x3.class, ShortcutWidget2x4.class,
                ShortcutWidget3x1.class, ShortcutWidget3x2.class, ShortcutWidget3x3.class, ShortcutWidget3x4.class,
                ShortcutWidget4x1.class, ShortcutWidget4x2.class, ShortcutWidget4x3.class, ShortcutWidget4x4.class
        };
    }
}
