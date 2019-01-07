package me.caibou.rockerview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

/**
 * @author caibou
 */
public class DirectionView extends RockerView {

    private static final int INSIDE_CIRCLE_RADIUS = 30;
    private static final int INDICATOR_SWEEP_ANGLE = 90;

    public enum Direction {
        NONE, UP, DOWN, LEFT, RIGHT,
        UP_AND_LEFT, UP_AND_RIGHT, DOWN_AND_LEFT, DOWN_AND_RIGHT
    }

    private Bitmap mBgBmp;
    private Bitmap mArrowLeftNor, mArrowLeftPre, mArrowRightNor, mArrowRightPre,
            mArrowUpNor, mArrowUpPre,mArrowDownNor, mArrowDownPre, mArrowLeft, mArrowRight, mArrowUp, mArrowDown;
    private int mArrowSize;

    private Direction currentDirection;

    private boolean pressedStatus = false;
    private int edgeRadius, buttonRadius, sideWidth;
    private int indicatorColor;
    private float startAngle;

    private Region invalidRegion = new Region();
    private Point centerPoint = new Point();

    private Paint paint = new Paint();
    private Path directPath;
    private RectF indicatorRect;

    private DirectionChangeListener directionChangeListener;

    public DirectionView(Context context) {
        this(context, null);
    }

    public DirectionView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DirectionView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setBackgroundResource(R.color.color_default);
        initialAttr(context, attrs);
        initialBitmap(context);
        initialData();
        resetInvalidRegion();
    }

    private void initialAttr(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.DirectionView);
        buttonRadius = typedArray.getDimensionPixelSize(
                R.styleable.DirectionView_button_outside_circle_radius, 180);
        edgeRadius = typedArray.getDimensionPixelSize(R.styleable.DirectionView_edge_radius, 200);
        sideWidth = typedArray.getDimensionPixelSize(
                R.styleable.DirectionView_button_side_width, 120);
        indicatorColor = typedArray.getColor(R.styleable.DirectionView_indicator_color, Color.GREEN);
        typedArray.recycle();

    }

    private void initialBitmap(Context context) {
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

        resetDirection();
    }

    private void initialData() {

        int sideLengthOfCenter = (int) Math.sqrt(Math.pow(buttonRadius, 2)
                - Math.pow(sideWidth / 2, 2));
        int sideLength = sideLengthOfCenter - sideWidth / 2;

        centerPoint = centerPoint();

        directPath = new Path();
        directPath.moveTo(-sideLengthOfCenter + centerPoint.x, -sideWidth / 2 + centerPoint.y);
        directPath.rLineTo(sideLength, 0);
        directPath.rLineTo(0, -sideLength);
        directPath.rLineTo(sideWidth, 0);
        directPath.rLineTo(0, sideLength);
        directPath.rLineTo(sideLength, 0);
        directPath.rLineTo(0, sideWidth);
        directPath.rLineTo(-sideLength, 0);
        directPath.rLineTo(0, sideLength);
        directPath.rLineTo(-sideWidth, 0);
        directPath.rLineTo(0, -sideLength);
        directPath.rLineTo(-sideLength, 0);
        directPath.rLineTo(0, -sideWidth);
        directPath.addCircle(centerPoint.x, centerPoint.y, INSIDE_CIRCLE_RADIUS, Path.Direction.CW);

        indicatorRect = new RectF();
        indicatorRect.set(centerPoint.x - INSIDE_CIRCLE_RADIUS, centerPoint.y - INSIDE_CIRCLE_RADIUS,
                centerPoint.x + INSIDE_CIRCLE_RADIUS, centerPoint.y + INSIDE_CIRCLE_RADIUS);

        currentDirection = Direction.NONE;
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
                mViewSize - mPadding * 2 - mArrowSize,
                centerPoint.x + mArrowSize ,
                mViewSize - mPadding * 2 + mArrowSize
        ),null);

        canvas.drawBitmap(mArrowLeft,null, new Rect(
                mPadding * 2 - mArrowSize,
                centerPoint.y  - mArrowSize ,
                mPadding * 2 + mArrowSize,
                centerPoint.y + mArrowSize
        ),null);

        canvas.drawBitmap(mArrowRight,null, new Rect(
                mViewSize - mPadding * 2 - mArrowSize,
                centerPoint.y  - mArrowSize ,
                mViewSize - mPadding * 2 + mArrowSize,
                centerPoint.y + mArrowSize
        ),null);

    }

    protected void drawIndicator(Canvas canvas) {
        paint.reset();
        paint.setColor(indicatorColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(20);
        canvas.drawArc(indicatorRect, startAngle, INDICATOR_SWEEP_ANGLE, false, paint);
    }


    @Override
    protected void actionDown(float x, float y, double angle) {
        pressedStatus = true;
        if (!invalidRegion.contains((int) x, (int) y)) {
            updateIndicator(angle);
        }
    }

    @Override
    protected void actionMove(float x, float y, double angle) {
        if (!invalidRegion.contains((int) x, (int) y)) {
            updateIndicator(angle);
        }
    }

    @Override
    protected void actionUp(float x, float y, double angle) {
        resetIndicator();
        notifyDirection(Direction.NONE);
    }

    private void resetIndicator() {
        pressedStatus = false;
        resetDirection();
        invalidate();
    }

    private void updateIndicator(double angle) {
        resetDirection();
        if (Utils.range(angle, 337.5, 360) || Utils.range(angle, 0, 22.5)) {
            startAngle = 315.0f;
            mArrowRight = mArrowRightPre;
            notifyDirection(Direction.RIGHT);
        } else if (Utils.range(angle, 22.5, 67.5)) {
            startAngle = 0.0f;
            mArrowRight = mArrowRightPre;
            mArrowDown = mArrowDownPre;
            notifyDirection(Direction.DOWN_AND_RIGHT);
        } else if (Utils.range(angle, 67.5, 112.5)) {
            startAngle = 45.0f;
            mArrowDown = mArrowDownPre;
            notifyDirection(Direction.DOWN);
        } else if (Utils.range(angle, 112.5, 157.5)) {
            startAngle = 90.0f;
            mArrowLeft = mArrowLeftPre;
            mArrowDown = mArrowDownPre;
            notifyDirection(Direction.DOWN_AND_LEFT);
        } else if (Utils.range(angle, 157.5, 202.5)) {
            startAngle = 135.0f;
            mArrowLeft = mArrowLeftPre;
            notifyDirection(Direction.LEFT);
        } else if (Utils.range(angle, 202.5, 247.5)) {
            startAngle = 180.0f;
            mArrowUp = mArrowUpPre;
            mArrowLeft = mArrowLeftPre;
            notifyDirection(Direction.UP_AND_LEFT);
        } else if (Utils.range(angle, 247.5, 292.5)) {
            startAngle = 225.0f;
            mArrowUp = mArrowUpPre;
            notifyDirection(Direction.UP);
        } else if (Utils.range(angle, 292.5, 337.5)) {
            startAngle = 270.0f;
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
