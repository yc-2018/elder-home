package com.local.elderhomehelper;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ShortcutConfigureActivity extends Activity {
    private static final int REQUEST_IMAGE = 1001;
    private static final String EXTRA_LABEL = "label";
    private static final String EXTRA_PACKAGE = "package";
    private static final String EXTRA_ACTIVITY = "activity";
    private static final String EXTRA_URL_MODE = "url_mode";

    private ScrollView configScroll;
    private TextView titleText;
    private TextView chosenAppText;
    private TextView helpText;
    private EditText nameEdit;
    private EditText urlEdit;
    private LinearLayout urlPanel;
    private Spinner widthSpinner;
    private Spinner heightSpinner;
    private ImageView iconPreview;
    private Button addButton;
    private Button defaultIconButton;
    private Button chooseAppButton;

    private String label;
    private String packageName;
    private String activityName;
    private String iconPackageName;
    private String iconActivityName;
    private Bitmap selectedBitmap;
    private boolean customImageSelected;
    private boolean urlMode;
    private boolean isSaving;
    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    public static Intent createIntent(Context context, AppEntry app) {
        Intent intent = new Intent(context, ShortcutConfigureActivity.class);
        intent.putExtra(EXTRA_LABEL, app.label);
        intent.putExtra(EXTRA_PACKAGE, app.packageName);
        intent.putExtra(EXTRA_ACTIVITY, app.activityName);
        return intent;
    }

    public static Intent createUrlIntent(Context context) {
        Intent intent = new Intent(context, ShortcutConfigureActivity.class);
        intent.putExtra(EXTRA_URL_MODE, true);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);
        Window window = getWindow();
        window.setStatusBarColor(getResources().getColor(R.color.elder_bg));
        window.setNavigationBarColor(getResources().getColor(R.color.elder_bg));
        setContentView(R.layout.activity_configure_shortcut);

        configScroll = findViewById(R.id.configScroll);
        titleText = findViewById(R.id.titleText);
        chosenAppText = findViewById(R.id.chosenAppText);
        helpText = findViewById(R.id.helpText);
        nameEdit = findViewById(R.id.nameEdit);
        urlEdit = findViewById(R.id.urlEdit);
        urlPanel = findViewById(R.id.urlPanel);
        widthSpinner = findViewById(R.id.widthSpinner);
        heightSpinner = findViewById(R.id.heightSpinner);
        iconPreview = findViewById(R.id.iconPreview);
        chooseAppButton = findViewById(R.id.chooseAppButton);
        defaultIconButton = findViewById(R.id.defaultIconButton);
        Button appIconButton = findViewById(R.id.appIconButton);
        Button pickImageButton = findViewById(R.id.pickImageButton);
        addButton = findViewById(R.id.addButton);

        applySafeAreaPadding();
        initSpinners();
        readIntent();

        chooseAppButton.setOnClickListener(v -> showAppChooser());
        defaultIconButton.setOnClickListener(v -> useDefaultIcon());
        appIconButton.setOnClickListener(v -> showIconAppChooser());
        pickImageButton.setOnClickListener(v -> chooseImage());
        addButton.setOnClickListener(v -> addToDesktop());
    }

    private void readIntent() {
        Intent intent = getIntent();
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        urlMode = intent.getBooleanExtra(EXTRA_URL_MODE, false);
        label = intent.getStringExtra(EXTRA_LABEL);
        packageName = intent.getStringExtra(EXTRA_PACKAGE);
        activityName = intent.getStringExtra(EXTRA_ACTIVITY);

        if (urlMode) {
            titleText.setText("设置 URL 入口");
            chosenAppText.setText("当前类型：应用 URL");
            chooseAppButton.setVisibility(View.GONE);
            urlPanel.setVisibility(View.VISIBLE);
            nameEdit.setHint("小红书");
            defaultIconButton.setText("清空图标");
            addButton.setText("添加 URL 到桌面");
            helpText.setText("填写名称和 URL，再选择图标。URL 例子：xhsdiscover://search/result");
            iconPreview.setImageResource(R.drawable.ic_launcher_foreground);
            addButton.setEnabled(true);
            return;
        }

        if (label == null || packageName == null || activityName == null) {
            titleText.setText("设置桌面入口");
            chosenAppText.setText("当前应用：未选择");
            addButton.setEnabled(false);
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                addButton.setText("保存这个桌面入口");
                widthSpinner.setEnabled(false);
                heightSpinner.setEnabled(false);
                helpText.setText("请先选择应用。这个入口的大小已经由刚才选择的小部件尺寸决定。");
            }
            return;
        }

        applySelectedApp(label, packageName, activityName, true);

        if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            addButton.setText("保存这个桌面入口");
            widthSpinner.setEnabled(false);
            heightSpinner.setEnabled(false);
            helpText.setText("这个入口的大小已经由刚才选择的小部件尺寸决定。");
        }
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

    private void showAppChooser() {
        final List<AppEntry> apps = AppLoader.loadLaunchableApps(this);
        if (apps.isEmpty()) {
            Toast.makeText(this, "没有找到可启动应用", Toast.LENGTH_LONG).show();
            return;
        }

        InstalledAppAdapter adapter = new InstalledAppAdapter(this, apps);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("选择应用")
                .setAdapter(adapter, (dialogInterface, which) -> {
                    AppEntry app = apps.get(which);
                    applySelectedApp(app.label, app.packageName, app.activityName, true);
                })
                .create();
        dialog.show();
    }

    private void showIconAppChooser() {
        final List<AppEntry> apps = AppLoader.loadLaunchableApps(this);
        if (apps.isEmpty()) {
            Toast.makeText(this, "没有找到可选图标", Toast.LENGTH_LONG).show();
            return;
        }

        InstalledAppAdapter adapter = new InstalledAppAdapter(this, apps);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("选择图标")
                .setAdapter(adapter, (dialogInterface, which) -> {
                    AppEntry app = apps.get(which);
                    iconPackageName = app.packageName;
                    iconActivityName = app.activityName;
                    selectedBitmap = IconStore.drawableToBitmap(app.icon);
                    iconPreview.setImageBitmap(selectedBitmap);
                    customImageSelected = false;
                })
                .create();
        dialog.show();
    }

    private void applySelectedApp(String newLabel, String newPackageName, String newActivityName, boolean resetName) {
        label = newLabel;
        packageName = newPackageName;
        activityName = newActivityName;
        iconPackageName = newPackageName;
        iconActivityName = newActivityName;
        titleText.setText("设置 " + label);
        chosenAppText.setText("当前应用：" + label);
        if (resetName) {
            nameEdit.setText(label);
        }
        useDefaultIcon();
        addButton.setEnabled(true);
    }

    private void useDefaultIcon() {
        if (urlMode) {
            iconPackageName = null;
            iconActivityName = null;
            selectedBitmap = null;
            iconPreview.setImageResource(R.drawable.ic_launcher_foreground);
            customImageSelected = false;
            return;
        }
        loadIconFromApp(packageName, activityName);
    }

    private void loadIconFromApp(String iconPackage, String iconActivity) {
        try {
            Drawable icon = getPackageManager().getActivityIcon(new ComponentName(iconPackage, iconActivity));
            selectedBitmap = IconStore.drawableToBitmap(icon);
            iconPreview.setImageBitmap(selectedBitmap);
            customImageSelected = false;
        } catch (PackageManager.NameNotFoundException e) {
            selectedBitmap = null;
            iconPreview.setImageResource(R.drawable.ic_launcher_foreground);
            customImageSelected = false;
        }
    }

    private void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent, REQUEST_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_IMAGE || resultCode != RESULT_OK || data == null || data.getData() == null) {
            return;
        }
        Uri uri = data.getData();
        try {
            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (RuntimeException ignored) {
        }

        try {
            selectedBitmap = IconStore.loadBitmapFromUri(this, uri);
            iconPreview.setImageBitmap(selectedBitmap);
            customImageSelected = true;
            String displayName = IconStore.queryDisplayName(this, uri);
            if (displayName.length() > 0) {
                Toast.makeText(this, "已选择：" + displayName, Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Toast.makeText(this, "图片读取失败，已使用应用默认图标", Toast.LENGTH_LONG).show();
            useDefaultIcon();
        }
    }

    private void addToDesktop() {
        if (isSaving) {
            return;
        }
        if (!urlMode && (packageName == null || activityName == null)) {
            Toast.makeText(this, "没有可添加的应用", Toast.LENGTH_LONG).show();
            return;
        }

        String displayName = nameEdit.getText().toString().trim();
        if (displayName.length() == 0) {
            displayName = urlMode ? "URL 入口" : label;
        }

        String url = urlEdit.getText().toString().trim();
        if (urlMode && url.length() == 0) {
            Toast.makeText(this, "请先填写应用 URL", Toast.LENGTH_LONG).show();
            return;
        }

        isSaving = true;
        addButton.setEnabled(false);
        addButton.setText(appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID ? "正在保存..." : "正在添加...");

        String iconPath = null;
        if (selectedBitmap != null) {
            try {
                File iconFile = IconStore.saveBitmap(this, selectedBitmap, customImageSelected ? "custom" : "default");
                iconPath = iconFile.getAbsolutePath();
            } catch (IOException e) {
                Toast.makeText(this, "图标保存失败，将尝试使用默认图标", Toast.LENGTH_LONG).show();
            }
        }

        ShortcutPrefs.ShortcutData shortcutData = urlMode
                ? ShortcutPrefs.createUrl(displayName, url, iconPackageName, iconActivityName, iconPath)
                : ShortcutPrefs.create(packageName, activityName, displayName, iconPath);

        if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            saveExistingWidget(shortcutData);
            return;
        }

        pinNewWidget(shortcutData);
    }

    private void saveExistingWidget(ShortcutPrefs.ShortcutData shortcutData) {
        ShortcutPrefs.saveForWidget(this, appWidgetId, shortcutData);
        ShortcutPrefs.clearPending(this);
        AppWidgetManager manager = AppWidgetManager.getInstance(this);
        ShortcutWidgetProvider.updateWidget(this, manager, appWidgetId);
        Intent result = new Intent();
        result.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(RESULT_OK, result);
        Toast.makeText(this, "已保存桌面入口", Toast.LENGTH_LONG).show();
        finish();
    }

    private void pinNewWidget(ShortcutPrefs.ShortcutData shortcutData) {
        ShortcutPrefs.savePending(this, shortcutData);
        int width = widthSpinner.getSelectedItemPosition() + 1;
        int height = heightSpinner.getSelectedItemPosition() + 1;
        ComponentName provider = WidgetComponentMap.shortcutComponent(this, width, height);
        AppWidgetManager manager = AppWidgetManager.getInstance(this);

        if (!manager.isRequestPinAppWidgetSupported()) {
            Toast.makeText(this, "当前桌面不支持直接添加。请长按桌面，进入小部件，选择大应用入口。", Toast.LENGTH_LONG).show();
            isSaving = false;
            addButton.setEnabled(true);
            addButton.setText("添加到桌面");
            return;
        }

        Intent callbackIntent = new Intent(this, PinResultReceiver.class);
        callbackIntent.setAction(PinResultReceiver.ACTION_SHORTCUT_PINNED);
        PendingIntent successCallback = PendingIntent.getBroadcast(
                this,
                width * 10 + height,
                callbackIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        boolean requested = manager.requestPinAppWidget(provider, null, successCallback);
        if (requested) {
            Toast.makeText(this, "请在桌面弹窗里确认添加", Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(this, "没有弹出确认窗口，请长按桌面手动添加小部件", Toast.LENGTH_LONG).show();
            isSaving = false;
            addButton.setEnabled(true);
            addButton.setText("添加到桌面");
        }
    }

    private void applySafeAreaPadding() {
        configScroll.setOnApplyWindowInsetsListener((view, insets) -> {
            int side = dp(18);
            int top = side;
            int bottom = side;
            if (android.os.Build.VERSION.SDK_INT >= 23) {
                top += insets.getSystemWindowInsetTop();
                bottom += insets.getSystemWindowInsetBottom();
            }
            view.setPadding(0, top, 0, bottom);
            return insets;
        });
        configScroll.requestApplyInsets();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
