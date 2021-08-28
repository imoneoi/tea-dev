package com.java.guohao;

import android.os.Handler;
import android.os.Message;
import android.util.ArrayMap;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.sql.Timestamp;
import java.util.Map;
import java.util.Objects;

public class HttpUtils {

    static class CourseLabel {
        String course, label;

        CourseLabel(String course, String label) {
            this.course = course;
            this.label = label;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CourseLabel that = (CourseLabel) o;
            return course.equals(that.course) && label.equals(that.label);
        }

        @Override
        public int hashCode() {
            return Objects.hash(course, label);
        }
    }

    static class User {
        String session, username;
        ArrayMap<CourseLabel, Long> favorite; // label and timestamp
        ArrayMap<CourseLabel, Long> history; // label and timestamp

        /* code of user-related operations */
        final static int REGISTER = 0;
        final static int LOGIN = 1;
        final static int USERDATA = 2;

        private static class SafeHandler extends Handler {
            private final WeakReference<User> mParent;
            public SafeHandler(User parent) {
                mParent = new WeakReference<>(parent);
            }
            @Override
            public void handleMessage(@NonNull Message msg) {
                try {
                    super.handleMessage(msg);
                    User parent = mParent.get();
                    JSONObject obj = new JSONObject(msg.obj.toString());
                    parent.decodeData(obj.getString("data"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        SafeHandler mHandler = new SafeHandler(this);
        String mSearchKey;

        User() {
            favorite = new ArrayMap<>();
            history = new ArrayMap<>();
            mSearchKey = Storage.getKey(this.getClass().getSimpleName(), session);
        }

        public void setFavourite(CourseLabel courseLabel, boolean isFavourite) {
            if (!isFavourite) {
                this.favorite.remove(courseLabel);
            } else {
                this.favorite.put(courseLabel, new Timestamp(System.currentTimeMillis()).getTime());
            }
            this.updateUserData();
        }

        public void reverseFavourite(CourseLabel courseLabel) {
            setFavourite(courseLabel, !isFavourite(courseLabel));
        }

        public boolean isFavourite(CourseLabel courseLabel) {
            return this.favorite.containsKey(courseLabel);
        }

        public void addHistory(CourseLabel courseLabel) {
            if (isHistory(courseLabel)) {
                history.remove(courseLabel);
            }
            this.history.put(courseLabel, new Timestamp(System.currentTimeMillis()).getTime());
            this.updateUserData();
        }

        public boolean isHistory(CourseLabel courseLabel) {
            return this.history.containsKey(courseLabel);
        }

        public void decodeData(String data) /* "data" item in user info */ {
            try {
                JSONObject obj = new JSONObject(data);
                JSONArray favourite_arr = obj.getJSONArray("favourite");
                for (int i = 0; i < favourite_arr.length(); ++i) {
                    JSONObject o = favourite_arr.getJSONObject(i);
                    favorite.put(new CourseLabel(o.getString("course"), o.getString("label")), o.getLong("time"));
                }

                JSONArray history_arr = obj.getJSONArray("history");
                for (int i = 0; i < history_arr.length(); ++i) {
                    JSONObject o = history_arr.getJSONObject(i);
                    history.put(new CourseLabel(o.getString("course"), o.getString("label")), o.getLong("time"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public String encodeData() {
            try {
                JSONObject obj = new JSONObject();
                JSONArray favourite_arr = new JSONArray();
                for (Map.Entry<CourseLabel, Long> item : favorite.entrySet()) {
                    JSONObject o = new JSONObject();
                    o.put("label", item.getKey().label);
                    o.put("course", item.getKey().course);
                    o.put("time", item.getValue());
                    favourite_arr.put(o);
                }
                obj.put("favourite", favourite_arr);

                JSONArray history_arr = new JSONArray();
                for (Map.Entry<CourseLabel, Long> item : history.entrySet()) {
                    JSONObject o = new JSONObject();
                    o.put("label", item.getKey().label);
                    o.put("course", item.getKey().course);
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

        public static void register(String username, String passwd, String data, Handler handler) {
            try {
                JSONObject params = new JSONObject();
                params.put("user", username);
                params.put("passwd", passwd);
                params.put("data", data);
                Requests.post(GlobVar.REGISTER_ADDR, params, handler, REGISTER);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public static void login(String username, String passwd, Handler handler) {
            try {
                user.username = username;
                JSONObject params = new JSONObject();
                params.put("user", username);
                params.put("passwd", passwd);
                Requests.post(GlobVar.LOGIN_ADDR, params, handler, LOGIN);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public static void checkSession(String session, Handler handler) {
            try {
                JSONObject params = new JSONObject();
                params.put("session", session);
                Requests.post(GlobVar.USERDATA_ADDR, params, handler, USERDATA);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public static User user = new User();

    public static void sendData(JSONObject data, String url, String method, Handler handler) {
        try {
            JSONObject params = new JSONObject();
            params.put("session", user.session);
            params.put("url", url);
            params.put("method", method);
            params.put("data", data.toString());
            Requests.post(GlobVar.PROC_ADDR, params, handler, GlobVar.SUCCESS_FROM_INTERNET);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // http://open.edukg.cn/opedukg/api/typeOpen/open/instanceList
    public static void searchEntity(String course, String searchKey, Handler handler) {
        try {
            JSONObject data = new JSONObject();
            data.put("course", course);
            data.put("searchKey", searchKey);
            sendData(data, "http://open.edukg.cn/opedukg/api/typeOpen/open/instanceList", "GET", handler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // http://open.edukg.cn/opedukg/api/typeOpen/open/infoByInstanceName
    public static void getEntityInfo(String course, String name, Handler handler) {
        try {
            JSONObject data = new JSONObject();
            data.put("course", course);
            data.put("name", name);
            sendData(data, "http://open.edukg.cn/opedukg/api/typeOpen/open/infoByInstanceName", "GET", handler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // http://open.edukg.cn/opedukg/api/typeOpen/open/relatedsubject
    public static void getEntityRelationship(String course, String name, Handler handler) {
        try {
            JSONObject data = new JSONObject();
            data.put("course", course);
            data.put("subjectName", name);
            sendData(data, "http://open.edukg.cn/opedukg/api/typeOpen/open/relatedsubject", "POST", handler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // http://open.edukg.cn/opedukg/api/typeOpen/open/questionListByUriName
    public static void getQuestion(String course, String name, Handler handler) {
        try {
            JSONObject data = new JSONObject();
            data.put("course", course);
            data.put("uriName", name);
            sendData(data, "http://open.edukg.cn/opedukg/api/typeOpen/open/questionListByUriName", "GET", handler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // http://open.edukg.cn/opedukg/api/typeOpen/open/inputQuestion
    public static void getAnswer(String course, String question, Handler handler) {
        try {
            JSONObject data = new JSONObject();
            data.put("course", course);
            data.put("inputQuestion", question);
            sendData(data, "http://open.edukg.cn/opedukg/api/typeOpen/open/inputQuestion", "POST", handler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
