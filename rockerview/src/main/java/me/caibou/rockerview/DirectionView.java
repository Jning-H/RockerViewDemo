package me.caibou.rockerview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import me.caibou.rockerview.bean.KeyModel;

/**
 * @author caibou
 */
public class DirectionView extends RockerView {

    public enum Direction {
        NONE, UP, DOWN, LEFT, RIGHT,
        UP_AND_LEFT, UP_AND_RIGHT, DOWN_AND_LEFT, DOWN_AND_RIGHT
    }

    private Bitmap mBgBmp;
    private Bitmap mArrowLeftNor, mArrowLeftPre, mArrowRightNor, mArrowRightPre,
            mArrowUpNor, mArrowUpPre,mArrowDownNor, mArrowDownPre, mArrowLeft, mArrowRight, mArrowUp, mArrowDown;
    private int mArrowSize;

    private Direction currentDirection;

    private int edgeRadius;

    private Region invalidRegion = new Region();
    private Point centerPoint = new Point();

    private DirectionChangeListener directionChangeListener;

    public DirectionView(Context context) {
        super(context);
    }

    @Override
    public DirectionView init(KeyModel model) {
        super.init(model);
        setViewParams(model);
        initialData();
        initialRes();
        resetDirection();
        resetInvalidRegion();
        return this;
    }

    private void setViewParams(KeyModel model) {
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.leftMargin = model.getCenterX() - mViewWidth/2;
        lp.topMargin = model.getCenterY() - mViewHeight/2;
        setLayoutParams(lp);
        setBackgroundColor(Color.GRAY);
    }

    private void initialData() {
        edgeRadius = mRadius;
        centerPoint = centerPoint();
        currentDirection = Direction.NONE;
    }

    private void initialRes() {
        Bitmap tmpBgBmp = BitmapFactory.decodeResource(getResources(), R.mipmap.ui_pic_joystick_left_pad);
        mBgBmp = Bitmap.createScaledBitmap(tmpBgBmp, edgeRadius * 2, edgeRadius * 2, true);

        mArrowSize = edgeRadius / 4 ;
        Bitmap tempArrowLeftNor = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_direction_left_nor);
        Bitmap tempArrowLeftPre = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_direction_left_pre);
        Bitmap tempArrowUpNor = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_direction_up_nor);
        Bitmap tempArrowUpPre = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_direction_up_pre);
        Bitmap tempArrowRightNor = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_direction_right_nor);
        Bitmap tempArrowRightPre = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_direction_right_pre);
        Bitmap tempArrowDownNor = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_direction_down_nor);
        Bitmap tempArrowDownPre = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_direction_down_pre);

        mArrowLeftNor = Bitmap.createScaledBitmap(tempArrowLeftNor, mArrowSize, mArrowSize, true);
        mArrowLeftPre = Bitmap.createScaledBitmap(tempArrowLeftPre, mArrowSize, mArrowSize, true);
        mArrowUpNor = Bitmap.createScaledBitmap(tempArrowUpNor, mArrowSize, mArrowSize, true);
        mArrowUpPre = Bitmap.createScaledBitmap(tempArrowUpPre, mArrowSize, mArrowSize, true);
        mArrowRightNor = Bitmap.createScaledBitmap(tempArrowRightNor, mArrowSize, mArrowSize, true);
        mArrowRightPre = Bitmap.createScaledBitmap(tempArrowRightPre, mArrowSize, mArrowSize, true);
        mArrowDownNor = Bitmap.createScaledBitmap(tempArrowDownNor, mArrowSize, mArrowSize, true);
        mArrowDownPre = Bitmap.createScaledBitmap(tempArrowDownPre, mArrowSize, mArrowSize, true);
    }

    private void resetInvalidRegion() {
        int invalidRadius = edgeRadius / 3;
        Region invalidRegionClip = new Region(centerPoint.x - invalidRadius,
                centerPoint.y - invalidRadius, centerPoint.x + invalidRadius,
                centerPoint.y + invalidRadius);
        Path eventInvalidPath = new Path();
        eventInvalidPath.addCircle(centerPoint.x, centerPoint.y, invalidRadius, Path.Direction.CW);
        invalidRegion.setPath(eventInvalidPath, invalidRegionClip);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawEdge(canvas);
        drawDirectButton(canvas);
    }

    protected void drawEdge(Canvas canvas) {
        canvas.drawBitmap(mBgBmp, mPadding, mPadding, null);//图片//背景
    }

    protected void drawDirectButton(Canvas canvas) {
        //朝上箭头
        canvas.drawBitmap(mArrowUp,null, new Rect(
                centerPoint.x - mArrowSize,
                mPadding * 2 - mArrowSize,
                centerPoint.x + mArrowSize ,
                mPadding * 2 + mArrowSize
        ),null);

        canvas.drawBitmap(mArrowDown,null, new Rect(
                centerPoint.x - mArrowSize,
                mViewHeight - mPadding * 2 - mArrowSize,
                centerPoint.x + mArrowSize ,
                mViewHeight - mPadding * 2 + mArrowSize
        ),null);

        canvas.drawBitmap(mArrowLeft,null, new Rect(
                mPadding * 2 - mArrowSize,
                centerPoint.y  - mArrowSize ,
                mPadding * 2 + mArrowSize,
                centerPoint.y + mArrowSize
        ),null);

        canvas.drawBitmap(mArrowRight,null, new Rect(
                mViewHeight - mPadding * 2 - mArrowSize,
                centerPoint.y  - mArrowSize ,
                mViewHeight - mPadding * 2 + mArrowSize,
                centerPoint.y + mArrowSize
        ),null);

    }

    @Override
    protected void actionDown(float x, float y, double angle) {
//        if (!invalidRegion.contains((int) x, (int) y)) {
            updateIndicator(angle);
//        }
    }

    @Override
    protected void actionMove(float x, float y, double angle) {
//        if (!invalidRegion.contains((int) x, (int) y)) {
            updateIndicator(angle);
//        }
    }

    @Override
    protected void actionUp(float x, float y, double angle) {
        resetIndicator();
        notifyDirection(Direction.NONE);
    }

    private void resetIndicator() {
        resetDirection();
        invalidate();
    }

    private void updateIndicator(double angle) {
        resetDirection();
        if (Utils.range(angle, 337.5, 360) || Utils.range(angle, 0, 22.5)) {
            mArrowRight = mArrowRightPre;
            notifyDirection(Direction.RIGHT);
        } else if (Utils.range(angle, 22.5, 67.5)) {
            mArrowRight = mArrowRightPre;
            mArrowDown = mArrowDownPre;
            notifyDirection(Direction.DOWN_AND_RIGHT);
        } else if (Utils.range(angle, 67.5, 112.5)) {
            mArrowDown = mArrowDownPre;
            notifyDirection(Direction.DOWN);
        } else if (Utils.range(angle, 112.5, 157.5)) {
            mArrowLeft = mArrowLeftPre;
            mArrowDown = mArrowDownPre;
            notifyDirection(Direction.DOWN_AND_LEFT);
        } else if (Utils.range(angle, 157.5, 202.5)) {
            mArrowLeft = mArrowLeftPre;
            notifyDirection(Direction.LEFT);
        } else if (Utils.range(angle, 202.5, 247.5)) {
            mArrowUp = mArrowUpPre;
            mArrowLeft = mArrowLeftPre;
            notifyDirection(Direction.UP_AND_LEFT);
        } else if (Utils.range(angle, 247.5, 292.5)) {
            mArrowUp = mArrowUpPre;
            notifyDirection(Direction.UP);
        } else if (Utils.range(angle, 292.5, 337.5)) {
            mArrowUp = mArrowUpPre;
            mArrowRight = mArrowRightPre;
            notifyDirection(Direction.UP_AND_RIGHT);
        }
        invalidate();
    }

    private void resetDirection() {
        mArrowUp = mArrowUpNor;
        mArrowDown = mArrowDownNor;
        mArrowLeft = mArrowLeftNor;
        mArrowRight = mArrowRightNor;
    }

    private void notifyDirection(Direction direction) {
        if (directionChangeListener != null) {
            directionChangeListener.onDirectChange(direction);
        }
    }

    /**
     * Returns the current Direction.
     *
     * @return The current Direction.
     */
    public Direction getCurrentDirection(){
        return currentDirection;
    }

    /**
     * Register a callback to be invoked when the direction changed.
     *
     * @param directionChangeListener The callback that will run.
     */
    public void setDirectionChangeListener(DirectionChangeListener directionChangeListener) {
        this.directionChangeListener = directionChangeListener;
    }

    /**
     * Interface definition for a callback to be invoked when The direction changed.
     */
    public interface DirectionChangeListener {

        /**
         * Called when direction changed.
         *
         * @param direction The direction of the center to finger.
         */
        void onDirectChange(Direction direction);
    }
}
