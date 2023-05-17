package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

public class PersonalHomepageActivity extends AppCompatActivity {

    private ImageView imageView;
    private TextView usernameTextView;
    private TextView followingTextView;
    private TextView followerTextView;
    private RecyclerView recyclerView;
    private DongTaiAdapter dongTaiAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_homepage);

        imageView = findViewById(R.id.imageView);
        usernameTextView = findViewById(R.id.username);
        followingTextView = findViewById(R.id.following);
        followerTextView = findViewById(R.id.follower);
        recyclerView = findViewById(R.id.recyclerview);

        // Set the user information
        imageView.setImageResource(R.drawable.touxiang);
        usernameTextView.setText("FrantGuo");
        followingTextView.setText("关注：1");
        followerTextView.setText("粉丝：1");

        // Set up the RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        dongTaiAdapter = new DongTaiAdapter(this, getDongTaiData());
        recyclerView.setAdapter(dongTaiAdapter);
    }

    // Method to generate dummy data for RecyclerView
    private ArrayList<DongTaiContent> getDongTaiData() {
        ArrayList<DongTaiContent> dongTaiContents = new ArrayList<>();
        dongTaiContents.add(
                new DongTaiContent("FrantGuo", R.drawable.touxiang, "14:00 Mar 23rd",
                        "盛典即将开启，让世界更美。", 1,2,3,"微博盛典",
                        new ArrayList<Integer>(Arrays.asList(R.drawable.touxiang)), this));
        dongTaiContents.add(
                new DongTaiContent("FrantGuo", R.drawable.touxiang, "15:25 Feb 25th",
                        "感谢徐工集团的大力支持！\n体验很好，下次还来！", 7,10,2, "徐工集团拜访记",
                        new ArrayList<Integer>(Arrays.asList(R.drawable.xugongjituan1, R.drawable.xugongjituan2,
                                R.drawable.xugongjituan3, R.drawable.xugongjituan4, R.drawable.xugongjituan6)), this));
        // Add dummy data here
        return dongTaiContents;
    }
}