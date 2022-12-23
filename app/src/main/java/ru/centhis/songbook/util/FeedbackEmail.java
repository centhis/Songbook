package ru.centhis.songbook.util;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

import androidx.core.content.FileProvider;

import java.io.File;
import java.util.ArrayList;

import ru.centhis.songbook.BuildConfig;
import ru.centhis.songbook.R;
import ru.centhis.songbook.data.SettingsContract;

public class FeedbackEmail {
    private final Activity activity;
    private String email = SettingsContract.LOG_EMAIL;
    private String subject = "";
    private String content = "";
    private ArrayList<File> cacheAttaches = new ArrayList<>();

    public FeedbackEmail(Activity activity){
        this.activity = activity;
    }

    public FeedbackEmail setSubject(String subject){
        this.subject = subject;
        return this;
    }

    public FeedbackEmail setEmail(String email){
        this.email = email;
        return this;
    }

    public FeedbackEmail setContent(String content){
        this.content = content;
        return this;
    }

    public FeedbackEmail cacheAttach(String name){
        File file = new File(activity.getCacheDir(), name + ".log");
        if (file.length() > 0)
            cacheAttaches.add(file);
        return this;
    }

    public FeedbackEmail cacheAttach(File file){
        if (file.length() > 0)
            cacheAttaches.add(file);
        return this;
    }

    public FeedbackEmail build(){
        if (TextUtils.isEmpty(subject))
            subject = activity.getString(R.string.app_name) + " Feedback";

        if (TextUtils.isEmpty(content))
            content = buildContent();
        return this;
    }

    private String buildContent(){
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 4; i++)
            builder.append("\n");
        builder.append("App version: " + BuildConfig.VERSION_NAME + "\n");
//        builder.append("Debuggable: " + Diagnostcis.DEBUG + "\n");
        builder.append("Device: " + Build.MANUFACTURER + " " + Build.PRODUCT + " " + Build.MODEL + "\n");
        builder.append("Android: " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")");

        return builder.toString();
    }

    public void send(){
        String action = cacheAttaches.size() > 0 ? Intent.ACTION_SEND_MULTIPLE : Intent.ACTION_SEND;

        Intent emailIntent = new Intent(action);
        emailIntent.setType("plain/text");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, content);

        ArrayList<Uri> uris = new ArrayList<>();

        for (File file : cacheAttaches){
            Uri contentUri = FileProvider.getUriForFile(activity, BuildConfig.APPLICATION_ID + ".fileprovider", file);
            uris.add(contentUri);
        }

        emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        activity.startActivity(emailIntent);
    }
}
