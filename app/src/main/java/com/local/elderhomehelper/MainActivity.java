package com.local.elderhomehelper;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

public class MainActivity extends Activity {
    private AppWidgetManager appWidgetManager;
    private Spinner widthSpinner;
    private Spinner heightSpinner;
    private Button addShortcutButton;

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

        widthSpinner = findViewById(R.id.widthSpinner);
        heightSpinner = findViewById(R.id.heightSpinner);
        addShortcutButton = findViewById(R.id.addShortcutButton);
        initSpinners();
        addShortcutButton.setOnClickListener(v -> addEmptyShortcutWidget());
    }

    @Override
    protected void onResume() {
        super.onResume();
        ShortcutWidgetProvider.updateAll(this);
    }

    private void initSpinners() {
        String[] values = new String[]{"1 格", "2 格", "3 格", "4 格"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, values);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        widthSpinner.setAdapter(adapter);
        heightSpinner.setAdapter(adapter);
        widthSpinner.setSelection(1);
        heightSpinner.setSelection(1);
    }

    private void addEmptyShortcutWidget() {
        if (!appWidgetManager.isRequestPinAppWidgetSupported()) {
            Toast.makeText(this, "当前桌面不支持直接添加，请长按桌面进入小部件添加大应用入口", Toast.LENGTH_LONG).show();
            return;
        }

        addShortcutButton.setEnabled(false);
        int width = widthSpinner.getSelectedItemPosition() + 1;
        int height = heightSpinner.getSelectedItemPosition() + 1;
        ComponentName provider = WidgetComponentMap.shortcutComponent(this, width, height);
        boolean requested = appWidgetManager.requestPinAppWidget(provider, null, null);
        if (requested) {
            Toast.makeText(this, "请在桌面弹窗里确认。添加后点桌面上的“点我设置”。", Toast.LENGTH_LONG).show();
            finish();
        } else {
            addShortcutButton.setEnabled(true);
            Toast.makeText(this, "没有弹出确认窗口，请长按桌面手动添加小部件", Toast.LENGTH_LONG).show();
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
