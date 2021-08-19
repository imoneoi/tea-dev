package com.java.guohao;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class HttpUtils {

    static class User {
        String session;
        HashMap<String, Long> favourite; // label and timestamp
        LinkedHashMap<String, Long> history; // label and timestamp

        /* code of user-related operations */
        final static int REGISTER = 0;
        final static int LOGIN = 1;
        final static int USERDATA = 2;

        private static class SafeHandler extends Handler {
            private final WeakReference<User> mParent;
            public SafeHandler(User parent) {
                mParent = new WeakReference<User>(parent);
            }
            @Override
            public void handleMessage(@NonNull Message msg) {
                try {
                    super.handleMessage(msg);
                    User parent = (User) (mParent.get());
                    switch (msg.what) {
                        case USERDATA: {
                            JSONObject obj = new JSONObject(msg.obj.toString());
                            parent.decodeData(obj.getString("data"));
                            break;
                        }
                        default: {
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        SafeHandler mHandler = new SafeHandler(this);

        User() {
            session = "774a7579-1b28-46b5-ab41-a1f543629ce0"; // hardcode
            favourite = new HashMap<>();
            history = new LinkedHashMap<>();
        }

        public void setFavourite(String label, boolean isFavourite) {
            if (!isFavourite) this.favourite.remove(label);
            else {
                this.favourite.put(label, new Timestamp(System.currentTimeMillis()).getTime());
            }
            this.updateUserData();
        }

        public void reverseFavourite(String label) {
            setFavourite(label, !isFavourite(label));
        }

        public boolean isFavourite(String label) {
            return this.favourite.containsKey(label);
        }

        public void addHistory(String label) {
            this.history.put(label, new Timestamp(System.currentTimeMillis()).getTime());
            this.updateUserData();
        }

        public void decodeData(String data) /* "data" item in user info */ {
            try {
                Log.i("data", data);
                JSONObject obj = new JSONObject(data);
                JSONArray favourite_arr = obj.getJSONArray("favourite");
                for (int i = 0; i < favourite_arr.length(); ++i) {
                    JSONObject o = favourite_arr.getJSONObject(i);
                    favourite.put(o.getString("label"), o.getLong("time"));
                }

                JSONArray history_arr = obj.getJSONArray("history");
                for (int i = 0; i < history_arr.length(); ++i) {
                    JSONObject o = history_arr.getJSONObject(i);
                    history.put(o.getString("label"), o.getLong("time"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public String encodeData() {
            try {
                JSONObject obj = new JSONObject();
                JSONArray favourite_arr = new JSONArray();
                for (Map.Entry<String, Long> item : favourite.entrySet()) {
                    JSONObject o = new JSONObject();
                    o.put("label", item.getKey());
                    o.put("time", item.getValue());
                    favourite_arr.put(o);
                }
                obj.put("favourite", favourite_arr);

                JSONArray history_arr = new JSONArray();
                for (Map.Entry<String, Long> item : history.entrySet()) {
                    JSONObject o = new JSONObject();
                    o.put("label", item.getKey());
                    o.put("time", item.getValue());
                    history_arr.put(o);
                }
                obj.put("history", history_arr);

                return obj.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "";
        }

        public void getUserData() {
            __setUserData("");
        }

        public void updateUserData() {
            __setUserData(encodeData());
        }

        private void __setUserData(String data) {
            try {
                JSONObject params = new JSONObject();
                params.put("session", user.session);
                params.put("data", data);
                Requests.post(GlobVar.USERDATA_ADDR, params, mHandler, USERDATA);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static User user = new User();

    // http://open.edukg.cn/opedukg/api/typeOpen/open/instanceList
    public static void searchEntity(String course, String searchKey, Handler handler) {
        try {
            JSONObject data = new JSONObject();
            data.put("course", course);
            data.put("searchKey", searchKey);
            JSONObject params = new JSONObject();
            params.put("session", user.session);

            params.put("url", "http://open.edukg.cn/opedukg/api/typeOpen/open/instanceList");
            params.put("method", "GET");
            params.put("data", data.toString());
            Requests.post(GlobVar.PROC_ADDR, params, handler, GlobVar.SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
