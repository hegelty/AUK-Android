package com.unrevr.munhaeryeok;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.rxjava3.schedulers.Schedulers;

final class ScoreSharing {
    private static final AtomicBoolean uploading = new AtomicBoolean();

    private ScoreSharing() {}

    private static SharedPreferences preferences(Context context) {
        return context.getSharedPreferences("uploaded", Context.MODE_PRIVATE);
    }

    static boolean isEnabled(Context context) {
        return preferences(context).getBoolean("share_score", false);
    }

    static void setEnabled(Context context, boolean enabled) {
        preferences(context).edit().putBoolean("share_score", enabled).apply();
    }

    // A rotation can create another home screen while the server is processing the first POST.
    // Keep that operation with the application until its acknowledgement is recorded.
    static void uploadIfDue(Context context, String id, int score) {
        Context app = context.getApplicationContext();
        if (id.isEmpty() || !isEnabled(app) || !uploading.compareAndSet(false, true)) return;
        Schedulers.io().scheduleDirect(() -> {
            try {
                String today = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
                SharedPreferences prefs = preferences(app);
                if (!isEnabled(app) || today.equals(prefs.getString("date", ""))) return;

                String result = Jsoup.connect("https://auk.hegelty.space/upload_score")
                        .timeout(8000)
                        .followRedirects(false)
                        .method(Connection.Method.POST)
                        .data("id", id)
                        .data("score", Integer.toString(score))
                        .execute()
                        .body();
                if ("success".equals(result.trim())) prefs.edit().putString("date", today).apply();
            } catch (IOException e) {
                Log.i("ScoreSharing", "Score upload deferred until the next visit");
            } finally {
                uploading.set(false);
            }
        });
    }
}
