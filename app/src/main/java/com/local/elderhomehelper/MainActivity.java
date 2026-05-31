package com.local.elderhomehelper;

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends Activity {
    private TextView statusText;
    private ListView appList;
    private AppWidgetManager appWidgetManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        appWidgetManager = AppWidgetManager.getInstance(this);
        LinearLayout mainRoot = findViewById(R.id.mainRoot);
        mainRoot.setOnApplyWindowInsetsListener((view, insets) -> {
            int side = dp(18);
            int top = side;
            int bottom = side;
            if (android.os.Build.VERSION.SDK_INT >= 23) {
                top += insets.getSystemWindowInsetTop();
                bottom += insets.getSystemWindowInsetBottom();
            }
            view.setPadding(side, top, side, bottom);
            return insets;
        });
        mainRoot.requestApplyInsets();
        statusText = findViewById(R.id.statusText);
        appList = findViewById(R.id.appList);
        Button addBatteryButton = findViewById(R.id.addBatteryButton);
        Button addClockButton = findViewById(R.id.addClockButton);
        Button addUrlButton = findViewById(R.id.addUrlButton);
        addBatteryButton.setOnClickListener(v -> requestPinWidget(BatteryWidgetProvider.class, "大电量"));
        addClockButton.setOnClickListener(v -> requestPinWidget(ClockWidgetProvider.class, "大钟表"));
        addUrlButton.setOnClickListener(v -> startActivity(ShortcutConfigureActivity.createUrlIntent(this)));
        loadApps();
    }

    @Override
    protected void onResume() {
        super.onResume();
        BatteryWidgetProvider.updateAll(this);
        ShortcutWidgetProvider.updateAll(this);
    }

    private void loadApps() {
        final List<AppEntry> apps = AppLoader.loadLaunchableApps(this);
        statusText.setText("共找到 " + apps.size() + " 个应用，点一个设置大图标");
        InstalledAppAdapter adapter = new InstalledAppAdapter(this, apps);
        appList.setAdapter(adapter);
        appList.setOnItemClickListener((parent, view, position, id) -> {
            AppEntry app = apps.get(position);
            Intent intent = ShortcutConfigureActivity.createIntent(this, app);
            startActivity(intent);
        });
    }

    private void requestPinWidget(Class<?> providerClass, String name) {
        if (!appWidgetManager.isRequestPinAppWidgetSupported()) {
            Toast.makeText(this, "当前桌面不支持直接添加，请长按桌面进入小部件添加" + name, Toast.LENGTH_LONG).show();
            return;
        }

        Intent callbackIntent = new Intent(this, MainActivity.class);
        PendingIntent callback = PendingIntent.getActivity(
                this,
                providerClass.getName().hashCode(),
                callbackIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        boolean requested = appWidgetManager.requestPinAppWidget(new ComponentName(this, providerClass), null, callback);
        if (requested) {
            Toast.makeText(this, "请在桌面弹窗里确认添加" + name, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "没有弹出确认窗口，请长按桌面手动添加小部件", Toast.LENGTH_LONG).show();
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
