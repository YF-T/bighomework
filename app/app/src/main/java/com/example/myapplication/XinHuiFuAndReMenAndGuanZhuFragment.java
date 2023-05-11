package com.example.myapplication;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link XinHuiFuAndReMenAndGuanZhuFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class XinHuiFuAndReMenAndGuanZhuFragment extends Fragment {

    private String content;
    public XinHuiFuAndReMenAndGuanZhuFragment(String text) {
        // Required empty public constructor
        content = String.format("Sorry，%s页面尚未完成。", text);
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment XinHuiFuAndReMenAndGuanZhuFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static XinHuiFuAndReMenAndGuanZhuFragment newInstance(String text) {
        XinHuiFuAndReMenAndGuanZhuFragment fragment = new XinHuiFuAndReMenAndGuanZhuFragment(text);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_xin_hui_fu_and_re_men_and_guan_zhu, container, false);
        ((TextView)view.findViewById(R.id.sorry)).setText(content);
        return view;
    }
}