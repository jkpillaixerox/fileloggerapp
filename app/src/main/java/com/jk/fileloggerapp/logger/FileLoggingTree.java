package com.jk.fileloggerapp.logger;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;

import timber.log.Timber;

public class FileLoggingTree extends Timber.Tree {
    private static final String TAG = "FileLoggingTree";
    public static final String FILE_NAME_1 = "app_logs_1.txt";
    public static final String FILE_NAME_2 = "app_logs_2.txt";
    public static final String FILE_NAME_3 = "app_logs_3.txt";
    public static final String CURRENT_FILE_NAME_KEY = "log_current_file_name_key";

    private static final String PREF_NAME_LOG_SETTINGS = "pref_log_settings";

    private static final int LOG_FILE_MAX_SIZE = 5 * 1024;//5Kb
    private final Context context;
    private BufferedWriter writer;
    private SimpleDateFormat simpleDateFormat;
    private final Date dateInstance = new Date();
    private final ExecutorService executorService;

    public FileLoggingTree(Context context, ExecutorService executorService) {
        this.context = context;
        this.executorService = executorService;
        AppLogUtil.getInstance().createLogsDirectory(context);
    }

    @SuppressLint("LogNotTimber")
    @Override
    protected void log(int priority, String tag, @NonNull String message, Throwable t) {
        try {
            if (executorService != null) {
                executorService.execute(() -> logToFile(priority, tag, message, t));
            }
        } catch (Exception e) {
            Log.e(TAG, "log error " + e.getMessage(), e);
        }
    }

    @SuppressLint("LogNotTimber")
    private void logToFile(int priority, String tag, String message, Throwable t) {
        //The log files are rotated when it reaches the maximum size(LOG_FILE_MAX_SIZE).
        //So that latest logs are available for viewing.
        try {

            String currentLogFileName = getCurrentLogFileName();
            File currentLogFile;
            if (currentLogFileName == null) {
                currentLogFile = new File(AppLogUtil.getInstance().getLogDirectory(), FILE_NAME_1);
                if (!currentLogFile.exists()) {
                    boolean isNewFileCreated = currentLogFile.createNewFile();
                    if (!isNewFileCreated) {
                        Log.e(TAG, "logToFile new file not created! " +
                                "file name : " + currentLogFile.getName());
                    }
                }
                saveCurrentLogFileName(currentLogFile.getName());
            } else {
                currentLogFile = new File(AppLogUtil.getInstance().getLogDirectory(),
                        currentLogFileName);
                if (!currentLogFile.exists()) {
                    boolean isNewFileCreated = currentLogFile.createNewFile();
                    if (!isNewFileCreated) {
                        Log.e(TAG, "logToFile new file not created! " +
                                "file name : " + currentLogFile.getName());
                    }
                }
            }

            long logFileSize = currentLogFile.length();
            if (logFileSize >= LOG_FILE_MAX_SIZE) {
                if (currentLogFile.getName().equals(FILE_NAME_1)) {
                    currentLogFile = new File(AppLogUtil.getInstance().getLogDirectory(),
                            FILE_NAME_2);
                    createOrDeleteContents(currentLogFile);

                } else if (currentLogFile.getName().equals(FILE_NAME_2)) {
                    currentLogFile = new File(AppLogUtil.getInstance().getLogDirectory(),
                            FILE_NAME_3);
                    createOrDeleteContents(currentLogFile);

                } else if (currentLogFile.getName().equals(FILE_NAME_3)) {
                    currentLogFile = new File(AppLogUtil.getInstance().getLogDirectory(),
                            FILE_NAME_1);
                    createOrDeleteContents(currentLogFile);

                }
                saveCurrentLogFileName(currentLogFile.getName());
                writer = null;
            }

            String logLine;

            if (tag != null && tag.length() > 20) {
                logLine = String.format("%s %s/%-20.20s: %s \n",
                        getDateString(System.currentTimeMillis()), getLogPriorityString(priority),
                        tag, message);
            } else {
                logLine = String.format("%s %s/%s: %s \n",
                        getDateString(System.currentTimeMillis()), getLogPriorityString(priority),
                        tag, message);
            }
            if (t != null) {
                logLine += t + "\n";
            }

            if (writer == null) {
                writer = new BufferedWriter(new FileWriter(currentLogFile, true));
            }
            writer.append(logLine);
            writer.flush();
        } catch (Exception e) {
            Log.e(TAG, "logToFile error " + e.getMessage(), e);
            if (writer != null) {
                try {
                    writer.flush();
                    writer.close();
                    writer = null;
                } catch (IOException ex) {
                    Log.e(TAG, "logToFile error " + e.getMessage(), e);
                }
            }
        }
    }

    private String getDateString(long dateTimestamp) {
        if (simpleDateFormat == null) {
            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a",
                    Locale.getDefault());
        }

        dateInstance.setTime(dateTimestamp);

        return simpleDateFormat.format(dateInstance);
    }

    private String getLogPriorityString(int priority) {
        switch (priority) {
            case Log.VERBOSE:
                return "V";
            case Log.DEBUG:
                return "D";
            case Log.INFO:
                return "I";
            case Log.WARN:
                return "W";
            case Log.ERROR:
                return "E";
            default:
                return "UNKNOWN";
        }
    }

    private String getCurrentLogFileName() {
        if (context != null) {
            SharedPreferences preferences = context.
                    getSharedPreferences(PREF_NAME_LOG_SETTINGS, Context.MODE_PRIVATE);
            if (preferences != null) {
                return preferences.getString(CURRENT_FILE_NAME_KEY, null);
            }
        }

        return null;
    }

    @SuppressLint("ApplySharedPref")
    private void saveCurrentLogFileName(String fileName) {
        if (context != null) {
            SharedPreferences preferences = context.
                    getSharedPreferences(PREF_NAME_LOG_SETTINGS, Context.MODE_PRIVATE);
            if (preferences != null) {
                preferences.edit().putString(CURRENT_FILE_NAME_KEY, fileName).commit();
            }
        }
    }

    @SuppressLint("LogNotTimber")
    private void deleteFileContents(File file) {
        if (file != null && file.exists()) {
            FileWriter fileWriter = null;
            try {
                fileWriter = new FileWriter(file);
                fileWriter.write("");
            } catch (IOException e) {
                Log.e(TAG, "deleteFileContents error " + e.getMessage(), e);
            } finally {
                try {
                    if (fileWriter != null) {
                        fileWriter.flush();
                        fileWriter.close();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "deleteFileContents error " + e.getMessage(), e);
                }
            }
        }
    }

    @SuppressLint("LogNotTimber")
    private void createOrDeleteContents(File currentFile) throws Exception {
        if (!currentFile.exists()) {

            boolean isNewFileCreated = currentFile.createNewFile();
            if (!isNewFileCreated) {
                Log.e(TAG, "logToFile new file not created! " +
                        "file name : " + currentFile.getName());
            }
        } else {
            deleteFileContents(currentFile);
        }
    }
}