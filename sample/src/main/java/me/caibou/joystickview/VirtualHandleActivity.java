package me.caibou.joystickview;

import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import me.caibou.rockerview.DirectionView;
import me.caibou.rockerview.JoystickView;
import me.caibou.rockerview.KeyContainerView;
import me.caibou.rockerview.RockerView;
import me.caibou.rockerview.bean.KeyModel;

public class VirtualHandleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_virtual_handle);

        autoAddKey();
    }

    private void autoAddKey() {
        RelativeLayout layout = findViewById(R.id.rl_layout);
        //配置信息
        List<KeyModel> modelList = new ArrayList<>();
        for(int i = 0 ; i< 1 ; i++){
            modelList.add(new KeyModel(300,300, 30,0,0,true));
        }

        //TODO 方案一：check requestLayout() is work,if no,add invalidate();
        new KeyContainerView(this).init(modelList).requestLayout();

        //TODO 方案二：
        for(int i = 0 ; i< modelList.size() ; i++){
            //TODO create view （原来拿布局宽高等数据的初始化方法可以不要了，根据Model来）
            //TODO 根据KeyType创建对应的键，用Builder还是Factory？
            RockerView view = new JoystickView(this).init(modelList.get(i));
            layout.addView(view);
        }

        RockerView view = new DirectionView(this).init(new KeyModel(300,300, 30,500,500,true));
        layout.addView(view);

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }
}
