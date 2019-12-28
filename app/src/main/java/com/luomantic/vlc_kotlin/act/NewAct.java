package com.luomantic.vlc_kotlin.act;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.luomantic.vlc_kotlin.R;

public class NewAct extends AppCompatActivity {

    // FIXME: 2019/11/1 特别注意，onCreate只有一个参数，否则跳转到activity的时候是空白（第二次犯错了！！！很严重）
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("onCreate");

        setContentView(R.layout.act_new);
        System.out.println("布局已加载...");
    }

    @Override
    protected void onStart() {
        super.onStart();
        System.out.println("onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        System.out.println("onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        System.out.println("onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("onDestroy");
    }
}
