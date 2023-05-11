package com.example.myapplication;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HuaTiAndDaoDuAndWoDeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HuaTiAndDaoDuAndWoDeFragment extends Fragment {

    private String content;
    public HuaTiAndDaoDuAndWoDeFragment(String text) {
        // Required empty public constructor
        content = String.format("抱歉，%s页面尚未完成。", text);
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HuaTiAndDaoDuAndWoDeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HuaTiAndDaoDuAndWoDeFragment newInstance(String text) {
        HuaTiAndDaoDuAndWoDeFragment fragment = new HuaTiAndDaoDuAndWoDeFragment(text);
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
        View view = inflater.inflate(R.layout.fragment_hua_ti_and_dao_du_and_wo_de, container, false);
        ((TextView)view.findViewById(R.id.sorry)).setText(content);
        return view;
    }
}