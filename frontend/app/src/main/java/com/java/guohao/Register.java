package com.java.guohao;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.Objects;

public class Register extends AppCompatActivity {

    private static class SafeHandler extends Handler {
        private final WeakReference<Register> mParent;
        public SafeHandler(Register parent) {
            mParent = new WeakReference<>(parent);
        }
        @Override
        public void handleMessage(@NonNull Message msg) {
            try {
                super.handleMessage(msg);
                Register parent = mParent.get();
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
        Button register_button = findViewById(R.id.registerButton);
        register_button.setOnClickListener(v -> {
            if (Objects.requireNonNull(password.getText()).toString().equals(Objects.requireNonNull(repeat_password.getText()).toString())) {
                HttpUtils.User.register(Objects.requireNonNull(username.getText()).toString(), password.getText().toString(), "", mHandler);
            } else {
                Toast.makeText(this, "两次输入的密码不一致", Toast.LENGTH_LONG).show();
            }
        });
    }

    void jump2login() {
        Intent intent = new Intent(Register.this, Login.class);
        startActivity(intent);
        this.finish();
    }

    @Override
    public void onBackPressed() {
        jump2login();
    }
}