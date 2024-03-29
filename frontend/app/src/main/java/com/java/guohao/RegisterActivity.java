package com.java.guohao;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private static class SafeHandler extends Handler {
        private final WeakReference<RegisterActivity> mParent;
        public SafeHandler(RegisterActivity parent) {
            mParent = new WeakReference<>(parent);
        }
        @Override
        public void handleMessage(@NonNull Message msg) {
            try {
                super.handleMessage(msg);
                RegisterActivity parent = mParent.get();
                JSONObject obj = new JSONObject(msg.obj.toString());
                if (obj.get("ok").equals(1)) {
                    Toast.makeText(parent, "注册成功", Toast.LENGTH_LONG).show();
                    parent.jump2login();
                } else {
                    Toast.makeText(parent, obj.get("msg").toString(), Toast.LENGTH_LONG).show();
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
        setContentView(R.layout.activity_register);
        TextInputEditText username = findViewById(R.id.register_username_text);
        TextInputEditText password = findViewById(R.id.register_password_text);
        TextInputEditText repeat_password = findViewById(R.id.register_repeat_password_text);
        FloatingActionButton register_button = findViewById(R.id.registerButton);
        register_button.setOnClickListener(v -> {
            if (Objects.requireNonNull(password.getText()).toString().equals(Objects.requireNonNull(repeat_password.getText()).toString())) {
                HttpUtils.User.register(Objects.requireNonNull(username.getText()).toString(), password.getText().toString(), "", mHandler);
            } else {
                Toast.makeText(this, "两次输入的密码不一致", Toast.LENGTH_LONG).show();
            }
        });
    }

    void jump2login() {
        this.finishAfterTransition();
    }

    @Override
    public void onBackPressed() {
        jump2login();
    }
}