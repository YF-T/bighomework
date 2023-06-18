package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.noties.markwon.Markwon;

public class dongtai extends AppCompatActivity {

    public ImageView headimg;
    public TextView publisher;
    public TextView content;
    public GridLayout contentimg;
    public TextView time;
    public TextView comment;
    public TextView like;
    public TextView collect;
    public LinearLayout all;
    public TextView title;

    private RecyclerView recyclerView;
    private CommentAdapter commentAdapter;
    private TextView tag;
    private TextView position;
    private TextView iffollow;
    private List<CommentContent> comments = new ArrayList<>();
    private DongTaiContent dongTaiContent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dongtai);
        Intent intent = this.getIntent();
        dongTaiContent = (DongTaiContent)intent.getSerializableExtra("DongTaiContent");

        headimg = findViewById(R.id.headimg);
        publisher = findViewById(R.id.publisher);
        content = findViewById(R.id.content);
        contentimg = findViewById(R.id.contentimg);
        time = findViewById(R.id.time);
        comment = findViewById(R.id.comment);
        like = findViewById(R.id.like);
        collect = findViewById(R.id.collect);
        all = findViewById(R.id.all);
        title = findViewById(R.id.title);
        recyclerView = findViewById(R.id.comment_items);
        tag = findViewById(R.id.tag);
        position = findViewById(R.id.position);
        iffollow = findViewById(R.id.ifguanzhu);

        WebRequest.setImageByUrl(headimg, dongTaiContent.headimg);

        publisher.setText(dongTaiContent.publisher);
        content.setText(dongTaiContent.content);
        time.setText(dongTaiContent.time);
        comment.setText(String.format("评论(%d)", dongTaiContent.comment));
        like.setText(String.format("点赞(%d)", dongTaiContent.like));
        if (dongTaiContent.bool_thumb) {
            like.setTextColor(Color.BLUE);
        }
        collect.setText(String.format("收藏(%d)", dongTaiContent.collect));
        if (dongTaiContent.bool_collect) {
            collect.setTextColor(Color.BLUE);
        }
        title.setText(String.format("# %s", dongTaiContent.title));
        tag.setText(dongTaiContent.tag);
        String tmp = dongTaiContent.position;
        if(tmp.equals("添加当前位置")){
            position.setVisibility(View.INVISIBLE);
        }
        else {position.setText(dongTaiContent.position);}
        if(dongTaiContent.bool_follow) {
            iffollow.setText("已关注");
        }
        else {
            iffollow.setText("未关注");
        }
        Markwon markwon = Markwon.builder(this).build();
        markwon.setMarkdown(content, dongTaiContent.content);

        ChangeContentImage(dongTaiContent.imagearray);

        commentAdapter = new CommentAdapter(comments);

        recyclerView.setLayoutManager(new LinearLayoutManager(dongtai.this));
        recyclerView.setAdapter(commentAdapter);

        Context context = this;
        headimg.setClickable(true);
        headimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PersonalHomepageActivity.class);
                intent.putExtra("username", publisher.getText().toString());
                startActivity(intent);
                overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
            }
        });
        comment.setOnClickListener(view -> {
            writeComment(view);
        });
        like.setOnClickListener(view -> {
            HashMap<String, String> args = new HashMap<>();
            args.put("id", Integer.toString(dongTaiContent.id));
            try {
                WebRequest.sendPostRequest("/dongtai/support", args, hashMap -> {
                    dongTaiContent.like = (int) hashMap.get("num_thumbing_users");
                    dongTaiContent.bool_thumb = (boolean) hashMap.get("bool_support");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            like.setText(String.format("点赞(%d)", dongTaiContent.like));
                            if (dongTaiContent.bool_thumb) {
                                like.setTextColor(Color.BLUE);
                            } else {
                                like.setTextColor(Color.parseColor("#666666"));
                            }
                        }
                    });
                    return null;
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        collect.setOnClickListener(view -> {
            HashMap<String, String> args = new HashMap<>();
            args.put("id", Integer.toString(dongTaiContent.id));
            try {
                WebRequest.sendPostRequest("/dongtai/collect", args, hashMap -> {
                    dongTaiContent.collect = (int) hashMap.get("num_collect_users");
                    dongTaiContent.bool_collect = (boolean) hashMap.get("bool_collect");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            collect.setText(String.format("收藏(%d)", dongTaiContent.collect));
                            if (dongTaiContent.bool_collect) {
                                collect.setTextColor(Color.BLUE);
                            } else {
                                collect.setTextColor(Color.parseColor("#666666"));
                            }
                        }
                    });
                    return null;
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        getComments();
    }

    private void getComments() {
        HashMap<String, String> args = new HashMap<>();
        args.put("id", Integer.toString(dongTaiContent.id));
        try {
            WebRequest.sendGetRequest("/dongtai/dongtai", args, hashMap -> {
                try {
                    dongTaiContent = new DongTaiContent(JsonUtil.jsonObjectToHashMap((JSONObject) hashMap.get("dongtai")));
                    comments.clear();
                    ArrayList<Object> arrayList = JsonUtil.jsonArrayToArrayList((JSONArray) hashMap.get("comments"));
                    boolean iffollow1 = (boolean) hashMap.get("iffollow");
                    for (Object o: arrayList) {
                        HashMap<String, Object> commentHashMap = (HashMap<String, Object>) o;
                        comments.add(new CommentContent((String) commentHashMap.get("author"), (String) commentHashMap.get("content")));
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (dongTaiContent.bool_thumb) {
                                like.setTextColor(Color.BLUE);
                            }
                            if (dongTaiContent.bool_collect) {
                                collect.setTextColor(Color.BLUE);
                            }
                            if (iffollow1) {
                                iffollow.setText("已关注");
                            } else{
                                iffollow.setText("未关注");
                            }
                        }
                    });
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        commentAdapter.notifyDataSetChanged();
                    }
                });
                return null;
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void ChangeContentImage(ArrayList<String> imagearray) {
        contentimg.removeAllViews();//清空子视图 防止原有的子视图影响
        int columnCount = 3;
        int size = imagearray.size();
        //遍历集合 动态添加
        if (imagearray.size() == 1 && imagearray.get(0).endsWith("mp4")) {
            VideoView videoView = new VideoView(contentimg.getContext());
            //加载网络视频，记得适配 6.0,7.0,9.0
            String videoPath = WebRequest.baseUrl + imagearray.get(0);
            videoView.setVideoPath(videoPath);

            videoView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            //由于宽（即列）已经定义权重比例 宽设置为0 保证均分
            ViewGroup.LayoutParams layoutParams = videoView.getLayoutParams();
            layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            layoutParams.height = 600;
            videoView.setLayoutParams(layoutParams);
            contentimg.addView(videoView);
            videoView.requestFocus();
            videoView.start();
            return;
        }
        for (int i = 0; i < size; i++) {
            GridLayout.Spec rowSpec = GridLayout.spec(i / columnCount);//行数
            GridLayout.Spec columnSpec = GridLayout.spec(i % columnCount, 1.0f);//列数 列宽的比例 weight=1
            SquareImageView imageView = new SquareImageView(contentimg.getContext());

            WebRequest.setImageByUrl(imageView, imagearray.get(i));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            //由于宽（即列）已经定义权重比例 宽设置为0 保证均分
            GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams(new ViewGroup.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT));
            layoutParams.rowSpec=rowSpec;
            layoutParams.columnSpec=columnSpec;
            layoutParams.setMargins(2, 2, 2, 2);
            contentimg.addView(imageView, layoutParams);
        }
    }
    public void BackToMain(View view) {
        finish();
    }

    public void writeComment(View view) {
        Loading loading = new Loading(this);
        EditText writecomment = new EditText(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("请输入评论");
        builder.setView(writecomment);
        builder.setPositiveButton("提交", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String strcomment = writecomment.getText().toString();
                Log.d("comment", strcomment);
                HashMap<String, String> args = new HashMap<>();
                args.put("content", strcomment);
                args.put("id", Integer.toString(dongTaiContent.id));
                try {
                    WebRequest.sendPostRequest("/dongtai/comment/create", args, hashMap -> {
                        comments.add(0, new CommentContent(GlobalVariable.get("username", "unknown"), strcomment));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                commentAdapter.notifyDataSetChanged();
                                comment.setText(String.format("评论(%d)", hashMap.get("num_comment")));
                                loading.dismiss();
                            }
                        });
                        return null;
                    });
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                dialogInterface.dismiss();
                loading.show();
            }
        });
        builder.setNegativeButton("返回", null);
        builder.show();
    }
    public void shareDongTai(View v){
        String title = "分享到...";
        Intent shareIntent = new Intent();

        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, "标题："+this.title.getText().toString()+"\n内容："+content.getText().toString());
//        shareIntent.putExtra(Intent.EXTRA_TEXT, "内容："+content.getText().toString());

        shareIntent.setType("text/plain");
        startActivity(Intent.createChooser(shareIntent,title));
    }
}