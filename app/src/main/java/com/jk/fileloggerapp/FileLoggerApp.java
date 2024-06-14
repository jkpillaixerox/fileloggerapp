package com.jk.fileloggerapp;

import android.app.Application;


import com.jk.fileloggerapp.logger.FileLoggingTree;

import java.util.concurrent.Executors;

import timber.log.Timber;

public class FileLoggerApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        if (!BuildConfig.DEBUG) {//Release mode
            Timber.plant(new FileLoggingTree(this, Executors.newSingleThreadExecutor()));
        } else {
            Timber.plant(new Timber.DebugTree());
        }
    }
}
