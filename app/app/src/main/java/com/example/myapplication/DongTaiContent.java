package com.example.myapplication;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.AnyRes;
import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;

public class DongTaiContent implements Serializable {
    public int id;
    public String publisher;
    public String headimg;
    public String time;
    public String content;
    public ArrayList<String> imagearray = new ArrayList<String>(); // 上传image
    public int like;
    public int comment;
    public int collect;
    public String title;
    public String urlImages;
    public String position;
    public String tag;
    public boolean bool_thumb;
    public boolean bool_collect;
    public boolean bool_follow;


    public DongTaiContent() {
        publisher = "Tanyifan";
        headimg = "/user/abc.jpg";
        time = "21:00 Apr 4th";
        content = "content";
        title = "title";
        like = 123;
        comment = 45;
        collect = 6;
    }

    public DongTaiContent(HashMap<String, Object> hashMap) {
        if (hashMap.containsKey("id")) {
            id = (int) hashMap.get("id");
        }
        if (hashMap.containsKey("author__name")) {
            publisher = (String) hashMap.get("author__name");
        }
        if (hashMap.containsKey("author__image")) {
            headimg = (String) hashMap.get("author__image");
        }
        if (hashMap.containsKey("created_time")) {
            time = (String) hashMap.get("created_time");
        }
        if (hashMap.containsKey("content")) {
            content = (String) hashMap.get("content");
        }
        if (hashMap.containsKey("num_thumbs")) {
            like = (int) hashMap.get("num_thumbs");
        }
        if (hashMap.containsKey("num_comments")) {
            comment = (int) hashMap.get("num_comments");
        }
        if (hashMap.containsKey("num_collects")) {
            collect = (int) hashMap.get("num_collects");
        }
        if (hashMap.containsKey("title")) {
            title = (String) hashMap.get("title");
        }
        if (hashMap.containsKey("url_images")) {
            urlImages = (String) hashMap.get("url_images");
        }
        if (hashMap.containsKey("tag")) {
            tag = (String) hashMap.get("tag");
        }
        if (hashMap.containsKey("position")) {
            position = (String) hashMap.get("position");
        }
        if (position.equals("点击获取用户位置")) {
            position = "unknown position";
        }
        if (hashMap.containsKey("bool_thumb")) {
            bool_thumb = (boolean) hashMap.get("bool_thumb");
            Log.d("bool_thumb", " "+bool_thumb);
        }
        if (hashMap.containsKey("bool_collect")) {
            bool_collect = (boolean) hashMap.get("bool_collect");
            Log.d("bool_collect", " "+bool_collect);
        }
        if (hashMap.containsKey("bool_follow")) {
            bool_follow = (boolean) hashMap.get("bool_follow");
            Log.d("bool_follow", " "+bool_follow);
        }

        // 处理图片数组
        if (hashMap.containsKey("url_images")) {
            String urlImages = (String) hashMap.get("url_images");
            if (urlImages != null && !urlImages.isEmpty()) {
                String[] imageUrls = urlImages.split(",");
                for (String imageUrl : imageUrls) {
                    imagearray.add(imageUrl);
                }
            }
        }
    }

    public Uri getUriFromResId(Context context, @AnyRes int drawableId) {
        return Uri.parse(
                ContentResolver.SCHEME_ANDROID_RESOURCE
                        + "://" + context.getResources().getResourcePackageName(drawableId)
                        + '/' + context.getResources().getResourceTypeName(drawableId)
                        + '/' + context.getResources().getResourceEntryName(drawableId)
        );
    }

    public DongTaiContent(String publisher, String headimg, String time, String content, int like, int comment, int collect, String title, ArrayList<String> imagearray) {
        this.publisher = publisher;
        this.headimg = headimg;
        this.time = time;
        this.content = content;
        this.like = like;
        this.comment = comment;
        this.collect = collect;
        this.imagearray = imagearray;
        this.title = title;
    }

}
