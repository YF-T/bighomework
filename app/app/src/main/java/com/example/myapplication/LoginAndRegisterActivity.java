package com.example.myapplication;

import android.content.Context;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.myapplication.databinding.ActivityLoginAndRegisterBinding;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.util.HashMap;

public class LoginAndRegisterActivity extends AppCompatActivity {

    TextView loginLabel;
    TextInputEditText usernameInput;
    TextInputEditText passwordInput;
    Button submitButton;
    String mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_and_register);

        loginLabel = findViewById(R.id.login_label);
        usernameInput = findViewById(R.id.username);
        passwordInput = findViewById(R.id.password);
        submitButton = findViewById(R.id.submit);

        mode = getIntent().getStringExtra("mode");

        if(mode.equals("login")) {
            loginLabel.setText("登录");
        } else {
            loginLabel.setText("注册");
        }
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = usernameInput.getText().toString();
                String password = passwordInput.getText().toString();
                try {
                    submit(username, password);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void submit(String username, String password) throws IOException {
        HashMap<String, String> requestArgs = new HashMap<>();
        requestArgs.put("username", username);
        requestArgs.put("password", password);
        Context context = this;
        if(mode.equals("login")) {
            WebRequest.sendGetRequest("/user/login", requestArgs, (result) -> {
                String status = (String) result.get("status");
                if(status.equals("success")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "登录成功", Toast.LENGTH_SHORT).show();
                        }
                    });
                    GlobalVariable.set("jwt", (String) result.get("jwt"));
                    GlobalVariable.set("iflogin", true);
                    GlobalVariable.set("username", username);
                    GlobalVariable.set("useremail", "12345678@qq.com");
                    GlobalVariable.set("userimageurl", (String) result.get("image"));
                    finish();
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "登录失败", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                return null;
            });
        } else {
            WebRequest.sendPostRequest("/user/register", requestArgs, (result) -> {
                String status = (String) result.get("status");
                if(status.equals("success")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "注册成功", Toast.LENGTH_SHORT).show();
                        }
                    });
                    finish();
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "注册失败", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                return null;
            });
        }
    }
}