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
                new DongTaiContent("FrantGuo", R.drawable.touxiang, "19:56 Feb 8th",
                        "哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈", 100,8,3, "有趣的动漫分享",
                        new ArrayList<Integer>(Arrays.asList(R.drawable.manhua1, R.drawable.manhua2,
                                R.drawable.manhua3, R.drawable.manhua4, R.drawable.manhua5, R.drawable.manhua6)), this));


        setContentView(R.layout.activity_shoucang);
        recyclerView = findViewById(R.id.recyclerview);
        adapter = new DongTaiAdapter(this, starList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


    }
}
