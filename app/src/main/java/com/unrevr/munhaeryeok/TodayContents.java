package com.unrevr.munhaeryeok;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import io.reactivex.rxjava3.core.Single;

final class TodayContents {
    final String title;
    final String content;
    final String author;
    final String url;

    private TodayContents(JSONObject column) throws JSONException, URISyntaxException {
        title = column.getString("title");
        content = column.getString("description");
        author = column.getString("author");
        url = column.getString("link");
        URI link = new URI(url);
        if (!("https".equalsIgnoreCase(link.getScheme()) || "http".equalsIgnoreCase(link.getScheme()))
                || link.getHost() == null) {
            throw new JSONException("Invalid article URL");
        }
    }

    static Single<TodayContents> fetch(int index) {
        return Single.create(emitter -> {
            try {
                emitter.onSuccess(request(index));
            } catch (IOException | JSONException | URISyntaxException error) {
                emitter.tryOnError(error);
            }
        });
    }

    private static TodayContents request(int index) throws IOException, JSONException, URISyntaxException {
        String body = Jsoup.connect("https://auk.hegelty.space/column")
                .data("id", Integer.toString(index))
                .timeout(8000)
                .ignoreContentType(true)
                .execute()
                .body();
        return new TodayContents(new JSONObject(body));
    }
}
