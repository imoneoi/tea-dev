package com.java.guohao;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private static class SafeHandler extends Handler {
        private final WeakReference<LoginActivity> mParent;
        public SafeHandler(LoginActivity parent) {
            mParent = new WeakReference<>(parent);
        }
        @Override
        public void handleMessage(@NonNull Message msg) {
            try {
                super.handleMessage(msg);
                LoginActivity parent = mParent.get();
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
        FloatingActionButton login_button = findViewById(R.id.loginButton);
        Button register_button = findViewById(R.id.register_button);
        login_button.setOnClickListener(v -> HttpUtils.User.login(
                Objects.requireNonNull(username.getText()).toString(),
                Objects.requireNonNull(password.getText()).toString(), mHandler)
        );
        register_button.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onBackPressed() {
        Intent home = new Intent(Intent.ACTION_MAIN);
        home.addCategory(Intent.CATEGORY_HOME);
        startActivity(home);
    }
}