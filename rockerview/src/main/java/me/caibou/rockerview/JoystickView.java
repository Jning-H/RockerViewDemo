package me.caibou.rockerview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Region;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;

/**
 * @author caibou
 */
public class JoystickView extends RockerView {

    private static final String TAG = "JoystickView";
    //add
    private Bitmap mBgBmp;        // 视图背景图片  假设是一个圆盘
    private Bitmap mTouchBmp;     // 视图中间的随手指移动的图片  假设是一个圆球
    private Bitmap mDirectionBmp;// 指示方向的图片  假设是一个箭头  整体是一个正方形的图片
    private ValueAnimator mValueAnimatorResetX;// 重置X动画
    private ValueAnimator mValueAnimatorResetY;// 重置Y动画
    private boolean isShowDirectionBmp = true;
//    private int mRoundBgPadding = 20;// 背景圆到view边界的像素

    private Region ballRegion, invalidRegion;//触摸球可移动有效区域、 操作无效区域(在此范围方向无效，超出此范围的方向才有效)

    private Paint paint = new Paint();
    private Point center;

    private int edgeRadius, stickRadius, dr;//背景圆半径、触摸球半径、外径距离（两圆距离差）
    private float stickX, stickY;
    private int stickBallColor;

    private double currentAngle;//当前角度

    private OnAngleUpdateListener angleUpdateListener;//角度监听事件

    public JoystickView(Context context) {
        this(context, null);
    }

    public JoystickView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public JoystickView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setBackgroundResource(R.color.color_default);
        initialAttr(context, attrs);
        initialData();
    }

    private void initialAttr(Context context, @Nullable AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.JoystickView);
        edgeRadius = typedArray.getDimensionPixelSize(R.styleable.JoystickView_edge_radius, 200);
        stickRadius = typedArray.getDimensionPixelSize(R.styleable.JoystickView_stick_radius, edgeRadius / 3);
        stickBallColor = typedArray.getColor(R.styleable.JoystickView_stick_color, getResources().getColor(R.color.stick_default_color));
        typedArray.recycle();
    }

    /**
     * 初始化摇杆数据
     */
    private void initialData() {
        //add 背景、触摸球图片资源
        Bitmap tmpBgBmp = BitmapFactory.decodeResource(getResources(), R.mipmap.ui_pic_joystick_left_pad);
        Bitmap tmpTouchBmp = BitmapFactory.decodeResource(getResources(), R.mipmap.ui_pic_joystick_control_ball);
        mBgBmp = Bitmap.createScaledBitmap(tmpBgBmp, edgeRadius * 2, edgeRadius * 2, true);
        mTouchBmp = Bitmap.createScaledBitmap(tmpTouchBmp, stickRadius * 2, stickRadius * 2, true);
        if (isShowDirectionBmp) {
            Bitmap tmpDirectionBmp = BitmapFactory.decodeResource(getResources(), R.mipmap.ui_pic_joystick_arrow);
            mDirectionBmp = Bitmap.createScaledBitmap(tmpDirectionBmp, (int) edgeRadius * 2, (int) edgeRadius * 2, true);
        }

        //背景和触摸球间距
        dr = edgeRadius - stickRadius;

        //初始位置
        center = centerPoint();
        stickX = center.x;
        stickY = center.y;

        //触摸球可移动范围
        Region ballRegionClip = new Region(center.x - dr, center.y - dr,
                center.x + dr, center.y + dr);
        Path rockerRulePath = new Path();
        rockerRulePath.addCircle(center.x, center.y, dr, Path.Direction.CW);
        ballRegion = new Region();
        ballRegion.setPath(rockerRulePath, ballRegionClip);

        //触摸球无效范围
        int invalidRadius = edgeRadius;
        Region invalidRegionClip = new Region(center.x - invalidRadius, center.y - invalidRadius,
                center.x + invalidRadius, center.y + invalidRadius);
        Path eventInvalidPath = new Path();
        eventInvalidPath.addCircle(center.x, center.y, invalidRadius, Path.Direction.CW);
        invalidRegion = new Region();
        invalidRegion.setPath(eventInvalidPath, invalidRegionClip);

        currentAngle = -1;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.i(TAG, "onMeasure()");
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        Log.i(TAG, "onLayout()");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.i(TAG, "onDraw()");
        canvas.drawColor(Color.TRANSPARENT);
        drawRockerEdge(canvas);//背景
        drawStickBall(canvas);//触摸球
        if (isShowDirectionBmp && center.x != stickX && center.y != stickY) {
            drawRotateBitmap(canvas, mDirectionBmp, (float) currentAngle + 180, 0, 0);
        }
    }

    /**
     * @param canvas   画布
     * @param bitmap   要绘制的bitmap
     * @param rotation 旋转角度
     * @param posX     左上角顶点的x值 - left
     * @param posY     左上角顶点的y值 - top
     */
    private static void drawRotateBitmap(Canvas canvas, Bitmap bitmap,
                                         float rotation, float posX, float posY) {
        Matrix matrix = new Matrix();
        int offsetX = bitmap.getWidth() / 2;
        int offsetY = bitmap.getHeight() / 2;
        matrix.postTranslate(-offsetX, -offsetY);
        matrix.postRotate(rotation);
        matrix.postTranslate(posX + offsetX, posY + offsetY);
        canvas.drawBitmap(bitmap, matrix, null);
    }

    /**
     * 绘制背景
     *
     * @param canvas
     */
    protected void drawRockerEdge(Canvas canvas) {
//        paint.reset();
//        paint.setColor(Color.BLACK);
//        paint.setStyle(Paint.Style.STROKE);
//        paint.setStrokeWidth(2.0f);
//        canvas.drawCircle(center.x, center.y, edgeRadius, paint);//tip 画笔
        canvas.drawBitmap(mBgBmp, 0, 0, null);//图片
    }

    /**
     * 绘制触摸球
     *
     * @param canvas
     */
    protected void drawStickBall(Canvas canvas) {
//        paint.reset();
//        paint.setColor(stickBallColor);
//        paint.setStyle(Paint.Style.FILL);
//        canvas.drawCircle(stickX, stickY, stickRadius, paint);//tip 画笔
        canvas.drawBitmap(mTouchBmp, stickX - mTouchBmp.getWidth() / 2 , stickY - mTouchBmp.getWidth() / 2 , null);//图片
    }

    /**
     * 更新触摸球坐标
     *
     * @param x
     * @param y
     */
    private void updateStickPos(float x, float y) {
        if (ballRegion.contains((int) x, (int) y)) {
            stickX = x;
            stickY = y;
            Log.i(TAG, "updateStickPos_contains()    stickX=" + stickX + ", stickY=" + stickY);
        } else {
            float dx = x - center.x;//600-200 ^2 = 160000
            float dy = y - center.y;//600-200 ^2 = 160000   0
            float scale = (float) Math.sqrt((Math.pow(dx, 2) + Math.pow(dy, 2)));//720000 > 360000 / 180000
            stickX = dx * dr / scale + center.x;//   360000*100/600+200
            stickY = dy * dr / scale + center.y;
            Log.i(TAG, "updateStickPos_else()    stickX=" + (dx * dr / scale) + ", stickY=" + (dy * dr / scale));
        }
        invalidate();
    }

    //TODO TouchView reset() Animation

    /**
     * 重置触摸球
     */
    private void resetStick() {
        Log.i(TAG, "resetStick()");
        //tip 粗暴重置
//        currentAngle = -1;
//        stickX = center.x;
//        stickY = center.y;
//        invalidate();

        //重置动画
        mValueAnimatorResetX = ValueAnimator.ofFloat(stickX, center.x).setDuration(200);
        mValueAnimatorResetX.start();
        mValueAnimatorResetX.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                stickX = (Float) animation.getAnimatedValue();
                invalidate();
            }
        });

        mValueAnimatorResetY = ValueAnimator.ofFloat(stickY, center.y).setDuration(200);
        mValueAnimatorResetY.start();
        mValueAnimatorResetY.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                stickY = (Float) animation.getAnimatedValue();
                invalidate();
            }
        });
    }

    /**
     * 更新角度
     *
     * @param angle
     * @param action
     */
    private void updateAngle(double angle, int action) {
        currentAngle = angle;
        if (angleUpdateListener != null) {
            angleUpdateListener.onAngleUpdate(angle, action);
        }
    }

    @Override
    protected void actionDown(float x, float y, double angle) {
//        if (!invalidRegion.contains((int) x, (int) y)) {//tip 不触边视为无效操作
        updateAngle(angle, ACTION_DOWN);
//        }
        updateStickPos(x, y);
    }

    @Override
    protected void actionMove(float x, float y, double angle) {
        if (mValueAnimatorResetX != null && mValueAnimatorResetY != null) {
            mValueAnimatorResetX.removeAllUpdateListeners();
            mValueAnimatorResetY.removeAllUpdateListeners();
        }
//        if (!invalidRegion.contains((int) x, (int) y)) {//tip 不触边视为无效操作
        updateAngle(angle, ACTION_MOVE);
//        }
        updateStickPos(x, y);
    }

    @Override
    protected void actionUp(float x, float y, double angle) {
        resetStick();
        updateAngle(angle, ACTION_RELEASE);
    }

    /**
     * Returns the current angle.
     *
     * @return The current angle, -1 represent no touch action.
     */
    public double getCurrentAngle() {
        return currentAngle;
    }

    /**
     * Register a callback to be invoked when the angle is updated.
     *
     * @param angleUpdateListener The callback that will run.
     */
    public void setAngleUpdateListener(OnAngleUpdateListener angleUpdateListener) {
        this.angleUpdateListener = angleUpdateListener;
    }

    /**
     * Interface definition for a callback to be invoked when The angle between the finger
     * and the center of the circle update.
     */
    public interface OnAngleUpdateListener {

        /**
         * Called when angle has been clicked.
         *
         * @param angle  The angle between the finger and the center of the circle.
         * @param action action of the finger.
         */
        void onAngleUpdate(double angle, int action);
    }
}
