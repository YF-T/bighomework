package com.example.myapplication;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 本例为单例类，用于实现所有的像后端发送的网络请求
 * 使用方法见下面已经写好的函数
 * 大家可以调用一些已经写好的工具，如url转bitmap等
 */
class WebRequest {

    private static WebRequest mInstance = new WebRequest();//程序启动时立即创建单例
    public static String baseUrl = "http://10.0.2.2:8000";

    // 创建OkHttpClient对象, 并设置超时时间 添加拦截器LoginInterceptor
    public static final OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .build();

    private static final Handler handler = new Handler(Looper.getMainLooper());
    private WebRequest() {//构造函数私有
    }

    public static WebRequest getInstance() {//唯一的访问入口
        return mInstance;
    }
    public static Context context;

    // 发送GET请求并调用回调函数
    public static void sendGetRequest(String url, HashMap<String, String> args, Function<HashMap<String, Object>, Void> callback) throws IOException {
        OkHttpClient client = okHttpClient;
        url = baseUrl + url;
        Log.d("url", url);
        String jwt = GlobalVariable.get("jwt", "");
        HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
        for (HashMap.Entry<String, String> entry : args.entrySet()) {
            urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
        }
        String requestUrl = urlBuilder.build().toString();
        Request request = new Request.Builder()
                .url(requestUrl)
                .header("Authorization", jwt)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                JSONObject json = null;
                try {
                    json = new JSONObject(responseBody);
                } catch (JSONException e) {
                    throw new IOException(e);
                }
                HashMap<String, Object> result = new HashMap<>();
                for (Iterator<String> it = json.keys(); it.hasNext(); ) {
                    String key = it.next();
                    try {
                        result.put(key, json.get(key));
                    } catch (JSONException e) {
                        throw new IOException(e);
                    }
                }
                Log.d("url status", result.get("status").toString());
                callback.apply(result);
                response.close();
            }
        });
    }

    // 发送POST请求并调用回调函数
    public static void sendPostRequest(String url, HashMap<String, String> args, Function<HashMap<String, Object>, Void> callback) throws IOException {
        OkHttpClient client = okHttpClient;
        url = baseUrl + url;
        String jwt = GlobalVariable.get("jwt", "");
        FormBody.Builder formBuilder = new FormBody.Builder();
        for (HashMap.Entry<String, String> entry : args.entrySet()) {
            formBuilder.add(entry.getKey(), entry.getValue());
        }
        RequestBody formBody = formBuilder.build();
        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .header("Authorization", jwt)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                JSONObject json = null;
                try {
                    json = new JSONObject(responseBody);
                } catch (JSONException e) {
                    throw new IOException(e);
                }
                HashMap<String, Object> result = new HashMap<>();
                for (Iterator<String> it = json.keys(); it.hasNext(); ) {
                    String key = it.next();
                    try {
                        result.put(key, json.get(key));
                    } catch (JSONException e) {
                        throw new IOException(e);
                    }
                }
                Log.d("url status", result.get("status").toString());
                callback.apply(result);
                response.close();
            }
        });
    }

    // 把url转成Bitmap的函数
    public static void downloadImage(String url, final Function<Bitmap, Void> callback) {
        String oldurl = url;
        url = WebRequest.baseUrl + url;

        Log.d("imageurl", url);
        Request request = new Request.Builder().url(url).build();

        // 判断本地是否存在缓存文件
        File cacheFile = getCacheFile(oldurl);
        if (cacheFile.exists()) {
            // 如果缓存文件存在，则直接从本地读取并返回Bitmap
            Bitmap bitmap = BitmapFactory.decodeFile(cacheFile.getAbsolutePath());
            callback.apply(bitmap);
            return;
        }

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // 处理下载失败情况
                return;
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    // 处理下载失败情况
                    return;
                }

                // 将响应体转化为Bitmap
                Bitmap bitmap = BitmapFactory.decodeStream(response.body().byteStream());

                saveBitmapToCache(bitmap, oldurl);

                // 通过Handler将结果返回给主线程
                handler.post(() -> callback.apply(bitmap));
            }
        });
    }

    private static File getCacheFile(String url) {
        // 根据URL生成唯一的文件名
        String fileName = url.replace('/', '_');
        // 根据文件名获取缓存目录下的文件
        File cacheDir = context.getCacheDir();
        return new File(cacheDir, fileName);
    }

    private static void saveBitmapToCache(Bitmap bitmap, String url) {
        File cacheFile = getCacheFile(url);
        try (OutputStream outputStream = new FileOutputStream(cacheFile)) {
            // 将Bitmap保存到缓存文件中
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getDataColumn(Uri uri, String selection, String[] selectionArgs) {
        String[] projection = {MediaStore.Images.Media.DATA};
        try (Cursor cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                return cursor.getString(columnIndex);
            }
        }
        return null;
    }

    public static void sendPostImageRequest(String url, HashMap<String, String> args, Bitmap image, String imageMediaType, Function<HashMap<String, Object>, Void> callback) throws IOException {
        OkHttpClient client = okHttpClient;
        url = baseUrl + url;
        String jwt = GlobalVariable.get("jwt", "");

        // 构建请求体
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);

        // 添加字段参数
        if (args != null) {
            for (Map.Entry<String, String> entry : args.entrySet()) {
                builder.addFormDataPart(entry.getKey(), entry.getValue());
            }
        }

        // 添加图像文件参数
        if (image != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            RequestBody imageBody = RequestBody.create(MediaType.parse(imageMediaType), baos.toByteArray());
            builder.addFormDataPart("image", "image.jpg", imageBody);
        }

        RequestBody requestBody = builder.build();

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .header("Authorization", jwt)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                // 执行回调函数并传递异常信息
                HashMap<String, Object> result = new HashMap<>();
                result.put("error", e.getMessage());
                callback.apply(result);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                JSONObject json = null;
                try {
                    json = new JSONObject(responseBody);
                } catch (JSONException e) {
                    throw new IOException(e);
                }
                HashMap<String, Object> result = new HashMap<>();
                for (Iterator<String> it = json.keys(); it.hasNext(); ) {
                    String key = it.next();
                    try {
                        result.put(key, json.get(key));
                    } catch (JSONException e) {
                        throw new IOException(e);
                    }
                }
                response.close();

                // 执行回调函数并传递结果
                callback.apply(result);
            }
        });
    }

    public static void setImageByUrl(ImageView imageView, String url) {
//        Uri imageUri = Uri.parse(url);
//        imageView.setImageURI(imageUri);
        WebRequest.downloadImage(url, bitmap -> {
                // 在这里处理下载完成后的逻辑，例如将图片显示在ImageView中
                imageView.setImageBitmap(bitmap);
                return null;
            });
    }
}