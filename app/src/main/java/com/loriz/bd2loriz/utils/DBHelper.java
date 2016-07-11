package com.loriz.bd2loriz.utils;

import android.content.Context;
import android.util.Log;

import com.esri.core.geometry.MultiPath;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import jsqlite.Constants;
import jsqlite.Database;
import jsqlite.Exception;
import jsqlite.Stmt;

/**
 * Created by Marco on 07/07/2016.
 */
public class DBHelper {

    private static DBHelper instance;
    private String DB_NAME = "bd2final.sqlite";
    private String DB_PATH = "/data/data/com.loriz.bd2loriz/databases";
    private String TAG = "DBHelper";
    private String TAG_SQL = "DBHelper_JSQLite";
    private Database database;
    private boolean isInitialized = false;

    private DBHelper(Context context) throws IOException {

        File cacheDatabase = new File(DB_PATH, DB_NAME);
        if (!cacheDatabase.getParentFile().exists()) {
            File dirDb = cacheDatabase.getParentFile();
            Log.i(TAG, "making directory: " + cacheDatabase.getParentFile());
            if (!dirDb.mkdir()) {
                throw new IOException(TAG_SQL + "Could not create dirDb: " + dirDb.getAbsolutePath());
            }
        }

        InputStream inputStream = context.getAssets().open("databases/" + DB_NAME);
        copyDatabase(inputStream, DB_PATH + File.separator + DB_NAME);
        database = new Database();

        try {
            database.open(cacheDatabase.getAbsolutePath(),
                    Constants.SQLITE_OPEN_READWRITE | Constants.SQLITE_OPEN_CREATE);
        } catch (Exception e) {
            Log.e(TAG_SQL, e.getMessage());
        }
    }

    private void copyDatabase(InputStream inputStream, String dbFilename) throws IOException {
        OutputStream outputStream = new FileOutputStream(dbFilename);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }

        outputStream.flush();
        outputStream.close();
        inputStream.close();
        Log.i(TAG, "Copied database to " + dbFilename);
    }

    /**
     * Return a singleton instance of DatabaseAccess.
     *
     * @param context the Context
     * @return the instance of DabaseAccess
     */
    public static DBHelper getInstance(Context context) throws IOException {
        if (instance == null) {
            instance = new DBHelper(context);
        }
        return instance;
    }


    /**
     * Close the database connection.
     */
    public void close() throws Exception {
        if (database != null) {
            this.database.close();
        }
    }

    public ArrayList<ArrayList<String>> prepare(String query) {

        //SUPER MEGA TEST
        ArrayList<ArrayList<String>> resultList = new ArrayList<ArrayList<String>>();


        try {
            Stmt stmt = database.prepare(query);

            int log = 0;
            while (stmt.step()) {
                Log.d("STMT", "Statement iterazione n. " + log++);

                for (int i = 0; i < stmt.column_count(); i++) {

                    if (resultList.size() != stmt.column_count()) {
                        resultList.add(new ArrayList<String>());
                    }

                    resultList.get(i).add(stmt.column_string(i));


                }
            }
            stmt.close();
        } catch (Exception e) {
            Log.e("AIUTO", "Query rotta! " + e.toString());
            e.printStackTrace();
        }
        return resultList;
    }

    public ArrayList<MultiPath> createGeometry(ArrayList<String> poly, int polyTypeStart, int polyTypeEnd) throws Exception {

        ArrayList<MultiPath> polyg = new ArrayList<>();

        ExecutorService es = Executors.newCachedThreadPool();
        ArrayList<GetSingleMultiPath> coll = new ArrayList<>();

        for (int i = 0; i < poly.size(); i++) {

            coll.add(new GetSingleMultiPath(poly.get(i), polyTypeStart, polyTypeEnd));

        }
        List<Future<MultiPath>> results = new LinkedList<>();
        try {
            results = es.invokeAll(coll);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (Future<MultiPath> fut : results) {
            try {
                polyg.add(fut.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

        }

        return polyg;
    }

    public ArrayList<Point> createPointSet(ArrayList<String> pointList) throws Exception {

        ArrayList<Point> points = new ArrayList<>();

        ExecutorService es = Executors.newCachedThreadPool();
        ArrayList<GetPoint> coll = new ArrayList<>();

        for (int i = 0; i < pointList.size(); i++) {

            coll.add(new GetPoint(pointList.get(i)));

        }
        List<Future<Point>> results = new LinkedList<>();
        try {
            results = es.invokeAll(coll);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (Future<Point> fut : results) {
            try {
                points.add(fut.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

        }

        return points;
    }


    class GetSingleMultiPath implements Callable<MultiPath> {

        private int trick;
        private int polyTypeStart;
        private int polyTypeEnd;
        private MultiPath geometry;
        private String passedString;

        public GetSingleMultiPath(String passedString, int polyTypeStart, int polyTypeEnd) {
            this.passedString = passedString;
            this.polyTypeStart = polyTypeStart;
            this.polyTypeEnd = polyTypeEnd;

            switch (polyTypeStart) {
                case 9: {
                    //case POLYGON
                    geometry = new Polygon();
                    trick = 4;
                    break;
                }

                case 11: {
                    //case POLYLINE
                    geometry = new Polyline();
                    trick = 0;
                    break;
                }

                default: {

                    //dummy
                    geometry = new Polygon();
                    trick = 4;

                    break;
                }
            }

        }

        @Override
        public MultiPath call() throws java.lang.Exception {

            String[] split_comma = passedString.substring(polyTypeStart, passedString.length() - polyTypeEnd).split("\\s*(,|\\s)\\s*");

            for (int j = 0; j < split_comma.length; j += trick) {

                if (j != 0) {
                    geometry.lineTo(new Point(Double.parseDouble(split_comma[j++]), Double.parseDouble(split_comma[j++])));
                } else {
                    geometry.startPath(new Point(Double.parseDouble(split_comma[j++]), Double.parseDouble(split_comma[j++])));
                }
            }
            return geometry;
        }
    }

    class GetPoint implements Callable<Point> {

        private Point point;
        private String passedString;

        public GetPoint(String passedString) {
            this.passedString = passedString;
        }

        @Override
        public Point call() throws java.lang.Exception {

            String[] split_comma = passedString.substring(6, passedString.length() - 1).split("\\s*(,|\\s)\\s*");

            for (int j = 0; j < split_comma.length; j++) {
                point = new Point(Double.parseDouble(split_comma[j++]), Double.parseDouble(split_comma[j++]));
            }
            return point;
        }
    }

}
