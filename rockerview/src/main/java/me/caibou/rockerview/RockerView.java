package me.caibou.rockerview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Region;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * @author caibou
 * TODO 定制数量、键位类型、大小、位置
 * TODO 方向指针缩放跟控制盘的大小缩放比例不一致
 */

public abstract class RockerView extends View {

    public static final int ACTION_DOWN = 1;
    public static final int ACTION_MOVE = 0;
    public static final int ACTION_RELEASE = -1;

    private Region edgeRegion = new Region();//边缘区域

    private Point centerPoint = new Point();
    private int radius;//触摸范围半径 值越大触摸范围越大
    public int mPadding;// 背景圆到Pad边界的px  一般是留给方向箭头的位置
    public int mViewSize;//View的长宽

    public RockerView(Context context) {
        this(context, null);
    }

    public RockerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RockerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeData(context, attrs);
        initialTouchRange();
    }

    //初始化圆心位置
    private void initializeData(Context context, @Nullable AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RockerView);
        radius = typedArray.getDimensionPixelSize(R.styleable.RockerView_edge_radius, 200);
        typedArray.recycle();

        mPadding = dip2px(context, 20);
        mViewSize = (radius + mPadding) * 2;

        centerPoint.x = centerPoint.y = mViewSize / 2;
    }

    //初始化触摸范围
    private void initialTouchRange() {
        Path edgeRulePath = new Path();
        edgeRulePath.addCircle(centerPoint.x, centerPoint.y, radius, Path.Direction.CW);
        Region globalRegion = new Region(
                centerPoint.x - radius, centerPoint.y - radius,
                centerPoint.x + radius, centerPoint.y + radius);
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
        int sideLength = mViewSize;
        setMeasuredDimension(sideLength, sideLength);
    }

    /**
     * 根据手机分辨率从DP转成PX
     * @param context
     * @param dpValue
     * @return
     */
    public static int dip2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * Return the center point of the circle.
     *
     * @return The center point of the circle
     */
    public Point centerPoint() { return new Point(centerPoint); }

    /**
     * Notify the View the current event information of the action down
     *
     * @param x     The event's x coordinate
     * @param y     The event's y coordinate
     * @param angle The angle of the touch point relative to the center of the circle.
     *              represented by an double between 0 and 359.
     */
    protected void actionDown(float x, float y, double angle) { }

    /**
     * Notify the View the current event information of the action move
     *
     * @param x     The event's x coordinate
     * @param y     The event's y coordinate
     * @param angle The angle of the touch point relative to the center of the circle.
     *              represented by an double between 0 and 359.
     */
    protected void actionMove(float x, float y, double angle) { }

    /**
     * Notify the View the current event information of the action up or cancel
     *
     * @param x     The event's x coordinate
     * @param y     The event's y coordinate
     * @param angle The angle of the touch point relative to the center of the circle.
     *              represented by an double between 0 and 359.
     */
    protected void actionUp(float x, float y, double angle) { }

}
