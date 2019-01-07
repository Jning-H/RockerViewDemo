package me.caibou.joystickview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import me.caibou.rockerview.JoystickView;

public class VirtualHandleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_virtual_handle);
        JoystickView joystickView = findViewById(R.id.joystick_left_control);
    }
}
