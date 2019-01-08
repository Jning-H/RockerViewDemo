package me.caibou.rockerview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Region;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import me.caibou.rockerview.bean.KeyModel;

/**
 * @author caibou
 */
public class JoystickView extends RockerView {

    private static final String TAG = "JoystickView";
    private Bitmap mBgBmp;        // 视图背景图片  假设是一个圆盘
    private Bitmap mTouchBmp;     // 视图中间的随手指移动的图片  假设是一个圆球
    private Bitmap mDirectionBmp;// 指示方向的图片  假设是一个箭头  整体是一个正方形的图片
    private ValueAnimator mValueAnimatorResetX;// 重置X动画
    private ValueAnimator mValueAnimatorResetY;// 重置Y动画

    private Region ballRegion, invalidRegion;//触摸球可移动有效区域、 操作无效区域(在此范围方向无效，超出此范围的方向才有效)

    private Point center;

    private int edgeRadius, stickRadius, dr;//背景圆半径、触摸球半径、外径距离（两圆距离差）
    private float stickX, stickY;

    private double currentAngle;//当前角度

    private OnAngleUpdateListener angleUpdateListener;//角度监听事件

    public JoystickView(Context context) {
        super(context);
    }

    public JoystickView init(KeyModel model) {
        super.init(model);
        setViewParams(model);
        initialData();
        initialRes();
        resetInvalidRegion();
        return this;
    }

    /**
     * 设置布局参数
     * @param model
     */
    private void setViewParams(KeyModel model) {
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.leftMargin = model.getCenterX() - mViewWidth/2;
        lp.topMargin = model.getCenterY() - mViewHeight/2;
        setLayoutParams(lp);
        setBackgroundColor(Color.GRAY);
    }

    /**
     * 初始化摇杆数据
     */
    private void initialData() {
        edgeRadius = mRadius;
        stickRadius = edgeRadius / 3;

        dr = edgeRadius - stickRadius;

        //初始位置
        center = centerPoint();
        stickX = center.x;
        stickY = center.y;

        currentAngle = -1;
    }

    /**
     * 初始化背景、触摸球图片资源
     */
    private void initialRes() {
        Bitmap tmpBgBmp = BitmapFactory.decodeResource(getResources(), R.mipmap.ui_pic_joystick_left_pad);
        mBgBmp = Bitmap.createScaledBitmap(tmpBgBmp, edgeRadius * 2, edgeRadius * 2, true);
        Bitmap tmpTouchBmp = BitmapFactory.decodeResource(getResources(), R.mipmap.ui_pic_joystick_control_ball);
        mTouchBmp = Bitmap.createScaledBitmap(tmpTouchBmp, stickRadius * 2, stickRadius * 2, true);

        if (isShowDirectionBmp) {
            Bitmap tmpDirectionBmp = BitmapFactory.decodeResource(getResources(), R.mipmap.ui_pic_joystick_arrow);
            mDirectionBmp = Bitmap.createScaledBitmap(tmpDirectionBmp, mViewWidth, mViewHeight, true);
        }
    }

    /**
     * 初始化触摸范围
     */
    private void resetInvalidRegion() {
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
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT);
        canvas.drawBitmap(mBgBmp, mPadding, mPadding, null);//图片//背景
        canvas.drawBitmap(mTouchBmp, stickX - mTouchBmp.getWidth() / 2, stickY - mTouchBmp.getWidth() / 2, null);//图片//触摸球
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
        matrix.postTranslate(offsetX, offsetY);
        canvas.drawBitmap(bitmap, matrix, null);
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

    /**
     * 重置触摸球 动画
     */
    private void resetStick() {
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
