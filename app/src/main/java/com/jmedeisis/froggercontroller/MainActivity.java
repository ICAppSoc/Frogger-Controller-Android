package com.jmedeisis.froggercontroller;

import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.ActionBarActivity;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ControllerFragment())
                    .commit();
        }

        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }


}
