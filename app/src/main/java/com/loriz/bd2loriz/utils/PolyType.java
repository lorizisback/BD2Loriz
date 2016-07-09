package com.loriz.bd2loriz.utils;

import android.content.Context;

import java.io.IOException;

/**
 * Created by Marco on 09/07/2016.
 */
public class PolyType {
    private static PolyType instance;

    public final int POLYGON = 9;
    public final int POLYGON_END = 2;
    public final int LINESTRING = 11;
    public final int LINESTRING_END = 1;

    private PolyType() {
    }

    public static PolyType getInstance() {
        if (instance == null) {
            instance = new PolyType();
        }
        return instance;
    }
}
