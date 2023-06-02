package com.example.myapplication;

import android.app.Activity;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;

public class ShoucangAcitvity extends Activity {

    public ArrayList<DongTaiContent> starList = new ArrayList<>();
    RecyclerView recyclerView;
    DongTaiAdapter adapter;

    public ShoucangAcitvity() {}  // empty construction funciton


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        // 此处应有初始化，暂时不写死
        starList.add(
                new DongTaiContent("FrantGuo", GlobalVariable.defaultImage, "14:00 Mar 23rd",
                        "盛典即将开启，让世界更美。", 1,2,3,"微博盛典",
                        new ArrayList<String>(Arrays.asList(GlobalVariable.defaultImage))));


        setContentView(R.layout.activity_shoucang);
        recyclerView = findViewById(R.id.recyclerview);
        adapter = new DongTaiAdapter(this, starList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


    }
}
