package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
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
    private Button followOrUnfollow;
    private Button sendMessage;
    private ArrayList<DongTaiContent> dongTaiContents;
    private Button banButton;

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
        followOrUnfollow = findViewById(R.id.follow);
        sendMessage = findViewById(R.id.send_message);
        banButton = findViewById(R.id.banButton);

        // Set the user information
        imageView.setImageResource(R.drawable.touxiang);
        usernameTextView.setText("FrantGuo");
        followingTextView.setText("关注：1");
        followerTextView.setText("粉丝：1");

        // Set up the RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        dongTaiAdapter = new DongTaiAdapter(this, getDongTaiData());
        recyclerView.setAdapter(dongTaiAdapter);

        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean iflogin = false;
                GlobalVariable.get("iflogin", iflogin);
                if(!iflogin){
                    return;
                }
                // 如果已经登录，则进行跳转
                // 跳转到私信界面，还没连接
                String username = "Default";
                GlobalVariable.get("username", username);
                Intent intent = new Intent(v.getContext(), ChatActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("sender", usernameTextView.toString());
                bundle.putString("username", username);
                intent.putExtras(bundle);
                v.getContext().startActivity(intent);
            }
        });

        followOrUnfollow.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                boolean iflogin = false;
                GlobalVariable.get("iflogin", iflogin);
                if(!iflogin){
                    return;
                }
                // 结合当前状态判定是关注还是取关，需要调用
                String txt = followOrUnfollow.getText().toString();
                if(txt.equals("关注")){
                    // 点击表示从未关注变为关注
                    followOrUnfollow.setText("取关");
                }
                else{
                    followOrUnfollow.setText("关注");
                }
                HashMap<String, String> inputValues = getAllInputValues();
                try {
                    WebRequest.sendPostRequest("/user/follow", inputValues, new Function<HashMap<String, Object>, Void>() {
                        @Override
                        public Void apply(HashMap<String, Object> stringObjectHashMap) {
                            return null;
                        }
                    });
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                // 之后还要刷新一下
            }
        });

        banButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String txt = banButton.getText().toString();
                if(txt.equals("取消屏蔽")){
                    // 已经拉黑，则从黑名单中移出
                    banButton.setText("将TA屏蔽");
                }
                else{
                    banButton.setText("取消屏蔽");
                }
            }
        });

        // 直接抄了userinformationactivity，大概率抄的比较冗余，能跑就行
        try {
            HashMap<String, String> args = new HashMap<>();
            args.put("username", getIntent().getStringExtra("username"));
            WebRequest.sendPostRequest("/user/foreigninfo", args, new Function<HashMap<String, Object>, Void>(){
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
                        String following = "关注：" + Integer.toString((int) info.get("following"));
                        String follower = "粉丝：" + Integer.toString((int) info.get("follower"));

                        // Update the UI with the retrieved user information
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                usernameTextView.setText(username);
                                descriptionTextView.setText(description);
                                followingTextView.setText(following);
                                followerTextView.setText(follower);
                                WebRequest.downloadImage(image, bitmap -> {
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

        searchDongTai();
    }

    // Method to generate dummy data for RecyclerView
    private ArrayList<DongTaiContent> getDongTaiData() {
        dongTaiContents = new ArrayList<>();
//        dongTaiContents.add(
//                new DongTaiContent("FrantGuo", GlobalVariable.defaultImage, "14:00 Mar 23rd",
//                        "盛典即将开启，让世界更美。", 1,2,3,"微博盛典",
//                        new ArrayList<String>(Arrays.asList(GlobalVariable.defaultImage))));
//        dongTaiContents.add(
//                new DongTaiContent("FrantGuo", GlobalVariable.defaultImage, "15:25 Feb 25th",
//                        "感谢徐工集团的大力支持！\n体验很好，下次还来！", 7,10,2, "徐工集团拜访记",
//                        new ArrayList<String>(Arrays.asList(GlobalVariable.defaultImage,GlobalVariable.defaultImage))));
//        // Add dummy data here
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
        return inputValues;
    }

    public void searchDongTai() {
        HashMap<String, String> args = new HashMap<>();
        args.put("username", getIntent().getStringExtra("username"));
        try {
            WebRequest.sendGetRequest("/dongtai/otheruser/dongtais", args, hashMap -> {
                dongTaiContents.clear();
                try {
                    ArrayList<Object> arrayList = JsonUtil.jsonArrayToArrayList((JSONArray) hashMap.get("dongtais"));
                    for (Object o: arrayList) {
                        dongTaiContents.add(new DongTaiContent((HashMap<String, Object>) o));
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dongTaiAdapter.notifyDataSetChanged();
                    }
                });
                return null;
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}