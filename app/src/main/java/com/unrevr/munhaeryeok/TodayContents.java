package com.unrevr.munhaeryeok;

import android.content.Context;
import android.util.Log;

import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

public class TodayContents {
    public String title;
    public String content;
    public String author;
    public String url;

    public void getColumn(int num) {
        try {
            String res = Jsoup.connect("https://auk.hegelty.space/column")
                    .data("id", Integer.toString(num))
                    .method(Connection.Method.GET)
                    .ignoreContentType(true)
                    .execute()
                    .body();
            JSONObject jsonObject = new JSONObject(res);
            this.title = jsonObject.getString("title");
            this.content = jsonObject.getString("description");
            this.author = jsonObject.getString("author");
            this.url = jsonObject.getString("link");
            Log.d("TodayContents", "getColumn: " + this.title + " " + this.content + " " + this.author + " " + this.url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
