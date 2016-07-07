package com.loriz.bd2loriz.fragments;

/**
 * Created by Marco on 06/07/2016.
 */

import com.loriz.bd2loriz.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class MapFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.map_fragment, container, false);

        return rootView;
    }
}