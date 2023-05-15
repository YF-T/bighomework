package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.HashMap;

public class UserInformationActivity extends AppCompatActivity {

    private EditText usernameEditView, introEditView, passwordEditText, emailEditText, ageEditText;
    private Spinner genderSpinner;
    private Button submitButton;
    private Button backButton;

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

        if(!username.equals(GlobalVariable.get("username", ""))) {
            submitButton.setVisibility(View.GONE);
            setEditable(false);
        } else {
            setEditable(true);
        }

    }

    // 获取个人信息的方法，返回一个HashMap
    private HashMap<String, String> getInfo() {
        HashMap<String, String> userInfo = new HashMap<>();
        // 通过你的逻辑获取个人信息，并将其存储在HashMap中
        userInfo.put("username", "John Doe");
        userInfo.put("intro", "Hello, I'm John.");
        userInfo.put("password", "password123");
        userInfo.put("email", "example@example.com");
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
        inputValues.put("intro", introEditView.getText().toString());
        inputValues.put("password", passwordEditText.getText().toString());
        inputValues.put("email", emailEditText.getText().toString());
        inputValues.put("age", ageEditText.getText().toString());
        inputValues.put("gender", genderSpinner.getSelectedItem().toString());
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
    public void onSaveButtonClick(View view) {
        // 获取所有输入框的值
        HashMap<String, String> inputValues = getAllInputValues();

        // 打印输入框的值
        for (String key : inputValues.keySet()) {
            String value = inputValues.get(key);
            System.out.println(key + ": " + value);
        }

        // 在这里你可以对输入框的值进行处理或保存操作
    }
}