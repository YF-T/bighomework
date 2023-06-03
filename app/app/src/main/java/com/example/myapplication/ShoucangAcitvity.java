package com.example.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class ShoucangAcitvity extends Activity {

    public ArrayList<DongTaiContent> starList = new ArrayList<>();
    RecyclerView recyclerView;
    DongTaiAdapter adapter;

    public ShoucangAcitvity() {}  // empty construction funciton


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_shoucang);
        recyclerView = findViewById(R.id.recyclerview);
        adapter = new DongTaiAdapter(this, starList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        searchDongTai();

        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 在这里添加返回逻辑
                finish(); // 如果是在 Activity 中，可以使用 finish() 方法关闭当前 Activity
                // 或者使用其他返回操作，例如返回上一个 Fragment 或执行其他操作
            }
        });

    }

    public void searchDongTai() {
        Loading loading = new Loading(this);
        HashMap<String, String> args = new HashMap<>();
        args.put("key", "");
        args.put("tag", "");
        args.put("sortkey", "新发表");
        args.put("iffollow", "");
        args.put("type", "collect");
        try {
            WebRequest.sendGetRequest("/dongtai/search", args, hashMap -> {
                starList.clear();
                try {
                    ArrayList<Object> arrayList = JsonUtil.jsonArrayToArrayList((JSONArray) hashMap.get("dongtais"));
                    for (Object o: arrayList) {
                        starList.add(new DongTaiContent((HashMap<String, Object>) o));
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loading.dismiss();
                        adapter.notifyDataSetChanged();
                    }
                });
                return null;
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        loading.show();
    }
}
