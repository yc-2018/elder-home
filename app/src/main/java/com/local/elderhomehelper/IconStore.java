package com.local.elderhomehelper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.database.Cursor;
import android.content.ContentResolver;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public final class IconStore {
    private static final int ICON_SIZE = 512;

    private IconStore() {
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            if (bitmap != null) {
                return Bitmap.createScaledBitmap(bitmap, ICON_SIZE, ICON_SIZE, true);
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(ICON_SIZE, ICON_SIZE, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static Bitmap loadBitmapFromUri(Context context, Uri uri) throws IOException {
        ContentResolver resolver = context.getContentResolver();
        InputStream inputStream = resolver.openInputStream(uri);
        if (inputStream == null) {
            throw new IOException("Cannot open image");
        }
        try {
            Bitmap source = BitmapFactory.decodeStream(inputStream);
            if (source == null) {
                throw new IOException("Cannot decode image");
            }
            return centerCrop(source, ICON_SIZE, ICON_SIZE);
        } finally {
            inputStream.close();
        }
    }

    public static File saveBitmap(Context context, Bitmap bitmap, String prefix) throws IOException {
        File dir = new File(context.getFilesDir(), "shortcut_icons");
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Cannot create icon directory");
        }
        File file = new File(dir, prefix + "_" + System.currentTimeMillis() + ".png");
        FileOutputStream outputStream = new FileOutputStream(file);
        try {
            if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)) {
                throw new IOException("Cannot write icon");
            }
        } finally {
            outputStream.close();
        }
        return file;
    }

    public static String queryDisplayName(Context context, Uri uri) {
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor == null) {
            return "";
        }
        try {
            int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            if (index >= 0 && cursor.moveToFirst()) {
                return cursor.getString(index);
            }
            return "";
        } finally {
            cursor.close();
        }
    }

    private static Bitmap centerCrop(Bitmap source, int width, int height) {
        int sourceWidth = source.getWidth();
        int sourceHeight = source.getHeight();
        if (sourceWidth <= 0 || sourceHeight <= 0) {
            return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        }

        float scale = Math.max(width / (float) sourceWidth, height / (float) sourceHeight);
        int scaledWidth = Math.round(sourceWidth * scale);
        int scaledHeight = Math.round(sourceHeight * scale);
        Bitmap scaled = Bitmap.createScaledBitmap(source, scaledWidth, scaledHeight, true);
        int left = Math.max(0, (scaledWidth - width) / 2);
        int top = Math.max(0, (scaledHeight - height) / 2);
        Bitmap cropped = Bitmap.createBitmap(scaled, left, top, width, height);
        if (scaled != source) {
            scaled.recycle();
        }
        return cropped;
    }
}
