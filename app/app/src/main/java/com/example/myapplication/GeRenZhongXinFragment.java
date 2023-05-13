package com.example.myapplication;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;

/**
 * A simple {@link Fragment} subclass.
 */
public class GeRenZhongXinFragment extends Fragment {

    private RelativeLayout personal_info;
    private RelativeLayout message;
    private RelativeLayout login;
    private RelativeLayout register;
    private RelativeLayout logout;
    private ImageView user_image;
    private TextView user_name;
    private TextView user_email;
    private ActivityResultLauncher<Intent> resultLauncherForRegisterAndLogin;

    public GeRenZhongXinFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_ge_ren_zhong_xin, container, false);

        personal_info = view.findViewById(R.id.personal_info);
        message = view.findViewById(R.id.message);
        login = view.findViewById(R.id.login);
        register = view.findViewById(R.id.register);
        logout = view.findViewById(R.id.logout);
        user_image = view.findViewById(R.id.user_image);
        user_name = view.findViewById(R.id.user_name);
        user_email = view.findViewById(R.id.user_email);

        resultLauncherForRegisterAndLogin = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            changeLayoutByStatus(view);
        });

        personal_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO
            }
        });
        message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), LoginAndRegisterActivity.class);
                intent.putExtra("mode", "login");
                resultLauncherForRegisterAndLogin.launch(intent);
            }
        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), LoginAndRegisterActivity.class);
                intent.putExtra("mode", "register");
                resultLauncherForRegisterAndLogin.launch(intent);
            }
        });
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GlobalVariable.set("iflogin", false);
                GlobalVariable.set("jwt", "");
                GlobalVariable.set("username", "");
                GlobalVariable.set("useremail", "");
                changeLayoutByStatus(view);
            }
        });

        changeLayoutByStatus(view);

        return view;
    }

    private void changeLayoutByStatus(View view) {
        Boolean iflogin = GlobalVariable.get("iflogin", false);
        if(iflogin) {
            personal_info.setVisibility(View.VISIBLE);
            message.setVisibility(View.VISIBLE);
            login.setVisibility(View.GONE);
            register.setVisibility(View.GONE);
            logout.setVisibility(View.VISIBLE);
            user_name.setText(GlobalVariable.get("username", "default"));
            user_email.setText(GlobalVariable.get("useremail", "default"));
            WebRequest.downloadImage(GlobalVariable.get("userimageurl", "/image/user/abc.jpg"), bitmap -> {
                // 在这里处理下载完成后的逻辑，例如将图片显示在ImageView中
                user_image.setImageBitmap(bitmap);
                return null;
            });
        } else {
            personal_info.setVisibility(View.GONE);
            message.setVisibility(View.GONE);
            login.setVisibility(View.VISIBLE);
            register.setVisibility(View.VISIBLE);
            logout.setVisibility(View.GONE);
            user_image.setImageResource(R.drawable.ic_logoutimage_foreground);
            user_name.setText("未登录");
            user_email.setText("");
        }
    }
}