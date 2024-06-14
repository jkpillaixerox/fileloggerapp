package com.jk.fileloggerapp.logger;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.File;


public class AppLogUtil {
    private static final String TAG = "AppLogUtil";

    private static AppLogUtil sInstance;
    private File logDirectory;

    private AppLogUtil() {
    }

    public static AppLogUtil getInstance() {
        if (sInstance == null)
            sInstance = new AppLogUtil();
        return sInstance;
    }

    @SuppressLint("LogNotTimber")
    public void createLogsDirectory(@Nullable Context context) {
        if (context == null) return;

        logDirectory = new File(context.getCacheDir(), "logs" + File.separator);
        if (!(logDirectory.exists() && logDirectory.isDirectory())) {
            boolean isLogDirCreated = logDirectory.mkdir();
            if (!isLogDirCreated) {
                Log.e(TAG, "Failed to create logs directory");
            }

        }
    }

    public File getLogDirectory() {
        return logDirectory;
    }
}
