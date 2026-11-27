package com.unrevr.munhaeryeok.Alarm;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;

import org.json.JSONArray;
import org.json.JSONException;

import java.net.URISyntaxException;

/** The receiver, activity, and service access this queue on the main thread. */
final class RingSession {
    static SharedPreferences state(Context context) {
        return context.getSharedPreferences("ring_session", Context.MODE_PRIVATE);
    }

    static String key(Intent intent) {
        if (intent == null) return null;
        int id = intent.getIntExtra("id", 0);
        int token = intent.getIntExtra("alarm_id", 0);
        return id == 0 || token == 0 ? null : id + "/" + token;
    }

    private static JSONArray queue(SharedPreferences state) {
        try {
            return new JSONArray(state.getString("queue", "[]"));
        } catch (JSONException e) {
            throw new IllegalStateException("Invalid stored ringing queue", e);
        }
    }

    private static Intent intentAt(JSONArray events, int index) {
        try {
            return Intent.parseUri(events.getString(index), Intent.URI_INTENT_SCHEME);
        } catch (JSONException | URISyntaxException e) {
            throw new IllegalStateException("Invalid stored alarm occurrence", e);
        }
    }

    static void enqueue(Context context, Intent ring) {
        SharedPreferences prefs = state(context);
        JSONArray events = queue(prefs);
        String occurrence = key(ring);
        for (int i = 0; i < events.length(); i++) {
            if (occurrence.equals(key(intentAt(events, i)))) return;
        }
        events.put(ring.toUri(Intent.URI_INTENT_SCHEME));
        SharedPreferences.Editor edit = prefs.edit().putString("queue", events.toString());
        if (events.length() == 1) {
            edit.putString("occurrence", occurrence).putLong("started_at", SystemClock.elapsedRealtime());
        }
        edit.apply();
    }

    static Intent current(Context context) {
        JSONArray events = queue(state(context));
        return events.length() == 0 ? null : intentAt(events, 0);
    }

    static boolean isActive(Context context, String occurrence) {
        return occurrence != null && occurrence.equals(state(context).getString("occurrence", null));
    }

    static void end(Context context, String occurrence) {
        if (!isActive(context, occurrence)) return;
        SharedPreferences prefs = state(context);
        JSONArray events = queue(prefs);
        events.remove(0);
        SharedPreferences.Editor edit = prefs.edit().clear();
        if (events.length() > 0) {
            edit.putString("queue", events.toString()).putString("occurrence", key(intentAt(events, 0)))
                    .putLong("started_at", SystemClock.elapsedRealtime());
        }
        edit.apply();
    }

    private RingSession() {}
}
