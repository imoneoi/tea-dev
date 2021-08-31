package com.one.backend;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.catalina.User;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


@RestController
public class Api {
    final private ConcurrentHashMap<String, UserData> users = new ConcurrentHashMap<>();  // user name --> data
    final private ConcurrentHashMap<String, String> sessions = new ConcurrentHashMap<>(); // session --> user name

    // data synchronization
    public void sync_write_user(String username) {

    }

    public void sync_load_all() {

    }

    // on load sync
    Api() {
        super();
        sync_load_all();
    }

    // api interfaces
    @RequestMapping("/hello")
    public HashMap<String, Object> api_hello() {
        HashMap<String, Object> result = new HashMap<>();

        result.put("data", "hello, world!");
        return result;
    }

    @RequestMapping("/login")
    public HashMap<String, Object> api_login(@RequestBody Map<String, String> payload) {
        HashMap<String, Object> result = new HashMap<>();

        String username = payload.getOrDefault("user", "");
        String passwd = payload.getOrDefault("passwd", "");
        String passwd_hash = DigestUtils.md5DigestAsHex(passwd.getBytes(StandardCharsets.UTF_8));

        if (users.containsKey(username) && users.get(username).passwd_hash.equals(passwd_hash)) {
            String session = UUID.randomUUID().toString();

            users.get(username).session = session;
            sessions.put(session, username);

            result.put("ok", 1);
            result.put("session", session);
            result.put("msg", "welcome, " + username);
        } else {
            result.put("ok", 0);
            result.put("session", "");
            result.put("msg", "wrong username or password");
        }
        return result;
    }

    @RequestMapping("/register")
    public HashMap<String, Object> api_register(@RequestBody Map<String, String> payload) {
        HashMap<String, Object> result = new HashMap<>();

        String username = payload.getOrDefault("user", "");
        String passwd = payload.getOrDefault("passwd", "");
        String data = payload.getOrDefault("data", "");
        if (username.isEmpty() || passwd.isEmpty()) {
            result.put("ok", 0);
            result.put("msg", "username and password cannot be empty");
        } else if (users.containsKey(username)) {
            result.put("ok", 0);
            result.put("msg", "existing username");
        } else {
            // checks passed
            // hash password
            String passwd_hash = DigestUtils.md5DigestAsHex(passwd.getBytes(StandardCharsets.UTF_8));

            UserData userdata = new UserData();
            userdata.username = username;
            userdata.passwd_hash = passwd_hash;
            userdata.data = data;
            userdata.session = "";

            users.put(userdata.username, userdata);
            sync_write_user(userdata.username);

            result.put("ok", 1);
            result.put("msg", "registered, " + userdata.username);
        }

        return result;
    }

    @RequestMapping("/userdata")
    public HashMap<String, Object> api_userdata(@RequestBody Map<String, String> payload) {
        HashMap<String, Object> result = new HashMap<>();

        String session = payload.getOrDefault("session", "");
        String data = payload.getOrDefault("data", "");
        if (sessions.containsKey(session)) {
            UserData userdata = users.get(sessions.get(session));

            if (!data.isEmpty()) {
                userdata.data = data;
                sync_write_user(userdata.username);
            }
            result.put("ok", 1);
            result.put("data", userdata.data);
        }
        else {
            result.put("ok", 0);
            result.put("data", "");
        }

        return result;
    }

    // FIXME: Not tested below
    // remote proc ids
    private String request(String url, String method, String payload) {
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod(method);
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
            con.setDoOutput(true);

            if (!payload.isEmpty()) {
                try (OutputStream os = con.getOutputStream()) {
                    byte[] input = payload.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
            }

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                return response.toString();
            }
        }
        catch (Exception e) {
            return "";
        }
    }

    private String edukg_id = "";

    private void edukg_login() {
        String result = request("http://open.edukg.cn/opedukg/api/typeAuth/user/login", "POST",
                "{\"phone\": 15049957199,\"password\": \"Vg0jhog4SG461VhXx7Zq\"}");
        try {
            TypeReference<HashMap<String, Object>> type_ref = new TypeReference<>() {};
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> json = mapper.readValue(result, type_ref);

            if (json.containsKey("id")) {
                edukg_id = (String) json.getOrDefault("id", "");
            }
        } catch (Exception e) {

        }
    }

    @RequestMapping("/proc")
    public String api_proc(@RequestBody Map<String, String> payload) {
        String session = payload.getOrDefault("session", "");
        String url = payload.getOrDefault("url", "");
        String method = payload.getOrDefault("method", "");
        String data = payload.getOrDefault("data", "");

        if (sessions.containsKey(session)) {
            // send request
            try {
                String result = request(url, method, data);

                // json decode
                TypeReference<HashMap<String, Object>> type_ref = new TypeReference<>() {};
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> json = mapper.readValue(result, type_ref);

                String code = (String) json.getOrDefault("code", "-1");
                if (code.equals("-1")) {
                    edukg_login();
                    return request(url, method, data);
                }

                return result;
            }
            catch (Exception e) {
                return "";
            }
        }
        else {
            return "";
        }
    }
}