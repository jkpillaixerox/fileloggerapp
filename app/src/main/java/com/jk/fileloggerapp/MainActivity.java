package com.jk.fileloggerapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.jk.fileloggerapp.logger.AppLogUtil;
import com.jk.fileloggerapp.logger.FileLoggingTree;

import java.io.File;
import java.util.ArrayList;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.e("onCreate...");
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        findViewById(R.id.share_log_files_button).setOnClickListener(view -> shareLogFile());
    }

    @Override
    protected void onResume() {
        super.onResume();
        Timber.e("onResume...");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Timber.e("onPause...");
    }

    private void shareLogFile() {

        for (int i = 0; i < 1000; i++) {
            Timber.tag(TAG).e("Error log e");
            Timber.tag(TAG).v("verbose log v");
            Timber.tag(TAG).d("debug log d");
            Timber.tag(TAG).i("info log i");
            Timber.tag(TAG).w("warn log w");
        }

        try {
            File logFileFromTimber1 = new File(AppLogUtil.getInstance().getLogDirectory(),
                    FileLoggingTree.FILE_NAME_1);
            File logFileFromTimber2 = new File(AppLogUtil.getInstance().getLogDirectory(),
                    FileLoggingTree.FILE_NAME_2);
            File logFileFromTimber3 = new File(AppLogUtil.getInstance().getLogDirectory(),
                    FileLoggingTree.FILE_NAME_3);
            String subject = "Log file";
            ArrayList<Uri> uris = new ArrayList<>();

            if (logFileFromTimber1.exists()) {
                Uri fileUri1 = FileProvider.getUriForFile(
                        this,
                        "com.jk.fileloggerapp.fileprovider",
                        logFileFromTimber1);
                uris.add(fileUri1);
                grantUriPermission(getPackageName(), fileUri1,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            if (logFileFromTimber2.exists()) {
                Uri fileUri2 = FileProvider.getUriForFile(
                        this,
                        "com.jk.fileloggerapp.fileprovider",
                        logFileFromTimber2);
                uris.add(fileUri2);
                grantUriPermission(getPackageName(), fileUri2,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            if (logFileFromTimber3.exists()) {
                Uri fileUri3 = FileProvider.getUriForFile(
                        this,
                        "com.jk.fileloggerapp.fileprovider",
                        logFileFromTimber3);
                uris.add(fileUri3);
                grantUriPermission(getPackageName(), fileUri3,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }

            Intent shareIntent = new ShareCompat.IntentBuilder(this).getIntent();
            shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
            shareIntent.setType("text/plain");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
            shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(shareIntent,
                    "Share application logs"));
        } catch (Exception e) {
            Timber.e(e, "shareLogFile error %s", e.getMessage());
        }
    }
}