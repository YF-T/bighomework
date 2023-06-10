package com.example.myapplication;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * create an instance of this fragment.
 */
public class FollowerAndFolloweeFragment extends Fragment {

    private String fragmentType;
    private RecyclerView recyclerView;
    private ItemForFollowFragmentAdapter adapter;
    private ArrayList<UserContent> userArrayList;
    private TextView textView;

    public FollowerAndFolloweeFragment(String fragmentType) {
        // Required empty public constructor
        this.fragmentType = fragmentType;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_follower_and_followee, container, false);
        super.onCreate(savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerview);
        textView = view.findViewById(R.id.texttitle);
        userArrayList = new ArrayList<>();

        adapter = new ItemForFollowFragmentAdapter(userArrayList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        String title;
        if (fragmentType.equals("following")) {
            title = "关注列表";
        } else {
            title = "粉丝列表";
        }
        textView.setText(title);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        searchUser();
    }

    public void searchUser() {
        Loading loading = new Loading(getContext());
        HashMap<String, String> args = new HashMap<>();
        String url;
        if (fragmentType.equals("following")) {
            url = "/user/myfollows";
        } else {
            url = "/user/myfollowers";
        }
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
                getActivity().runOnUiThread(new Runnable() {
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