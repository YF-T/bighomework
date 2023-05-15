package com.example.myapplication;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * create an instance of this fragment.
 */
public class FollowerAndFolloweeFragment extends Fragment {

    private RecyclerView recyclerView;
    private ItemForFollowFragmentAdapter adapter;
    private ArrayList<UserContent> userArrayList;

    public FollowerAndFolloweeFragment() {
        // Required empty public constructor
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
        userArrayList = new ArrayList<>();
        userArrayList.add(new UserContent("User 1", "Introduction 1"));
        userArrayList.add(new UserContent("User 2", "Introduction 2"));
        userArrayList.add(new UserContent("User 3", "Introduction 3"));

        adapter = new ItemForFollowFragmentAdapter(userArrayList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        return view;
    }
}