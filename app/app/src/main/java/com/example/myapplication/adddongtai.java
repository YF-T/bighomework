package com.example.myapplication;

import static android.content.Intent.ACTION_OPEN_DOCUMENT;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import android.util.Base64;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

public class adddongtai extends AppCompatActivity {

    public ImageView headimg;
    public TextView publisher;
    public EditText content;
    public GridLayout contentimg;
    public LinearLayout all;
    public EditText title;
    public SquareImageView addphoto;
    public ArrayList<String> uriArrayList = new ArrayList<String>();
    public ArrayList<String> urlArrayList = new ArrayList<String>();
    public ActivityResultLauncher<String> pickMedia =
            registerForActivityResult(new ActivityResultContracts.GetMultipleContents(), new ActivityResultCallback<List<Uri>>() {
                @Override
                public void onActivityResult(List<Uri> result) {
                    for(Uri uri: result) {
                        uriArrayList.add(uri.toString());
                        Log.d("uri", uri.toString());
                        String imageMediaType = getContentResolver().getType(uri);
                        try {
                            WebRequest.sendPostImageRequest("/dongtai/image/upload", new HashMap<>(), getBitmapFromUri(uri), imageMediaType, new Function<HashMap<String, Object>, Void>() {
                                @Override
                                public Void apply(HashMap<String, Object> stringObjectHashMap) {

                                    //AddContentImage();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Log.d("url", stringObjectHashMap.toString());
                                            urlArrayList.add((String) stringObjectHashMap.get("url"));
                                            ChangeContentImage();
                                        }
                                    });
                                    return null;
                                }
                            });
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            });
    private SharedPreferences mPreferences;
    private String sharedPrefFile ="com.example.android.adddongtai";
    public DongTaiContent dongTaiContent;

    private static final String[] REQUIRED_PERMISSIONS = {"android.permission.WRITE_EXTERNAL_STORAGE", Manifest.permission.CAMERA};

    private void requestStoragePermission() {

        String[] permis = REQUIRED_PERMISSIONS;
        if (ContextCompat.checkSelfPermission(this, permis[0]) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, permis[1]) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permis, 0);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adddongtai);

        requestStoragePermission();

        headimg = findViewById(R.id.headimg);
        publisher = findViewById(R.id.publisher);
        content = findViewById(R.id.content);
        contentimg = findViewById(R.id.contentimg);
        all = findViewById(R.id.all);
        title = findViewById(R.id.title);

        // Restore preferences

        //DongTaiContent dongTaiContent1 = new DongTaiContent();
        mPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);

        //dongTaiContent = deserialize(mPreferences.getString("NewDongTai", serialize(dongTaiContent1)));
        dongTaiContent = new DongTaiContent();

        content.setText(dongTaiContent.content);
        title.setText(dongTaiContent.title);
        for(String base64: dongTaiContent.imagearray) {
            uriArrayList.add(base64ToUri(base64).toString());
        }

        ChangeContentImage();
    }

    public Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

    public String uriToBase64(Uri uri) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Bitmap bitmap = null;
        try {
            bitmap = getBitmapFromUri(uri);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
        return encoded;
    }

    public Uri base64ToUri(String string) {
        byte[] decode = Base64.decode(string, Base64.DEFAULT);
        Bitmap mBitmap = BitmapFactory.decodeByteArray(decode, 0, decode.length);
        return Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), mBitmap, null,null));
    }

    @Override
    protected void onPause() {
        super.onPause();

//        ArrayList<String> base64ArrayList = new ArrayList<String>();
//        for(String uri: uriArrayList) {
//            base64ArrayList.add(uriToBase64(Uri.parse(uri)));
//            Log.d("putin", uriToBase64(Uri.parse(uri)));
//        }
//
//        DongTaiContent dongTaiContent = new DongTaiContent(publisher.getText().toString() ,R.drawable.thussbuilding ,
//                "0:00", content.getText().toString(), 0, 0, 0,
//                title.getText().toString(), base64ArrayList);
//
//        SharedPreferences.Editor preferencesEditor = mPreferences.edit();
//        preferencesEditor.putString("NewDongTai", serialize(dongTaiContent));
//        preferencesEditor.apply();
    }

    public static String serialize(Serializable obj) {
        if(obj!=null) {
            ByteArrayOutputStream bos=null;
            ObjectOutputStream oos=null;
            try {
                bos=new ByteArrayOutputStream();
                oos=new ObjectOutputStream(bos);
                oos.writeObject(obj);
                byte[] bytes = bos.toByteArray();
                return Base64.encodeToString(bytes, Base64.DEFAULT);
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                try {
                    if(bos!=null){
                        bos.close();
                    }
                    if(oos!=null){
                        oos.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static DongTaiContent deserialize(String str) {
        if(!str.isEmpty()){
            ByteArrayInputStream bai=null;
            ObjectInputStream ois=null;
            try{
                bai=new ByteArrayInputStream(Base64.decode(str, Base64.DEFAULT));
                ois=new ObjectInputStream(bai);
                return (DongTaiContent) ois.readObject();
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                try {
                    if(ois!=null){
                        ois.close();
                    }
                    if(bai!=null){
                        bai.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public void ChangeContentImage() {
        contentimg.removeAllViews();//清空子视图 防止原有的子视图影响
        int columnCount = 3;
        int size = urlArrayList.size();
        //遍历集合 动态添加
        for (int i = 0; i < size; i++) {
            GridLayout.Spec rowSpec = GridLayout.spec(i / columnCount);//行数
            GridLayout.Spec columnSpec = GridLayout.spec(i % columnCount, 1.0f);//列数 列宽的比例 weight=1
            SquareImageView imageView = new SquareImageView(contentimg.getContext());
            Log.d("changecontent", urlArrayList.get(i));
            WebRequest.downloadImage(urlArrayList.get(i), bitmap -> {
                // 在这里处理下载完成后的逻辑，例如将图片显示在ImageView中
                imageView.setImageBitmap(bitmap);
                return null;
            });
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            //由于宽（即列）已经定义权重比例 宽设置为0 保证均分
            GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams(new ViewGroup.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT));
            layoutParams.rowSpec=rowSpec;
            layoutParams.columnSpec=columnSpec;
            layoutParams.setMargins(2, 2, 2, 2);
            contentimg.addView(imageView, layoutParams);
        }
        GridLayout.Spec rowSpec = GridLayout.spec(size / columnCount);//行数
        GridLayout.Spec columnSpec = GridLayout.spec(size % columnCount, 1.0f);//列数 列宽的比例 weight=1
        addphoto = new SquareImageView(contentimg.getContext());
        addphoto.setImageResource(R.drawable.baseline_add_24);
        addphoto.setScaleType(ImageView.ScaleType.FIT_XY);
        addphoto.setClickable(true);
        addphoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("click", "1");
                pickMedia.launch("image/*");
            }
        });
        addphoto.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        //由于宽（即列）已经定义权重比例 宽设置为0 保证均分
        GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams(new ViewGroup.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT));
        layoutParams.rowSpec=rowSpec;
        layoutParams.columnSpec=columnSpec;
        layoutParams.setMargins(2, 2, 2, 2);
        contentimg.addView(addphoto, layoutParams);
    }
    public void AddContentImage() {
        int columnCount = 3;
        int size = urlArrayList.size();
        int i = size - 1;
        GridLayout.Spec rowSpec = GridLayout.spec(i / columnCount);//行数
        GridLayout.Spec columnSpec = GridLayout.spec(i % columnCount, 1.0f);//列数 列宽的比例 weight=1
        SquareImageView imageView = new SquareImageView(contentimg.getContext());
//        Log.d("changecontent", urlArrayList.get(i));
//        WebRequest.downloadImage(urlArrayList.get(i), bitmap -> {
//            // 在这里处理下载完成后的逻辑，例如将图片显示在ImageView中
//            imageView.setImageBitmap(bitmap);
//            return null;
//        });
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        //由于宽（即列）已经定义权重比例 宽设置为0 保证均分
        GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams(new ViewGroup.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT));
        layoutParams.rowSpec=rowSpec;
        layoutParams.columnSpec=columnSpec;
        layoutParams.setMargins(2, 2, 2, 2);
        contentimg.addView(imageView, layoutParams);

        rowSpec = GridLayout.spec(size / columnCount);//行数
        columnSpec = GridLayout.spec(size % columnCount, 1.0f);//列数 列宽的比例 weight=1
        Log.d("col",columnSpec.toString());
        addphoto = new SquareImageView(contentimg.getContext());
        addphoto.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        //由于宽（即列）已经定义权重比例 宽设置为0 保证均分
        layoutParams = new GridLayout.LayoutParams(new ViewGroup.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT));
        layoutParams.rowSpec=rowSpec;
        layoutParams.columnSpec=columnSpec;
        layoutParams.setMargins(2, 2, 2, 2);
        contentimg.addView(addphoto, layoutParams);
    }
    public void BackToMain(View view) {
        finish();
    }
    public void Submit(View view) throws IOException{
        Date date = new Date();
        SimpleDateFormat dateFormat= new SimpleDateFormat("hh:mm");

        HashMap<String, String> requestArgs = new HashMap<>();
        requestArgs.put("title", title.getText().toString());
        requestArgs.put("content", content.getText().toString());
        requestArgs.put("tag", "normal");
        try {
            JSONArray jsonArray = JsonUtil.stringArrayListToJsonArray(urlArrayList);
            requestArgs.put("url_images", jsonArray.toString());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        Context context = this;


        WebRequest.sendPostRequest("/dongtai/create", requestArgs, (result) -> {
            String status = (String) result.get("status");
            if(status.equals("success")) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "创建成功", Toast.LENGTH_SHORT).show();
                    }
                });
                finish();
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "创建失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            return null;
        });

        DongTaiContent dongTaiContent = new DongTaiContent(publisher.getText().toString() ,R.drawable.thussbuilding ,
                dateFormat.format(date), content.getText().toString(), 0, 0, 0,
                title.getText().toString(), uriArrayList);

        Intent intent2 = new Intent(adddongtai.this, MainActivity.class);
        intent2.putExtra("result", dongTaiContent);
        setResult(RESULT_OK, intent2);

        finish();
    }
}