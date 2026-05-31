package com.local.elderhomehelper;

import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

    private ScrollView configScroll;
    private TextView titleText;
    private TextView chosenAppText;
    private TextView helpText;
    private TextView sizeTitle;
    private EditText nameEdit;
    private EditText urlEdit;
    private LinearLayout urlPanel;
    private LinearLayout sizeRow;
    private Spinner widthSpinner;
    private Spinner heightSpinner;
    private ImageView iconPreview;
    private Button addButton;
    private Button defaultIconButton;
    private Button chooseAppButton;
    private Button urlModeButton;

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
        sizeTitle = findViewById(R.id.sizeTitle);
        nameEdit = findViewById(R.id.nameEdit);
        urlEdit = findViewById(R.id.urlEdit);
        urlPanel = findViewById(R.id.urlPanel);
        sizeRow = findViewById(R.id.sizeRow);
        widthSpinner = findViewById(R.id.widthSpinner);
        heightSpinner = findViewById(R.id.heightSpinner);
        iconPreview = findViewById(R.id.iconPreview);
        chooseAppButton = findViewById(R.id.chooseAppButton);
        urlModeButton = findViewById(R.id.urlModeButton);
        defaultIconButton = findViewById(R.id.defaultIconButton);
        Button appIconButton = findViewById(R.id.appIconButton);
        Button pickImageButton = findViewById(R.id.pickImageButton);
        addButton = findViewById(R.id.addButton);

        applySafeAreaPadding();
        initSpinners();
        readIntent();

        chooseAppButton.setOnClickListener(v -> showAppChooser());
        urlModeButton.setOnClickListener(v -> enableUrlMode());
        defaultIconButton.setOnClickListener(v -> useDefaultIcon());
        appIconButton.setOnClickListener(v -> showIconAppChooser());
        pickImageButton.setOnClickListener(v -> chooseImage());
        addButton.setOnClickListener(v -> saveCurrentWidget());
    }

    private void readIntent() {
        Intent intent = getIntent();
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

        sizeTitle.setVisibility(View.GONE);
        sizeRow.setVisibility(View.GONE);
        widthSpinner.setEnabled(false);
        heightSpinner.setEnabled(false);
        addButton.setText("保存这个桌面入口");

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            titleText.setText("请从桌面进入设置");
            chosenAppText.setText("当前入口：无");
            helpText.setText("请先在首页添加空入口到桌面，再点桌面上的“点我设置”。");
            addButton.setEnabled(false);
            return;
        }

        ShortcutPrefs.ShortcutData existing = ShortcutPrefs.loadForWidget(this, appWidgetId);
        if (existing != null) {
            if (ShortcutPrefs.TYPE_URL.equals(existing.type)) {
                enableUrlMode();
                nameEdit.setText(existing.label);
                urlEdit.setText(existing.url);
                iconPackageName = existing.iconPackageName;
                iconActivityName = existing.iconActivityName;
                loadExistingIcon(existing);
                return;
            }
            applySelectedApp(existing.label, existing.packageName, existing.activityName, true);
            loadExistingIcon(existing);
            return;
        }

        titleText.setText("设置桌面入口");
        chosenAppText.setText("当前入口：未设置");
        helpText.setText("请选择应用入口，或填写 URL 入口。这个入口的大小已经由首页选择决定。");
        addButton.setEnabled(false);
        iconPreview.setImageResource(R.drawable.ic_launcher_foreground);
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
                    urlMode = false;
                    urlPanel.setVisibility(View.GONE);
                    defaultIconButton.setText("使用应用默认图标");
                    applySelectedApp(app.label, app.packageName, app.activityName, true);
                })
                .create();
        dialog.show();
    }

    private void enableUrlMode() {
        urlMode = true;
        label = null;
        packageName = null;
        activityName = null;
        titleText.setText("设置 URL 入口");
        chosenAppText.setText("当前入口：URL");
        urlPanel.setVisibility(View.VISIBLE);
        nameEdit.setHint("小红书");
        defaultIconButton.setText("清空图标");
        addButton.setText("保存这个桌面入口");
        addButton.setEnabled(true);
        helpText.setText("填写名称和 URL，再选择图标。URL 例子：xhsdiscover://search/result");
        if (selectedBitmap == null) {
            iconPreview.setImageResource(R.drawable.ic_launcher_foreground);
        }
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
        if (packageName == null || activityName == null) {
            Toast.makeText(this, "请先选择应用入口", Toast.LENGTH_SHORT).show();
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

    private void loadExistingIcon(ShortcutPrefs.ShortcutData existing) {
        if (existing.iconPath == null) {
            if (ShortcutPrefs.TYPE_URL.equals(existing.type) && existing.iconPackageName != null && existing.iconActivityName != null) {
                loadIconFromApp(existing.iconPackageName, existing.iconActivityName);
            }
            return;
        }
        File file = new File(existing.iconPath);
        if (!file.exists()) {
            return;
        }
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        if (bitmap != null) {
            selectedBitmap = bitmap;
            iconPreview.setImageBitmap(bitmap);
            customImageSelected = true;
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

    private void saveCurrentWidget() {
        if (isSaving) {
            return;
        }
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            Toast.makeText(this, "请从桌面入口进入设置", Toast.LENGTH_LONG).show();
            return;
        }
        if (!urlMode && (packageName == null || activityName == null)) {
            Toast.makeText(this, "请先选择应用入口", Toast.LENGTH_LONG).show();
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
        addButton.setText("正在保存...");

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
