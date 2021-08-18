package com.java.guohao;

import android.os.Handler;

import org.json.JSONObject;

import java.util.HashMap;

public class HttpUtils {
    private static String session = "774a7579-1b28-46b5-ab41-a1f543629ce0"; // hardcode

    // http://open.edukg.cn/opedukg/api/typeOpen/open/instanceList
    public static void searchEntity(String course, String searchKey, Handler handler) {
        try {
            JSONObject data = new JSONObject();
            data.put("course", course);
            data.put("searchKey", searchKey);
            JSONObject params = new JSONObject();
            params.put("session", session);

            params.put("url", "http://open.edukg.cn/opedukg/api/typeOpen/open/instanceList");
            params.put("method", "GET");
            params.put("data", data.toString());
            Requests.post(GlobVar.PROC_ADDR, params, handler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
