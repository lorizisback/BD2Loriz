package com.loriz.bd2loriz.utils;

import android.content.Context;
import android.util.Log;

import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.SpatialReference;
import com.wangjie.androidbucket.thread.ThreadPool;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RunnableFuture;

import bolts.Task;
import bolts.TaskCompletionSource;
import jsqlite.Constants;
import jsqlite.Database;
import jsqlite.Exception;
import jsqlite.Stmt;

/**
 * Created by Marco on 07/07/2016.
 */
public class DBHelper {

    private static DBHelper instance;
    private String DB_NAME = "test3.sqlite";
    private String DB_PATH = "/data/data/com.loriz.bd2loriz/databases";
    private String TAG = "DBHelper";
    private String TAG_SQL = "DBHelper_JSQLite";
    private Database database;

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

    public ArrayList<String> prepare(String query) {

        //SUPER MEGA TEST
        ArrayList<String> resultList = new ArrayList<String>();

        try {
            Stmt stmt = database.prepare(query);

            while (stmt.step()) {
                String result = stmt.column_string(0);
                resultList.add(result);
            }
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultList;
    }

    public ArrayList<Polygon> createPolygon(ArrayList<String> poly) throws Exception {

        Log.d("drawPoly", "Inizio disegno poligono");
        ArrayList<Polygon> polyg=new ArrayList<>();

        ExecutorService es = Executors.newCachedThreadPool();
        ArrayList<GetSinglePolygon> coll = new ArrayList<>();

        for (int i = 0; i < poly.size(); i++) {

            Log.d("lel", "Polygono " + i);
            coll.add(new GetSinglePolygon(poly.get(i), i));

        }
        List<Future<Polygon>> results = new LinkedList<>();
        try {
          results = es.invokeAll(coll);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (Future<Polygon> fut : results) {
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


    class GetSinglePolygon implements Callable<Polygon> {

        private int id;
        private Polygon polygon;
        private String passedString;

        public GetSinglePolygon(String passedString, int id) {
            this.passedString = passedString;
            this.id = id;
            polygon = new Polygon();
        }

        @Override
        public Polygon call() throws java.lang.Exception {
            Log.d("THREADS", "Thread " + id + " started.");


            String[] split_comma = passedString.substring(9, passedString.length() - 2).split("\\s*(,|\\s)\\s*");

            for (int j = 0; j < split_comma.length; j+=4) {

                if (j != 0) {
                    polygon.lineTo(new Point(Double.parseDouble(split_comma[j++]), Double.parseDouble(split_comma[j++])));
                } else {
                    Log.d("THREADS", "Thread " + id + " working.");
                    polygon.startPath(new Point(Double.parseDouble(split_comma[j++]), Double.parseDouble(split_comma[j++])));
                }
            }
            Log.d("THREADS", "Thread " + id + " returning.");
            return polygon;
        }
    }

}
