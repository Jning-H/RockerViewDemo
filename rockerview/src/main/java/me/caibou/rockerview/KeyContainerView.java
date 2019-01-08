package me.caibou.rockerview;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import me.caibou.rockerview.bean.KeyModel;

@Deprecated
public class KeyContainerView extends RelativeLayout {
    private Context mContext;
    private List<View> mListView;

    public KeyContainerView(Context context) {
        super(context);
        mContext = context;
    }

    public KeyContainerView init(List<KeyModel> modelList){
        //创建View
        mListView = new ArrayList<>();
        for(int i = 0 ; i< modelList.size() ; i++){

            //TODO create view （原来拿布局宽高等数据的初始化方法可以不要了，根据Model来）
            //TODO 根据KeyType创建对应的键，用Builder还是Factory？
//            JoystickView joystickView = new JoystickView(mContext).init(modelList.get(i));

            // tip：
            //      此处单纯返回View不行（用个空View存Model or 用个对象存View和Model）
            //      还要带着Model数据给容器绘制信息（大小、位置）
//            mListView.add(new View(mContext));

        }
        return this;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //TODO onMeasure

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        //TODO onLayout

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //TODO onDraw
    }
}
