package com.java.guohao;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

import java.lang.ref.WeakReference;

public class Login extends AppCompatActivity {

    private static class SafeHandler extends Handler {
        private final WeakReference<Login> mParent;
        public SafeHandler(Login parent) {
            mParent = new WeakReference<>(parent);
        }
        @Override
        public void handleMessage(@NonNull Message msg) {
            try {
                super.handleMessage(msg);
                Login parent = mParent.get();
                JSONObject obj = new JSONObject(msg.obj.toString());
                if (obj.get("ok").equals(1)) {
                    Toast.makeText(parent, "登录成功", Toast.LENGTH_LONG).show();
                    HttpUtils.user.session = obj.get("session").toString();
                    parent.finish();
                } else {
                    Toast.makeText(parent, "登录失败", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    SafeHandler mHandler = new SafeHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        TextInputEditText username = findViewById(R.id.login_username_text);
        TextInputEditText password = findViewById(R.id.login_password_text);
        Button login_button = findViewById(R.id.loginButton), register_button = findViewById(R.id.register_button);
        login_button.setOnClickListener(v -> HttpUtils.User.login(username.getText().toString(), password.getText().toString(), mHandler));
        register_button.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, Register.class);
            startActivity(intent);
            this.finish();
        });
    }

}