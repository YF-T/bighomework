package com.example.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import io.noties.markwon.Markwon;

public class DongTaiAdapter extends RecyclerView.Adapter<DongTaiViewHolder> {

    private final LayoutInflater inflater;
    private final ArrayList<DongTaiContent> dongTaiContents;

    public DongTaiAdapter(Context context, ArrayList<DongTaiContent> contentlist) {
        inflater = LayoutInflater.from(context);
        dongTaiContents = contentlist;
    }

    @NonNull
    @Override
    public DongTaiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate an item view.
        View mItemView = inflater.inflate(
                R.layout.item_list, parent, false);
        DongTaiViewHolder dongTaiViewHolder = new DongTaiViewHolder(mItemView, this);
        dongTaiViewHolder.all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), dongtai.class);
                DongTaiContent dongTaiContent = dongTaiContents.get(dongTaiViewHolder.getAdapterPosition());
                Bundle bundle = new Bundle();
                bundle.putSerializable("DongTaiContent", dongTaiContent);
                intent.putExtras(bundle);
                view.getContext().startActivity(intent);
                ((Activity)view.getContext()).overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
            }
        });
        return dongTaiViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull DongTaiViewHolder holder, int position) {
        // Retrieve the data for that position.
        DongTaiContent dongTaiContent = dongTaiContents.get(position);
        // Add the data to the view holder.
        WebRequest.setImageByUrl(holder.headimg, dongTaiContent.headimg);
        holder.publisher.setText(dongTaiContent.publisher);
        holder.content.setText(dongTaiContent.content);
        holder.time.setText(dongTaiContent.time);
        holder.comment.setText(String.format("评论(%d)", dongTaiContent.comment));
        holder.like.setText(String.format("点赞(%d)", dongTaiContent.like));
        holder.collect.setText(String.format("收藏(%d)", dongTaiContent.collect));
        holder.title.setText(String.format("# %s", dongTaiContent.title));
        holder.tag.setText(dongTaiContent.tag);
        if (dongTaiContent.bool_thumb) {
            holder.like.setTextColor(Color.BLUE);
        }
        if (dongTaiContent.bool_collect) {
            holder.like.setTextColor(Color.BLUE);
        }
        String tmp = dongTaiContent.position;
        if(tmp.equals("添加当前位置")){
            holder.position.setVisibility(View.GONE);
        }
        else {holder.position.setText(dongTaiContent.position);}

        Markwon markwon = Markwon.builder(inflater.getContext()).build();
        markwon.setMarkdown(holder.content, dongTaiContent.content);

        holder.ChangeContentImage(dongTaiContent.imagearray);
    }

    @Override
    public int getItemCount() {
        return dongTaiContents.size();
    }
}

class DongTaiViewHolder extends RecyclerView.ViewHolder {
    public final ImageView headimg;
    public final TextView publisher;
    public final TextView content;
    public final GridLayout contentimg;
    public final TextView time;
    public final TextView comment;
    public final TextView like;
    public final TextView collect;
    public final LinearLayout all;
    public final TextView title;
    public final TextView tag;
    public final TextView position;

    public DongTaiViewHolder(@NonNull View itemView, DongTaiAdapter adapter) {
        super(itemView);
        headimg = itemView.findViewById(R.id.headimg);
        publisher = itemView.findViewById(R.id.publisher);
        content = itemView.findViewById(R.id.content);
        contentimg = itemView.findViewById(R.id.contentimg);
        time = itemView.findViewById(R.id.time);
        comment = itemView.findViewById(R.id.comment);
        like = itemView.findViewById(R.id.like);
        collect = itemView.findViewById(R.id.collect);
        all = itemView.findViewById(R.id.all);
        title = itemView.findViewById(R.id.title);
        tag = itemView.findViewById(R.id.tag);
        position = itemView.findViewById(R.id.position);
    }

    public void ChangeContentImage(ArrayList<String> imagearray) {
        contentimg.removeAllViews();//清空子视图 防止原有的子视图影响
        int columnCount = 3;
        int size = imagearray.size();
        if (imagearray.size() == 1 && imagearray.get(0).endsWith("mp4")) {
            VideoView videoView = new VideoView(contentimg.getContext());
            //加载网络视频，记得适配 6.0,7.0,9.0
            String videoPath = WebRequest.baseUrl + imagearray.get(0);
            videoView.setVideoPath(videoPath);

            videoView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            //由于宽（即列）已经定义权重比例 宽设置为0 保证均分
            ViewGroup.LayoutParams layoutParams = videoView.getLayoutParams();
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.height = 600;
            videoView.setLayoutParams(layoutParams);
            contentimg.addView(videoView);
            videoView.requestFocus();
            videoView.start();
            return;
        }
        //遍历集合 动态添加
        for (int i = 0; i < size; i++) {
            GridLayout.Spec rowSpec = GridLayout.spec(i / columnCount);//行数
            GridLayout.Spec columnSpec = GridLayout.spec(i % columnCount, 1.0f);//列数 列宽的比例 weight=1
            SquareImageView imageView = new SquareImageView(contentimg.getContext());

            WebRequest.setImageByUrl(imageView, imagearray.get(i));
//            imageView.setImageURI(Uri.parse(imagearray.get(i)));
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
}

