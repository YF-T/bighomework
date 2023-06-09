package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
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
    private int fans;
    private Button backButton;

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
        backButton = findViewById(R.id.back);

        // Set the user information
        imageView.setImageResource(R.drawable.circle_profile);
        usernameTextView.setText("   ");
        followingTextView.setText("关注：  ");
        followerTextView.setText("粉丝：  ");

        // Set up the RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        dongTaiAdapter = new DongTaiAdapter(this, getDongTaiData());
        recyclerView.setAdapter(dongTaiAdapter);

        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean iflogin = false;
                iflogin = GlobalVariable.get("iflogin", iflogin);
                if(!iflogin){
                    return;
                }
                // 如果已经登录，则进行跳转
                // 跳转到私信界面，还没连接
                String username = "Default";
                username = GlobalVariable.get("username", "");
                Intent intent = new Intent(v.getContext(), ChatActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("sender", usernameTextView.getText().toString());
                bundle.putString("username", username);
                intent.putExtras(bundle);
                v.getContext().startActivity(intent);
            }
        });

        followOrUnfollow.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                boolean iflogin = true;
                iflogin = GlobalVariable.get("iflogin", iflogin);
                if(!iflogin){
                    return;
                }
                HashMap<String, String> inputValues = getAllInputValues();
                try {
                    WebRequest.sendPostRequest("/user/follow", inputValues, new Function<HashMap<String, Object>, Void>() {
                        @Override
                        public Void apply(HashMap<String, Object> stringObjectHashMap) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if((boolean) stringObjectHashMap.get("bool_follow")){
                                        // 点击表示从未关注变为关注
                                        followOrUnfollow.setText("取关");
                                        v.setBackgroundColor(ContextCompat.getColor(v.getContext(), R.color.tint_blue));
                                        fans += 1;
                                        String follower = "粉丝：" + Integer.toString(fans);
                                        followerTextView.setText(follower);
                                    }
                                    else{
                                        followOrUnfollow.setText("关注");
                                        v.setBackgroundColor(ContextCompat.getColor(v.getContext(), R.color.button_blue));
                                        fans -= 1;
                                        String follower = "粉丝：" + Integer.toString(fans);
                                        followerTextView.setText(follower);
                                    }
                                }
                            });
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
                boolean iflogin = true;
                iflogin = GlobalVariable.get("iflogin", iflogin);
                if(!iflogin){
                    return;
                }
                HashMap<String, String> inputValues = getAllInputValues();
                try {
                    WebRequest.sendPostRequest("/user/ban", inputValues, new Function<HashMap<String, Object>, Void>() {
                        @Override
                        public Void apply(HashMap<String, Object> stringObjectHashMap) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if((boolean) stringObjectHashMap.get("bool_banned")){
                                        // 已经拉黑，则从黑名单中移出
                                        banButton.setText("取消屏蔽");
                                    }
                                    else{
                                        banButton.setText("将TA屏蔽");
                                    }
                                }
                            });
                            return null;
                        }
                    });
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                // 之后还要刷新一下
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
                        fans = (int) info.get("follower");
                        String following = "关注：" + Integer.toString((int) info.get("following"));
                        String follower = "粉丝：" + Integer.toString((int) info.get("follower"));
                        String followOrUnfollowtext;
                        String banbuttontext;
                        if ((boolean) info.get("bool_follow")) {
                            followOrUnfollowtext = "取关";
                        } else {
                            followOrUnfollowtext = "关注";
                        }
                        if ((boolean) info.get("bool_ban")) {
                            banbuttontext = "取消屏蔽";
                        } else {
                            banbuttontext = "将TA屏蔽";
                        }

                        // Update the UI with the retrieved user information
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                usernameTextView.setText(username);
                                descriptionTextView.setText(description);
                                followingTextView.setText(following);
                                followerTextView.setText(follower);
                                followOrUnfollow.setText(followOrUnfollowtext);
                                if(followOrUnfollowtext.equals("取关")){
                                    followOrUnfollow.setBackgroundColor(getResources().getColor(R.color.tint_blue));
                                }
                                else{
                                    followOrUnfollow.setBackgroundColor(getResources().getColor(R.color.button_blue));
                                }
                                banButton.setText(banbuttontext);
                                if(banbuttontext.equals("取消屏蔽")){
                                    banButton.setBackgroundColor(getResources().getColor(R.color.tint_blue));
                                }
                                else{
                                    banButton.setBackgroundColor(getResources().getColor(R.color.button_blue));
                                }
                                WebRequest.downloadImage(image, bitmap -> {
                                    int width = bitmap.getWidth();
                                    int height = bitmap.getHeight();
                                    // 计算正方形的边长
                                    int size = Math.min(width, height);
                                    // 计算裁剪的起始位置
                                    int x = (width - size) / 2;
                                    int y = (height - size) / 2;
                                    Bitmap squareBitmap = Bitmap.createBitmap(bitmap, x, y, size, size);
                                    Bitmap circularBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
                                    Canvas canvas = new Canvas(circularBitmap);
                                    Paint paint = new Paint();
                                    Rect rect = new Rect(0, 0, size, size);
                                    RectF rectF = new RectF(rect);
                                    float radius = size / 2f;
                                    paint.setAntiAlias(true);
                                    canvas.drawARGB(0, 0, 0, 0);
                                    canvas.drawCircle(radius, radius, radius, paint);
                                    paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
                                    canvas.drawBitmap(squareBitmap, rect, rect, paint);

                                    imageView.post(() -> {
                                        imageView.setImageBitmap(circularBitmap);
                                        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                    });
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
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        searchDongTai();
    }

    // Method to generate dummy data for RecyclerView
    private ArrayList<DongTaiContent> getDongTaiData() {
        dongTaiContents = new ArrayList<>();
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