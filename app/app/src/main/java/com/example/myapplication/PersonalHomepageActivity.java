package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Function;

public class PersonalHomepageActivity extends AppCompatActivity {

    private ImageView imageView;
    private TextView usernameTextView;
    private TextView descriptionTextView;
    private TextView followingTextView;
    private TextView followerTextView;
    // 关注数需要动态更新
    private RecyclerView recyclerView;
    private DongTaiAdapter dongTaiAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_homepage);

        imageView = findViewById(R.id.imageView);
        usernameTextView = findViewById(R.id.username);
        descriptionTextView = findViewById(R.id.description);
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

        try {
            WebRequest.sendGetRequest("/user/foreigninfo", new HashMap<>(), new Function<HashMap<String, Object>, Void>(){
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
                        String description = (String) info.get("description");
                        String image = (String) info.get("image");
                        String following = (String) info.get("following");
                        String follower = (String) info.get("follower");

                        // Update the UI with the retrieved user information
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                usernameTextView.setText(username);
                                descriptionTextView.setText(description);
                                followingTextView.setText(following);
                                followerTextView.setText(follower);
                                WebRequest.downloadImage(GlobalVariable.get("userimageurl", "/image/user/abc.jpg"), bitmap -> {
                                    // 在这里处理下载完成后的逻辑，例如将图片显示在ImageView中
                                    // 是抄的UserInformationActivity，这个代码很像只能返回默认头像的样子……
                                    imageView.setImageBitmap(bitmap);
                                    return null;
                                });
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

    // 对HashMap的使用参考UserInformationActivity.java
    private HashMap<String, String> getInfo() {
        HashMap<String, String> userInfo = new HashMap<>();
        userInfo.put("username", "John Doe");
        userInfo.put("intro", "Hello, I'm John.");
        userInfo.put("following", "关注：1");
        userInfo.put("follower", "粉丝：1");
        return userInfo;
    }
    private HashMap<String, String> getAllInputValues() {
        HashMap<String, String> inputValues = new HashMap<>();
        inputValues.put("username", usernameTextView.getText().toString());
        inputValues.put("description", descriptionTextView.getText().toString());
        inputValues.put("following", followingTextView.getText().toString());
        inputValues.put("follower", followerTextView.getText().toString());
        inputValues.put("uid", "");
        return inputValues;
    }

}