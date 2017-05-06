package com.lx852357.lineviewdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.lx852357.lineviewdemo.view.LineView;

public class MainActivity extends AppCompatActivity {
    private LineView lineView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lineView = (LineView) findViewById(R.id.line);

    }
}
