package com.example.myapplication;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

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
    public int headimg;
    public String time;
    public String content;
    public ArrayList<String> imagearray = new ArrayList<String>(); // 上传image
    public int like;
    public int comment;
    public int collect;
    public String title;

    public DongTaiContent() {
        publisher = "Tanyifan";
        headimg = R.drawable.thussbuilding;
        time = "21:00 Apr 4th";
        content = "content";
        title = "title";
        like = 123;
        comment = 45;
        collect = 6;
    }

    public DongTaiContent(String publisher, int headimg, String time, String content, int like, int comment, int collect, String title, ArrayList<Integer> imagearray, Context context) {
        this.publisher = publisher;
        this.headimg = headimg;
        this.time = time;
        this.content = content;
        this.like = like;
        this.comment = comment;
        this.collect = collect;
        this.imagearray = new ArrayList<String>();
        this.title = title;
        for(Integer resId: imagearray) {
            Uri uri = getUriFromResId(context, resId.intValue());
            String imageMediaType = context.getContentResolver().getType(uri);
            DongTaiContent dongTaiContent = this;
            this.imagearray.add(uri.toString());
//            try {
//                WebRequest.sendPostImageRequest("/dongtai/image/upload", new HashMap<>(), uri, imageMediaType, new Function<HashMap<String, Object>, Void>() {
//                    @Override
//                    public Void apply(HashMap<String, Object> stringObjectHashMap) {
//                        dongTaiContent.imagearray.add(getUriFromResId(context, resId.intValue()).toString());
//                        return null;
//                    }
//                });
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
        }
    }

    public Uri getUriFromResId(@NonNull Context context, @AnyRes int drawableId) {
        return Uri.parse(
                ContentResolver.SCHEME_ANDROID_RESOURCE
                        + "://" + context.getResources().getResourcePackageName(drawableId)
                        + '/' + context.getResources().getResourceTypeName(drawableId)
                        + '/' + context.getResources().getResourceEntryName(drawableId)
        );
    }

    public DongTaiContent(String publisher, int headimg, String time, String content, int like, int comment, int collect, String title, ArrayList<String> imagearray) {
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
