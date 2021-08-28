package com.java.guohao;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class Helper {
    public static void ImageViewAnimatedChange(Context c, final ImageView v, final Integer resId, final Integer colorId, long duration) {
        final Animation anim_out = AnimationUtils.loadAnimation(c, android.R.anim.fade_out);
        final Animation anim_in  = AnimationUtils.loadAnimation(c, android.R.anim.fade_in);
        anim_in.setDuration(duration);
        anim_out.setDuration(duration / 2);
        anim_out.setAnimationListener(new Animation.AnimationListener()
        {
            @Override public void onAnimationStart(Animation animation) {}
            @Override public void onAnimationRepeat(Animation animation) {}
            @Override public void onAnimationEnd(Animation animation)
            {
                v.setImageResource(resId);
                if (colorId != null) {
                    v.setColorFilter(c.getResources().getColor(colorId));
                }
                anim_in.setAnimationListener(new Animation.AnimationListener() {
                    @Override public void onAnimationStart(Animation animation) {}
                    @Override public void onAnimationRepeat(Animation animation) {}
                    @Override public void onAnimationEnd(Animation animation) {}
                });
                v.startAnimation(anim_in);
            }
        });
        v.startAnimation(anim_out);
    }

    public static void ImageViewFadeIn(Context c, final ImageView v, final Integer resId, long duration) {
        final Animation anim_in  = AnimationUtils.loadAnimation(c, android.R.anim.fade_in);
        anim_in.setDuration(duration);
        v.setImageResource(resId);
        v.startAnimation(anim_in);
    }

    public static String array2Str(String[] l) {
        return TextUtils.join("\n", l);
    }

    public static String[] str2Array(String s) {
        if (!s.equals("")) {
            return s.split("\n");
        } else {
            return new String[0];
        }
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }
}

