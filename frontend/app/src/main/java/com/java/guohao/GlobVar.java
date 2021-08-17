package com.java.guohao;

import java.util.HashMap;

public class GlobVar {
    public static final int CONNECTION_TIMEOUT = 5000;
    public static final int SUCCESS = 0;
    public static final int BOTTOM_NAV_LENGTH = 4;
    public static final String API_ADDR = "http://47.102.217.32:19741/api/";
    public static final String LOGIN_ADDR = API_ADDR + "login";
    public static final String REGISTER_ADDR = API_ADDR + "register";
    public static final String USERDATA_ADDR = API_ADDR + "userdata";
    public static final String PROC_ADDR = API_ADDR + "proc";
    public static final String SUBJECTS[] = { "语文", "英语", "数学", "物理", "化学", "生物", "历史", "地理", "政治" };
    public static final String SUBJECT_KEYWORDS[] = {"chinese", "english", "math", "physics", "chemistry", "biology", "history", "geo", "politics"};
    public static HashMap<String, String> KEYWORD_OF_SUBJECT;
    static {
        KEYWORD_OF_SUBJECT = new HashMap<>();
        for (int i = 0; i < SUBJECTS.length; ++i) {
            KEYWORD_OF_SUBJECT.put(SUBJECTS[i], SUBJECT_KEYWORDS[i]);
        }
    }
}
