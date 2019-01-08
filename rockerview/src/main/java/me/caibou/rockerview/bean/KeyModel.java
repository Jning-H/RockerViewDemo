package me.caibou.rockerview.bean;

public class KeyModel {
    private int mWidth;
    private int mHeight;
    private int mPadding;
    private int mCenterX;
    private int mCenterY;
    private boolean isShowDirection;

    public KeyModel(int width, int height, int padding, int centerX, int centerY,boolean showDirection) {
        this.mWidth = width;
        this.mHeight = height;
        this.mPadding = padding;
        this.mCenterX = centerX;
        this.mCenterY = centerY;
        this.isShowDirection = showDirection;
    }

    public int getWidth() {
        return mWidth;
    }

    public void setWidth(int width) {
        this.mWidth = width;
    }

    public int getHeight() {
        return mHeight;
    }

    public void setHeight(int height) {
        mHeight = height;
    }

    public int getPadding() {
        return mPadding;
    }

    public void setPadding(int padding) {
        this.mPadding = padding;
    }

    public int getCenterX() {
        return mCenterX;
    }

    public void setCenterX(int centerX) {
        this.mCenterX = centerX;
    }

    public int getCenterY() {
        return mCenterY;
    }

    public void setCenterY(int centerY) {
        this.mCenterY = centerY;
    }

    public boolean isShowDirection() {
        return isShowDirection;
    }

    public void setShowDirection(boolean showDirection) {
        isShowDirection = showDirection;
    }
}
