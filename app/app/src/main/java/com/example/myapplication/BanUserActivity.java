package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class BanUserActivity extends AppCompatActivity {

    private String fragmentType;
    private RecyclerView recyclerView;
    private ItemForBanUserActivityAdapter adapter;
    private ArrayList<UserContent> userArrayList;
    private TextView textView;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ban_user);

        recyclerView = findViewById(R.id.recyclerview);
        textView = findViewById(R.id.texttitle);
        userArrayList = new ArrayList<>();

        adapter = new ItemForBanUserActivityAdapter(userArrayList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        searchUser();
    }

    public void searchUser() {
        Loading loading = new Loading(this);
        HashMap<String, String> args = new HashMap<>();
        String url = "/user/mybans";
        try {
            WebRequest.sendGetRequest(url, args, hashMap -> {
                userArrayList.clear();
                try {
                    ArrayList<Object> arrayList = JsonUtil.jsonArrayToArrayList((JSONArray) hashMap.get("list"));
                    for (Object o: arrayList) {
                        userArrayList.add(new UserContent((HashMap<String, Object>) o));
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