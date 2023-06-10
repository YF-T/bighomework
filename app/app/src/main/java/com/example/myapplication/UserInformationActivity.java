package com.example.myapplication;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

public class UserInformationActivity extends AppCompatActivity {

    private EditText usernameEditView, introEditView, passwordEditText, emailEditText, ageEditText;
    private ImageView imageView;
    private Spinner genderSpinner;
    private Button submitButton;
    private Button backButton;
    private Bitmap tochange = null;
    private String type = null;

    public ActivityResultLauncher<String> pickMedia =
            registerForActivityResult(new ActivityResultContracts.GetMultipleContents(), new ActivityResultCallback<List<Uri>>() {
                @Override
                public void onActivityResult(List<Uri> result) {
                    for(Uri uri: result) {
                        imageView.setImageURI(uri);
                        ParcelFileDescriptor parcelFileDescriptor = null;
                        try {
                            parcelFileDescriptor = getContentResolver().openFileDescriptor(uri, "r");
                            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                            Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                            parcelFileDescriptor.close();
                            tochange = image;
                            type = getContentResolver().getType(uri);
                        } catch (FileNotFoundException e) {
                            throw new RuntimeException(e);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_information);

        String username = getIntent().getStringExtra("username");


        // 初始化布局中的控件
        usernameEditView = findViewById(R.id.username_edittext);
        introEditView = findViewById(R.id.introduction_edittext);
        passwordEditText = findViewById(R.id.password_edittext);
        emailEditText = findViewById(R.id.email_edittext);
        ageEditText = findViewById(R.id.age_edittext);
        genderSpinner = findViewById(R.id.gender_spinner);
        imageView = findViewById(R.id.avatar_imageview);

        // 获取个人信息
        HashMap<String, String> userInfo = getInfo();

        // 显示个人信息
        usernameEditView.setText(userInfo.get("username"));
        introEditView.setText(userInfo.get("intro"));
        passwordEditText.setText(userInfo.get("password"));
        emailEditText.setText(userInfo.get("email"));
        ageEditText.setText(userInfo.get("age"));

        // 设置性别Spinner的选中项
        String gender = userInfo.get("gender");
        if (gender != null) {
            int position = getPositionForGender(gender);
            genderSpinner.setSelection(position);
        }

        submitButton = findViewById(R.id.submit_button);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 执行提交按钮点击后的操作
                try {
                    onSaveButtonClick(v);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                finish(); // 结束当前的Activity
            }
        });

        backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 执行返回按钮点击后的操作
                finish(); // 结束当前的Activity
            }
        });

        imageView.setClickable(true);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickMedia.launch("image/*");
            }
        });

        if(!username.equals(GlobalVariable.get("username", ""))) {
            submitButton.setVisibility(View.GONE);
            setEditable(false);
        } else {
            setEditable(true);
        }

        // Make the API request to get the user information
        try {
            WebRequest.sendGetRequest("/user/tobeupdated", new HashMap<>(), new Function<HashMap<String, Object>, Void>() {
                @Override
                public Void apply(HashMap<String, Object> response) {
                    if (response != null && (boolean) response.get("status")) {
                        Log.d("info", response.get("info").toString());
                        HashMap<String, Object> info = null;
                        try {
                            info = JsonUtil.jsonObjectToHashMap((JSONObject)response.get("info"));
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }

                        // Extract the user information from the response
                        String username = (String) info.get("name");
                        String password = (String) info.get("password");
                        int age = (int) info.get("age");
                        String sex = (String) info.get("sex");
                        String email = (String) info.get("email");
                        String description = (String) info.get("description");
                        String image = (String) info.get("image");

                        // Update the UI with the retrieved user information
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                usernameEditView.setText(username);
                                passwordEditText.setText("******");
                                ageEditText.setText(String.valueOf(age));
                                emailEditText.setText(email);
                                introEditView.setText(description);
                                WebRequest.setImageByUrl(imageView, GlobalVariable.get("userimageurl", "/image/user/abc.jpg"));
//                                WebRequest.downloadImage(GlobalVariable.get("userimageurl", "/image/user/abc.jpg"), bitmap -> {
//                                    // 在这里处理下载完成后的逻辑，例如将图片显示在ImageView中
//                                    imageView.setImageBitmap(bitmap);
//                                    return null;
//                                });
                                if(sex.equals("M")) {
                                    genderSpinner.setSelection(0);
                                } else {
                                    genderSpinner.setSelection(1);
                                }
                            }
                        });
                    }
                    return null;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // 获取个人信息的方法，返回一个HashMap
    private HashMap<String, String> getInfo() {
        HashMap<String, String> userInfo = new HashMap<>();
        // 通过你的逻辑获取个人信息，并将其存储在HashMap中
        userInfo.put("username", "John Doe");
        userInfo.put("intro", "Hello, I'm John.");
        userInfo.put("password", "password123");
        userInfo.put("email", "example@163.com");
        userInfo.put("age", "25");
        userInfo.put("gender", "男");
        return userInfo;
    }

    // 根据性别获取Spinner中的选中位置
    private int getPositionForGender(String gender) {
        String[] genders = getResources().getStringArray(R.array.gender_options);
        for (int i = 0; i < genders.length; i++) {
            if (gender.equals(genders[i])) {
                return i;
            }
        }
        return 0; // 默认返回第一项
    }

    // 获取所有输入框的值并打包成HashMap
    private HashMap<String, String> getAllInputValues() {
        HashMap<String, String> inputValues = new HashMap<>();
        inputValues.put("username", usernameEditView.getText().toString());
        inputValues.put("description", introEditView.getText().toString());
        inputValues.put("password", passwordEditText.getText().toString());
        inputValues.put("email", emailEditText.getText().toString());
        inputValues.put("age", ageEditText.getText().toString());
        inputValues.put("sex", genderSpinner.getSelectedItem().toString());
        inputValues.put("uid", "");
        inputValues.put("ifChangeImage", "0");
        return inputValues;
    }

    // 设置输入框是否可编辑
    private void setEditable(boolean isEditable) {
        introEditView.setEnabled(isEditable);
        passwordEditText.setEnabled(isEditable);
        emailEditText.setEnabled(isEditable);
        ageEditText.setEnabled(isEditable);
        genderSpinner.setEnabled(isEditable);
    }

    // 点击保存按钮的事件处理
    public void onSaveButtonClick(View view) throws IOException {
        // 获取所有输入框的值
        HashMap<String, String> inputValues = getAllInputValues();

        if (tochange != null) {
            inputValues.put("ifChangeImage", "1");
            WebRequest.sendPostImageRequest("/user/updatemyinfo", inputValues, tochange, type, new Function<HashMap<String, Object>, Void>() {
                @Override
                public Void apply(HashMap<String, Object> stringObjectHashMap) {
                    GlobalVariable.set("userimageurl", (String) stringObjectHashMap.get("url"));
                    return null;
                }
            });
            return;
        }

        WebRequest.sendPostRequest("/user/updatemyinfo", inputValues, new Function<HashMap<String, Object>, Void>() {
            @Override
            public Void apply(HashMap<String, Object> stringObjectHashMap) {
                return null;
            }
        });

        // 打印输入框的值
        for (String key : inputValues.keySet()) {
            String value = inputValues.get(key);
            System.out.println(key + ": " + value);
        }

        // 在这里可以对输入框的值进行处理或保存操作
    }
}