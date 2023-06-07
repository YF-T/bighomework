package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.DocumentsContract;
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
                Log.d("returndata", result.toString());
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
                Log.d("returndata", result.toString());
                callback.apply(result);
                response.close();
            }
        });
    }

    // 把url转成Bitmap的函数
    public static void downloadImage(String url, final Function<Bitmap, Void> callback) {
        if (!url.startsWith("/"))
            url = "/" + url;
        if (!url.startsWith("/image"))
            url = "/image" + url;
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

    public static void sendPostVideoRequest(String url, HashMap<String, String> args, Uri video, String videoMediaType, Context context, final Function<HashMap<String, Object>, Void> callback) {
        OkHttpClient client = okHttpClient;
        url = baseUrl + url;
        String jwt = GlobalVariable.get("jwt", "");

        // 构建请求体参数
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);

        // 添加文本参数
        if (args != null) {
            for (String key : args.keySet()) {
                builder.addFormDataPart(key, args.get(key));
            }
        }

        // 添加视频文件参数
        Log.d("roud", video.getPath());
        File videoFile = new File(getRealPathFromUri(context, video));
        Log.d("roud", "success1");
        builder.addFormDataPart("image", videoFile.getName(),
                RequestBody.create(MediaType.parse(videoMediaType), videoFile));
        Log.d("roud", "success2");

        RequestBody requestBody = builder.build();

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .header("Authorization", jwt)
                .build();

        Log.d("roud", "success3" + Build.VERSION.SDK_INT);

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
        Log.d("roud", "success4");
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

    /**
     * 根据Uri获取文件的绝对路径
     *
     * @param context 上下文对象
     * @param uri  文件的Uri
     * @return 如果Uri对应的文件存在, 那么返回该文件的绝对路径, 否则返回null
     */
    public static String getRealPathFromUri(Context context, Uri uri) {
        System.out.println("RealPathEromUri中的uri:"+uri);
        int sdkVersion = Build.VERSION.SDK_INT;
        if (sdkVersion >= 19) { // api >= 19
            System.out.println("sdkVersion>=19");
            return getRealPathFromUriAboveApi19(context, uri);
        } else { // api < 19
            return getRealPathFromUriBelowAPI19(context, uri);
        }
    }

    /**
     * 适配api19以下(不包括api19),根据uri获取图片的绝对路径
     *
     * @param context 上下文对象
     * @param uri  图片的Uri
     * @return 如果Uri对应的图片存在, 那么返回该图片的绝对路径, 否则返回null
     */
    private static String getRealPathFromUriBelowAPI19(Context context, Uri uri) {
        return getDataColumn(context, uri, null, null);
    }

    /**
     * 适配api19及以上,根据uri获取图片的绝对路径
     *
     * @param context 上下文对象
     * @param uri  图片的Uri
     * @return 如果Uri对应的图片存在, 那么返回该图片的绝对路径, 否则返回null
     */
    @SuppressLint("NewApi")
    private static String getRealPathFromUriAboveApi19(Context context, Uri uri) {
        String filePath = null;
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // 如果是document类型的 uri, 则通过document id来进行处理
            String documentId = DocumentsContract.getDocumentId(uri);
            if (isMediaDocument(uri)) { // MediaProvider
                System.out.println("11111111111111111111111111111111111");
                // 使用':'分割
                String id = documentId.split(":")[1];
                System.out.println("id:"+id);
                String selection = MediaStore.Video.Media._ID + "=?";
                System.out.println("selection:"+selection);
                String[] selectionArgs = {id};
                filePath = getDataColumn(context, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, selection, selectionArgs);
                System.out.println("filePath:"+filePath);
            } else if (isDownloadsDocument(uri)) { // DownloadsProvider
                System.out.println("2222222222222222222222222222222222222");
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(documentId));
                filePath = getDataColumn(context, contentUri, null, null);
                System.out.println("filePath:"+filePath);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            System.out.println("333333333333333333333333333333333");
            // 如果是 content 类型的 Uri
            filePath = getDataColumn(context, uri, null, null);
            System.out.println("filePath:"+filePath);
        } else if ("file".equals(uri.getScheme())) {
            System.out.println("444444444444444444444444444444444444");
            // 如果是 file 类型的 Uri,直接获取图片对应的路径
            filePath = uri.getPath();
            System.out.println("filePath:"+filePath);
        }
        return filePath;
    }

    /**
     * 获取数据库表中的 _data 列，即返回Uri对应的文件路径
     *
     * @return
     */
    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        String path = null;

        String[] projection = new String[]{MediaStore.Images.Media.DATA};
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(projection[0]);
                path = cursor.getString(columnIndex);
            }
        } catch (Exception e) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return path;
    }

    /**
     * @param uri the Uri to check
     * @return Whether the Uri authority is MediaProvider
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri the Uri to check
     * @return Whether the Uri authority is DownloadsProvider
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }
}