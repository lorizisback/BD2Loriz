package com.loriz.bd2loriz.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.loriz.bd2loriz.R;

/**
 * Created by Marco on 06/07/2016.
 */
public class QueryFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.query_fragment, container, false);

        return rootView;
    }
}