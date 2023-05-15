package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dongtai);
        Intent intent = this.getIntent();
        DongTaiContent dongTaiContent=(DongTaiContent)intent.getSerializableExtra("DongTaiContent");

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

        headimg.setImageResource(dongTaiContent.headimg);

        publisher.setText(dongTaiContent.publisher);
        content.setText(dongTaiContent.content);
        time.setText(dongTaiContent.time);
        comment.setText(String.format("评论(%d)", dongTaiContent.comment));
        like.setText(String.format("点赞(%d)", dongTaiContent.like));
        collect.setText(String.format("收藏(%d)", dongTaiContent.collect));
        title.setText(String.format("# %s", dongTaiContent.title));
        ChangeContentImage(dongTaiContent.imagearray);

        commentAdapter = new CommentAdapter(getComments());

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(commentAdapter);
    }

    private List<CommentContent> getComments() {
        List<CommentContent> comments = new ArrayList<>();

        // 添加两三条评论到列表中
        CommentContent comment1 = new CommentContent("John", "Great post!");
        CommentContent comment2 = new CommentContent("Emma", "I agree with you.");
        CommentContent comment3 = new CommentContent("Michael", "Well written!");

        comments.add(comment1);
        comments.add(comment2);
        comments.add(comment3);

        return comments;
    }


    public void ChangeContentImage(ArrayList<String> imagearray) {
        contentimg.removeAllViews();//清空子视图 防止原有的子视图影响
        int columnCount = 3;
        int size = imagearray.size();
        //遍历集合 动态添加
        for (int i = 0; i < size; i++) {
            GridLayout.Spec rowSpec = GridLayout.spec(i / columnCount);//行数
            GridLayout.Spec columnSpec = GridLayout.spec(i % columnCount, 1.0f);//列数 列宽的比例 weight=1
            SquareImageView imageView = new SquareImageView(contentimg.getContext());
            imageView.setImageURI(Uri.parse(imagearray.get(i)));
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
}