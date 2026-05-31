package com.local.elderhomehelper;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class AppLoader {
    private AppLoader() {
    }

    public static List<AppEntry> loadLaunchableApps(Context context) {
        PackageManager packageManager = context.getPackageManager();
        Intent launcherIntent = new Intent(Intent.ACTION_MAIN);
        launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> results = packageManager.queryIntentActivities(launcherIntent, 0);
        List<AppEntry> apps = new ArrayList<>();
        String ownPackage = context.getPackageName();

        for (ResolveInfo info : results) {
            if (info.activityInfo == null) {
                continue;
            }
            String packageName = info.activityInfo.packageName;
            if (ownPackage.equals(packageName)) {
                continue;
            }
            String label = String.valueOf(info.loadLabel(packageManager));
            apps.add(new AppEntry(
                    label,
                    packageName,
                    info.activityInfo.name,
                    info.loadIcon(packageManager)
            ));
        }

        final Collator collator = Collator.getInstance(Locale.CHINA);
        Collections.sort(apps, new Comparator<AppEntry>() {
            @Override
            public int compare(AppEntry left, AppEntry right) {
                return collator.compare(left.label, right.label);
            }
        });
        return apps;
    }
}
