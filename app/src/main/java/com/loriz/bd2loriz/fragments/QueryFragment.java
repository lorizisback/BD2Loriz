package com.loriz.bd2loriz.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.android.map.GraphicsLayer;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.MultiPath;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.map.Graphic;
import com.loriz.bd2loriz.MainActivity;
import com.loriz.bd2loriz.R;
import com.loriz.bd2loriz.utils.DBHelper;
import com.loriz.bd2loriz.utils.PolyType;

import java.io.IOException;
import java.util.ArrayList;

import jsqlite.Exception;

/**
 * Created by Marco on 06/07/2016.
 */
public class QueryFragment extends Fragment {

    private Button button;
    private DBHelper dbHelper;
    private PolyType polyType;
    private MainActivity mMainActivity;
    private RadioGroup radioGroup;
    private RadioButton radio1;
    private RadioButton radio2;
    private RadioButton radio3;
    private RadioButton radio4;
    private RadioButton radio5;
    private EditText editText;
    private RadioButton radio6;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.query_fragment, container, false);
        radioGroup = (RadioGroup) rootView.findViewById(R.id.radio_group);
        button = (Button) rootView.findViewById(R.id.button);
        editText = (EditText) rootView.findViewById(R.id.edittext);

        try {
            dbHelper = DBHelper.getInstance(getContext());
        } catch (IOException e) {
            e.printStackTrace();
        }

        polyType = PolyType.getInstance();

        radio1 = (RadioButton) radioGroup.findViewById(R.id.radio_1);
        radio2 = (RadioButton) radioGroup.findViewById(R.id.radio_2);
        radio3 = (RadioButton) radioGroup.findViewById(R.id.radio_3);
        radio4 = (RadioButton) radioGroup.findViewById(R.id.radio_4);
        radio5 = (RadioButton) radioGroup.findViewById(R.id.radio_5);
        radio6 = (RadioButton) radioGroup.findViewById(R.id.radio_6);

        button.setOnClickListener(new View.OnClickListener() {
            public String prov;

            @Override
            public void onClick(View view) {
                if ((editText.getText().toString() != null && !(editText.getText().toString().equals("")))) {

                    if (radio1.isChecked()) {
                        // PRIMA QUERY
                        if (editText.getText().toString().equalsIgnoreCase("Medio-Campidano")) {
                            prov = "Madio-Campidano";
                        } else {
                            prov = editText.getText().toString();
                        }
                        new QueryAcquedottiInProvincia().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, prov);
                        new QueryProvincia().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, prov);

                    }

                    if (radio2.isChecked()) {
                        // SECONDA QUERY

                            if (editText.getText().toString().equalsIgnoreCase("Medio-Campidano")) {
                                prov = "Madio-Campidano";
                            } else {
                                prov = editText.getText().toString();
                            }

                        new QueryAcquElettrIntersProv().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, prov);
                        new QueryProvincia().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, prov);

                    }

                    if (radio3.isChecked()) {
                        // terza QUERY

                        new QueryProvicePerAcquedotto().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, editText.getText().toString());
                        new QueryAcquedotto().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, editText.getText().toString());

                    }
                    if (radio4.isChecked()) {
                        // quarta QUERY

                        new QueryProvicePerElettrodotto().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, editText.getText().toString());
                        new QueryElettrodotto().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, editText.getText().toString());

                    }

                    if (radio5.isChecked()) {
                        // quint QUERY
                        if (editText.getText().toString().equalsIgnoreCase("Medio-Campidano")) {
                            prov = "Madio-Campidano";
                        } else {
                            prov = editText.getText().toString();
                        }
                        new QueryElettrodottiInProvincia().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, prov);
                        new QueryProvincia().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, prov);

                    }

                    if (radio6.isChecked()) {
                        // sesta QUERY

                        if (editText.getText().toString().equalsIgnoreCase("Medio-Campidano")) {
                            prov = "Madio-Campidano";
                        } else {
                            prov = editText.getText().toString();
                        }

                        new QueryAcquElettrCrossBordProv().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, prov);
                        new QueryProvincia().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, prov);

                    }

                } else {
                    Toast.makeText(getContext(), "Inserire l'input!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mMainActivity = (MainActivity) getActivity();

    }


    private class QueryAcquedottiInProvincia extends AsyncTask<String, Void, GraphicsLayer> {
        private GraphicsLayer query1_layer;
        private Graphic[] prov_graphics;

        @Override
        protected GraphicsLayer doInBackground(String... nome) {
            ArrayList<ArrayList<String>> res = new ArrayList<>();

            res = dbHelper.prepare("SELECT ASText(acquedotto.Geometry) FROM DBTProvincia provincia, DBTAcquedotto acquedotto " +
                    "WHERE provincia.NOME='" + nome[0] + "' AND ST_Contains(provincia.Geometry, acquedotto.Geometry) " +
                    " AND acquedotto.ROWID IN " +
                    "(SELECT pkid " +
                    "FROM idx_DBTAcquedotto_geometry WHERE xmin <= MbrMaxX(provincia.Geometry) AND " +
                    "ymin <= MbrMaxY(provincia.Geometry) AND " +
                    "xmax >= MbrMinX(provincia.Geometry) AND " +
                    "ymax >= MbrMinY(provincia.Geometry)) " +
                    "GROUP BY acquedotto.PK_UID;");



            ArrayList<MultiPath> prov_res = new ArrayList<>();

            if (res.size() != 0) {

                try {
                    prov_res = dbHelper.createGeometry(res.get(0), polyType.LINESTRING, polyType.LINESTRING_END);
                } catch (Exception e) {
                    e.printStackTrace();
                }


                prov_graphics = new Graphic[prov_res.size()];

                for (int i = 0; i < prov_res.size(); i++) {
                    Polyline temp = (Polyline) prov_res.get(i);
                    prov_graphics[i] = new Graphic(GeometryEngine.project(temp, mMainActivity.input, mMainActivity.output), mMainActivity.slsAcqua);
                }

                query1_layer = new GraphicsLayer();

                query1_layer.addGraphics(prov_graphics);

                return query1_layer;
            }   else return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!mMainActivity.pDialog.isShowing()) {
                mMainActivity.pDialog.show();
            }
        }

        @Override
        protected void onPostExecute(GraphicsLayer aVoid) {
            super.onPostExecute(aVoid);
            if (mMainActivity.pDialog.isShowing()) {
                mMainActivity.pDialog.dismiss();
            }
            if (aVoid != null) {
                mMainActivity.mMapView.addLayer(aVoid);
            } else {
                Toast.makeText(getContext(), "Nessun Risultato!", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private class QueryElettrodottiInProvincia extends AsyncTask<String, Void, GraphicsLayer> {
        private GraphicsLayer query1_layer;
        private Graphic[] prov_graphics;

        @Override
        protected GraphicsLayer doInBackground(String... nome) {
            ArrayList<ArrayList<String>> res = new ArrayList<>();

            res = dbHelper.prepare("SELECT ASText(elettrodotto.Geometry) FROM DBTProvincia provincia, DBTElettrodotto elettrodotto " +
                    "WHERE provincia.NOME='" + nome[0] + "' AND ST_Contains(provincia.Geometry, elettrodotto.Geometry) " +
                    " AND elettrodotto.ROWID IN " +
                    "(SELECT pkid " +
                    "FROM idx_DBTElettrodotto_geometry WHERE xmin <= MbrMaxX(provincia.Geometry) AND " +
                    "ymin <= MbrMaxY(provincia.Geometry) AND " +
                    "xmax >= MbrMinX(provincia.Geometry) AND " +
                    "ymax >= MbrMinY(provincia.Geometry)) " +
                    "GROUP BY elettrodotto.PK_UID;");


            ArrayList<MultiPath> prov_res = new ArrayList<>();

            if (res.size() != 0) {

                try {
                    prov_res = dbHelper.createGeometry(res.get(0), polyType.LINESTRING, polyType.LINESTRING_END);
                } catch (Exception e) {
                    e.printStackTrace();
                }


                prov_graphics = new Graphic[prov_res.size()];

                for (int i = 0; i < prov_res.size(); i++) {
                    Polyline temp = (Polyline) prov_res.get(i);
                    prov_graphics[i] = new Graphic(GeometryEngine.project(temp, mMainActivity.input, mMainActivity.output), mMainActivity.slsElettro);
                }

                query1_layer = new GraphicsLayer();

                query1_layer.addGraphics(prov_graphics);

                return query1_layer;
            } else return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!mMainActivity.pDialog.isShowing()) {
                mMainActivity.pDialog.show();
            }
        }

        @Override
        protected void onPostExecute(GraphicsLayer aVoid) {
            super.onPostExecute(aVoid);
            if (mMainActivity.pDialog.isShowing()) {
                mMainActivity.pDialog.dismiss();
            }
            if (aVoid != null) {
                mMainActivity.mMapView.addLayer(aVoid);
            } else {
                Toast.makeText(getContext(), "Nessun Risultato!", Toast.LENGTH_SHORT).show();
            }

        }
    }


    private class QueryProvincia extends AsyncTask<String, Void, GraphicsLayer> {
        private GraphicsLayer query1_layer;
        private Graphic[] prov_graphics;

        @Override
        protected GraphicsLayer doInBackground(String... nome) {
            ArrayList<ArrayList<String>> res = new ArrayList<>();
            res = dbHelper.prepare("SELECT ASText(provincia.Geometry) FROM DBTProvincia provincia " +
                    "WHERE provincia.NOME='" + nome[0] + "';");

            ArrayList<MultiPath> prov_res = new ArrayList<>();

            if (res.size() != 0) {


                try {
                    prov_res = dbHelper.createGeometry(res.get(0), polyType.POLYGON, polyType.POLYGON_END);
                } catch (Exception e) {
                    e.printStackTrace();
                }


                prov_graphics = new Graphic[prov_res.size()];

                for (int i = 0; i < prov_res.size(); i++) {
                    Polygon temp = (Polygon) prov_res.get(i);
                    prov_graphics[i] = new Graphic(GeometryEngine.project(temp, mMainActivity.input, mMainActivity.output), mMainActivity.slsProv);
                }

                query1_layer = new GraphicsLayer();

                query1_layer.addGraphics(prov_graphics);

                return query1_layer;
            } else return null;
        }


        @Override
        protected void onPostExecute(GraphicsLayer aVoid) {
            super.onPostExecute(aVoid);
            /*if (mMainActivity.pDialog.isShowing()) {
                mMainActivity.pDialog.dismiss();
            }*/
            if (aVoid != null) {
                mMainActivity.mMapView.addLayer(aVoid);
            }

        }
    }

    private class QueryAcquedotto extends AsyncTask<String, Void, GraphicsLayer> {
        private GraphicsLayer query1_layer;
        private Graphic[] prov_graphics;

        @Override
        protected GraphicsLayer doInBackground(String... nome) {
            ArrayList<ArrayList<String>> res = new ArrayList<>();
            res = dbHelper.prepare("SELECT ASText(acquedotto.Geometry) FROM DBTAcquedotto acquedotto " +
                    "WHERE acquedotto.PK_UID=" + nome[0] + ";");

            ArrayList<MultiPath> prov_res = new ArrayList<>();

            if (res.size() != 0) {


                try {
                    prov_res = dbHelper.createGeometry(res.get(0), polyType.LINESTRING, polyType.LINESTRING_END);
                } catch (Exception e) {
                    e.printStackTrace();
                }


                prov_graphics = new Graphic[prov_res.size()];

                for (int i = 0; i < prov_res.size(); i++) {
                    Polyline temp = (Polyline) prov_res.get(i);
                    prov_graphics[i] = new Graphic(GeometryEngine.project(temp, mMainActivity.input, mMainActivity.output), mMainActivity.slsAcqua);
                }

                query1_layer = new GraphicsLayer();

                query1_layer.addGraphics(prov_graphics);

                return query1_layer;
            } else return null;
        }


        @Override
        protected void onPostExecute(GraphicsLayer aVoid) {
            super.onPostExecute(aVoid);
            /*if (mMainActivity.pDialog.isShowing()) {
                mMainActivity.pDialog.dismiss();
            }*/
            if (aVoid != null) {
                mMainActivity.mMapView.addLayer(aVoid);
            }

        }
    }

    private class QueryElettrodotto extends AsyncTask<String, Void, GraphicsLayer> {
        private GraphicsLayer query1_layer;
        private Graphic[] prov_graphics;

        @Override
        protected GraphicsLayer doInBackground(String... nome) {
            ArrayList<ArrayList<String>> res = new ArrayList<>();
            res = dbHelper.prepare("SELECT ASText(elettrodotto.Geometry) FROM DBTElettrodotto elettrodotto " +
                    "WHERE elettrodotto.PK_UID=" + nome[0] + ";");

            ArrayList<MultiPath> prov_res = new ArrayList<>();

            if (res.size() != 0) {


                try {
                    prov_res = dbHelper.createGeometry(res.get(0), polyType.LINESTRING, polyType.LINESTRING_END);
                } catch (Exception e) {
                    e.printStackTrace();
                }


                prov_graphics = new Graphic[prov_res.size()];

                for (int i = 0; i < prov_res.size(); i++) {
                    Polyline temp = (Polyline) prov_res.get(i);
                    prov_graphics[i] = new Graphic(GeometryEngine.project(temp, mMainActivity.input, mMainActivity.output), mMainActivity.slsElettro);
                }

                query1_layer = new GraphicsLayer();

                query1_layer.addGraphics(prov_graphics);

                return query1_layer;
            } else return null;
        }


        @Override
        protected void onPostExecute(GraphicsLayer aVoid) {
            super.onPostExecute(aVoid);
            /*if (mMainActivity.pDialog.isShowing()) {
                mMainActivity.pDialog.dismiss();
            }*/
            if (aVoid != null) {
                mMainActivity.mMapView.addLayer(aVoid);
            }

        }
    }

    private class QueryAcquElettrIntersProv extends AsyncTask<String, Void, GraphicsLayer> {

        private ArrayList<MultiPath> acqua_res;
        private ArrayList<MultiPath> elettro_res;
        private Graphic[] acqua_graphics;
        private Graphic[] elettro_graphics;
        private GraphicsLayer query2_layer;

        @Override
        protected GraphicsLayer doInBackground(String... nome) {
            ArrayList<ArrayList<String>> res = new ArrayList<>();

            res = dbHelper.prepare("SELECT ASText(acquedotti.Geometry), ASText(elettrodotti.Geometry)" +
                    " FROM DBTAcquedotto acquedotti, DBTElettrodotto elettrodotti, DBTProvincia province " +
                    "where ST_Contains(province.Geometry, acquedotti.Geometry) and ST_Contains(province.Geometry, elettrodotti.Geometry) " +
                    "AND ST_Crosses(acquedotti.Geometry, elettrodotti.Geometry) " +
                    "AND province.nome='" + nome[0] + "' " +
                    "AND acquedotti.ROWID IN " +
                    "(SELECT pkid " +
                    "FROM idx_DBTAcquedotto_geometry WHERE xmin <= MbrMaxX(province.Geometry) AND " +
                    "ymin <= MbrMaxY(province.Geometry) AND " +
                    "xmax >= MbrMinX(province.Geometry) AND " +
                    "ymax >= MbrMinY(province.Geometry)) " +
                    "AND elettrodotti.ROWID IN " +
                    "(SELECT pkid " +
                    "FROM idx_DBTElettrodotto_geometry WHERE xmin <= MbrMaxX(province.Geometry) AND " +
                    "ymin <= MbrMaxY(province.Geometry) AND " +
                    "xmax >= MbrMinX(province.Geometry) AND " +
                    "ymax >= MbrMinY(province.Geometry)) " +
                    "GROUP BY elettrodotti.PK_UID, acquedotti.PK_UID;");

            if (res.size() != 0) {


                try {
                    acqua_res = dbHelper.createGeometry(res.get(0), polyType.LINESTRING, polyType.LINESTRING_END);
                    elettro_res = dbHelper.createGeometry(res.get(1), polyType.LINESTRING, polyType.LINESTRING_END);
                } catch (Exception e) {
                    e.printStackTrace();
                }


                acqua_graphics = new Graphic[acqua_res.size()];
                elettro_graphics = new Graphic[elettro_res.size()];

                for (int i = 0; i < acqua_res.size(); i++) {
                    Polyline acqua_temp = (Polyline) acqua_res.get(i);
                    acqua_graphics[i] = new Graphic(GeometryEngine.project(acqua_temp, mMainActivity.input, mMainActivity.output), mMainActivity.slsAcqua);
                }

                for (int i = 0; i < elettro_res.size(); i++) {
                    Polyline elettro_temp = (Polyline) elettro_res.get(i);
                    elettro_graphics[i] = new Graphic(GeometryEngine.project(elettro_temp, mMainActivity.input, mMainActivity.output), mMainActivity.slsElettro);
                }

                query2_layer = new GraphicsLayer();

                query2_layer.addGraphics(acqua_graphics);
                query2_layer.addGraphics(elettro_graphics);

                return query2_layer;
            } else return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!mMainActivity.pDialog.isShowing()) {
                mMainActivity.pDialog.show();
            }
        }

        @Override
        protected void onPostExecute(GraphicsLayer aVoid) {
            super.onPostExecute(aVoid);
            if (mMainActivity.pDialog.isShowing()) {
                mMainActivity.pDialog.dismiss();
            }
            if (aVoid != null) {
                mMainActivity.mMapView.addLayer(aVoid);
            } else {
                Toast.makeText(getContext(), "Nessun Risultato!", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private class QueryAcquElettrCrossBordProv extends AsyncTask<String, Void, GraphicsLayer> {

        private ArrayList<MultiPath> acqua_res;
        private ArrayList<MultiPath> elettro_res;
        private Graphic[] acqua_graphics;
        private Graphic[] elettro_graphics;
        private GraphicsLayer query2_layer;

        @Override
        protected GraphicsLayer doInBackground(String... nome) {
            ArrayList<ArrayList<String>> res = new ArrayList<>();

            res = dbHelper.prepare("SELECT ASText(acquedotti.Geometry), ASText(elettrodotti.Geometry) " +
                    "FROM DBTAcquedotto acquedotti, DBTElettrodotto elettrodotti, DBTProvincia province " +
                            "where ST_Crosses(province.Geometry, acquedotti.Geometry) and ST_Crosses(province.Geometry, elettrodotti.Geometry) " +
                            "AND province.nome='" + nome[0] + "' " +
                            "AND acquedotti.ROWID IN " +
                            "(SELECT pkid " +
                            "FROM idx_DBTAcquedotto_geometry WHERE xmin <= MbrMaxX(province.Geometry) AND " +
                            "ymin <= MbrMaxY(province.Geometry) AND " +
                            "xmax >= MbrMinX(province.Geometry) AND " +
                            "ymax >= MbrMinY(province.Geometry)) " +
                            "AND elettrodotti.ROWID IN "  +
                            "(SELECT pkid " +
                            "FROM idx_DBTElettrodotto_geometry WHERE xmin <= MbrMaxX(province.Geometry) AND " +
                            "ymin <= MbrMaxY(province.Geometry) AND " +
                            "xmax >= MbrMinX(province.Geometry) AND " +
                            "ymax >= MbrMinY(province.Geometry)) " +
                            "GROUP BY elettrodotti.PK_UID, acquedotti.PK_UID;");

            if (res.size() != 0) {

                try {
                    acqua_res = dbHelper.createGeometry(res.get(0), polyType.LINESTRING, polyType.LINESTRING_END);
                    elettro_res = dbHelper.createGeometry(res.get(1), polyType.LINESTRING, polyType.LINESTRING_END);
                } catch (Exception e) {
                    e.printStackTrace();
                }


                acqua_graphics = new Graphic[acqua_res.size()];
                elettro_graphics = new Graphic[elettro_res.size()];

                for (int i = 0; i < acqua_res.size(); i++) {
                    Polyline acqua_temp = (Polyline) acqua_res.get(i);
                    acqua_graphics[i] = new Graphic(GeometryEngine.project(acqua_temp, mMainActivity.input, mMainActivity.output), mMainActivity.slsAcqua);
                }

                for (int i = 0; i < elettro_res.size(); i++) {
                    Polyline elettro_temp = (Polyline) elettro_res.get(i);
                    elettro_graphics[i] = new Graphic(GeometryEngine.project(elettro_temp, mMainActivity.input, mMainActivity.output), mMainActivity.slsElettro);
                }

                query2_layer = new GraphicsLayer();

                query2_layer.addGraphics(acqua_graphics);
                query2_layer.addGraphics(elettro_graphics);

                return query2_layer;
            } else return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!mMainActivity.pDialog.isShowing()) {
                mMainActivity.pDialog.show();
            }
        }

        @Override
        protected void onPostExecute(GraphicsLayer aVoid) {
            super.onPostExecute(aVoid);
            if (mMainActivity.pDialog.isShowing()) {
                mMainActivity.pDialog.dismiss();
            }
            if (aVoid != null) {
                mMainActivity.mMapView.addLayer(aVoid);
            } else {
                Toast.makeText(getContext(), "Nessun Risultato!", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private class QueryProvicePerAcquedotto extends AsyncTask<String, Void, GraphicsLayer> {

        private ArrayList<MultiPath> acqua_res;
        private ArrayList<MultiPath> elettro_res;
        private Graphic[] acqua_graphics;
        private Graphic[] elettro_graphics;
        private GraphicsLayer query2_layer;
        private ArrayList<MultiPath> prov_res;
        private Graphic[] prov_graphics;

        @Override
        protected GraphicsLayer doInBackground(String... nome) {
            ArrayList<ArrayList<String>> res = new ArrayList<>();

            res = dbHelper.prepare("SELECT ASText(province.Geometry) from " +
                    "DBTProvincia province, DBTAcquedotto acquedotti " +
                    "WHERE acquedotti.PK_UID='" + nome[0] + "' AND " +
                    "ST_Intersects(acquedotti.Geometry, province.Geometry) " +
                    "AND province.ROWID IN " +
                    "(SELECT pkid " +
                    "FROM idx_DBTProvincia_geometry WHERE xmin <= MbrMaxX(acquedotti.Geometry) AND " +
                    "ymin <= MbrMaxY(acquedotti.Geometry) AND " +
                    "xmax >= MbrMinX(acquedotti.Geometry) AND " +
                    "ymax >= MbrMinY(acquedotti.Geometry)) " +
                    "GROUP BY province.PK_UID;");

            if (res.size() != 0) {

                try {
                    prov_res = dbHelper.createGeometry(res.get(0), polyType.POLYGON, polyType.POLYGON_END);
                } catch (Exception e) {
                    e.printStackTrace();
                }


            prov_graphics = new Graphic[prov_res.size()];

            for (int i = 0; i < prov_res.size(); i++) {
                Polygon acqua_temp = (Polygon) prov_res.get(i);
                prov_graphics[i] = new Graphic(GeometryEngine.project(acqua_temp, mMainActivity.input, mMainActivity.output), mMainActivity.slsProv);
            }

            query2_layer = new GraphicsLayer();

            query2_layer.addGraphics(prov_graphics);

            return query2_layer;
        } else return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!mMainActivity.pDialog.isShowing()) {
                mMainActivity.pDialog.show();
            }
        }

        @Override
        protected void onPostExecute(GraphicsLayer aVoid) {
            super.onPostExecute(aVoid);
            if (mMainActivity.pDialog.isShowing()) {
                mMainActivity.pDialog.dismiss();
            }
            if (aVoid != null) {
                mMainActivity.mMapView.addLayer(aVoid);
            } else {
                Toast.makeText(getContext(), "Nessun Risultato!", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private class QueryProvicePerElettrodotto extends AsyncTask<String, Void, GraphicsLayer> {

        private ArrayList<MultiPath> acqua_res;
        private ArrayList<MultiPath> elettro_res;
        private Graphic[] acqua_graphics;
        private Graphic[] elettro_graphics;
        private GraphicsLayer query2_layer;
        private ArrayList<MultiPath> prov_res;
        private Graphic[] prov_graphics;

        @Override
        protected GraphicsLayer doInBackground(String... nome) {
            ArrayList<ArrayList<String>> res = new ArrayList<>();

            res = dbHelper.prepare("SELECT ASText(province.Geometry) from " +
                    "DBTProvincia province, DBTElettrodotto elettrodotto " +
                    "WHERE elettrodotto.PK_UID='" + nome[0] + "' AND " +
                    "ST_Intersects(elettrodotto.Geometry, province.Geometry) " +
                    "AND province.ROWID IN " +
                    "(SELECT pkid " +
                    "FROM idx_DBTProvincia_geometry WHERE xmin <= MbrMaxX(elettrodotto.Geometry) AND " +
                    "ymin <= MbrMaxY(elettrodotto.Geometry) AND " +
                    "xmax >= MbrMinX(elettrodotto.Geometry) AND " +
                    "ymax >= MbrMinY(elettrodotto.Geometry)) " +
                    "GROUP BY province.PK_UID;");

            if (res.size() != 0) {

                try {
                    prov_res = dbHelper.createGeometry(res.get(0), polyType.POLYGON, polyType.POLYGON_END);
                } catch (Exception e) {
                    e.printStackTrace();
                }


                prov_graphics = new Graphic[prov_res.size()];

                for (int i = 0; i < prov_res.size(); i++) {
                    Polygon acqua_temp = (Polygon) prov_res.get(i);
                    prov_graphics[i] = new Graphic(GeometryEngine.project(acqua_temp, mMainActivity.input, mMainActivity.output), mMainActivity.slsProv);
                }

                query2_layer = new GraphicsLayer();

                query2_layer.addGraphics(prov_graphics);

                return query2_layer;
            } else return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!mMainActivity.pDialog.isShowing()) {
                mMainActivity.pDialog.show();
            }
        }

        @Override
        protected void onPostExecute(GraphicsLayer aVoid) {
            super.onPostExecute(aVoid);
            if (mMainActivity.pDialog.isShowing()) {
                mMainActivity.pDialog.dismiss();
            }
            if (aVoid != null) {
                mMainActivity.mMapView.addLayer(aVoid);
            } else {
                Toast.makeText(getContext(), "Nessun Risultato!", Toast.LENGTH_SHORT).show();
            }

        }
    }

}