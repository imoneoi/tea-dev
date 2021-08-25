package com.java.guohao;

import android.os.Message;
import android.os.Handler;
import android.util.Log;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Requests {
    // only need to use POST here
    public static void post(String url, JSONObject params, Handler handler, int what) {
        new Thread(() -> {
            try {
                URL urlObj = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                conn.connect();
                byte[] jsonStr = params.toString().getBytes();
                Log.i("Requests", params.toString());
                conn.getOutputStream().write(jsonStr);
                conn.getOutputStream().flush();
                conn.getOutputStream().close();
                String ret = readFromStream(conn.getInputStream());
                Message msg = new Message();
                msg.what = what;
                msg.obj = ret;
                handler.sendMessage(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }


    private static String readFromStream(InputStream inStream) {
        String s = "";
        try {
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            int len;
            byte[] buffer = new byte[10240];
            while ((len = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }
            outStream.close();
            inStream.close();
            s = outStream.toString("utf8");
        } catch(Exception e) {
            e.printStackTrace();
        }
        return s;
    }
}
