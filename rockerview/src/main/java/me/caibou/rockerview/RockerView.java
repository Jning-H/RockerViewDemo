package me.caibou.rockerview;

import android.content.Context;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Region;
import android.view.MotionEvent;
import android.view.View;

import me.caibou.rockerview.bean.KeyModel;

/**
 * @author caibou
 * TODO 【features】定制数量、键位类型、大小、位置(机型适配github：https://github.com/JessYanCoding/AndroidAutoSize)；
 * TODO 【features】方向指针缩放跟控制盘的大小缩放比例不一致；  另：DirectionView还没有方向指针。
 * TODO 【features】google android keyborad diy；
 */

public abstract class RockerView extends View {

    public static final int ACTION_DOWN = 1;
    public static final int ACTION_MOVE = 0;
    public static final int ACTION_RELEASE = -1;

    public boolean isShowDirectionBmp = false;
    public int mViewWidth;//View的宽
    public int mViewHeight;//View的高
    public int mPadding;// 背景圆到Pad边界的px  一般是留给方向箭头的位置
    public int mRadius;//触摸范围半径 值越大触摸范围越大

    private Region edgeRegion = new Region();//边缘区域

    private Point centerPoint = new Point();


    public RockerView(Context context) {
        super(context);
    }

    //初始化圆心位置
    protected RockerView init(KeyModel model) {
        mViewWidth = model.getWidth();
        mViewHeight = model.getHeight();

        mPadding = model.getPadding();
        mRadius = mViewWidth / 2 - mPadding;

        isShowDirectionBmp = model.isShowDirection();

        centerPoint.x = mViewWidth / 2;
        centerPoint.y = mViewHeight / 2;

        initialTouchRange();
        return this;
    }

    //初始化触摸范围
    private void initialTouchRange() {
        Path edgeRulePath = new Path();
        edgeRulePath.addCircle(centerPoint.x, centerPoint.y, mRadius, Path.Direction.CW);
        Region globalRegion = new Region(
                centerPoint.x - mRadius, centerPoint.y - mRadius,
                centerPoint.x + mRadius, centerPoint.y + mRadius);
        edgeRegion.setPath(edgeRulePath, globalRegion);
    }

    //计算角度
    public double calculateAngle(float dx, float dy) {
        double degrees = Math.toDegrees(Math.atan2(dy, dx));
        return degrees < 0 ? Math.floor(degrees + 360) : degrees;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();
        double angle = calculateAngle(x - centerPoint.x, y - centerPoint.y);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (edgeRegion.contains((int) x, (int) y)) {
                    actionDown(x, y, angle);
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                actionMove(x, y, angle);
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                actionUp(x, y, angle);
                break;
        }
        return super.onTouchEvent(event);
    }

    //view面积
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(mViewWidth, mViewHeight);
    }

    /**
     * Return the center point of the circle.
     *
     * @return The center point of the circle
     */
    public Point centerPoint() {
        return new Point(centerPoint);
    }

    /**
     * Notify the View the current event information of the action down
     *
     * @param x     The event's x coordinate
     * @param y     The event's y coordinate
     * @param angle The angle of the touch point relative to the center of the circle.
     *              represented by an double between 0 and 359.
     */
    protected void actionDown(float x, float y, double angle) {
    }

    /**
     * Notify the View the current event information of the action move
     *
     * @param x     The event's x coordinate
     * @param y     The event's y coordinate
     * @param angle The angle of the touch point relative to the center of the circle.
     *              represented by an double between 0 and 359.
     */
    protected void actionMove(float x, float y, double angle) {
    }

    /**
     * Notify the View the current event information of the action up or cancel
     *
     * @param x     The event's x coordinate
     * @param y     The event's y coordinate
     * @param angle The angle of the touch point relative to the center of the circle.
     *              represented by an double between 0 and 359.
     */
    protected void actionUp(float x, float y, double angle) {
    }

}
