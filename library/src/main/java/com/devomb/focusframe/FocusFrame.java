package com.devomb.focusframe;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Ombrax on 20/06/2015.
 */
public class FocusFrame extends View {

    //region declaration

    //region constant
    private static final int DEFAULT_HEIGHT_DIMENSION_DIP = 100;
    private static final int DEFAULT_WIDTH_DIMENSION_DIP = 125;
    //endregion

    //region attribute
    private float boxHeight;
    private float boxWidth;
    private BoxPosition boxPosition;
    private BoxShape boxShape;
    private boolean movable;
    private float borderWidth;
    private int borderColor;
    private int movingBorderColor;
    //endregion

    //region inner field
    private RectF box;
    private Paint backgroundPaint;
    private Paint boxBorderPaint;
    private FocusFrameTouchHandler touchHandler;
    //endregion

    //endregion

    //region constructor
    public FocusFrame(Context context) {
        super(context);
        init(null, 0);
    }

    public FocusFrame(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public FocusFrame(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }
    //endregion

    //region getter setter

    //region getter
    public RectF getBox(){
        return box;
    }

    public float getBoxWidth() {
        return boxWidth;
    }

    public float getBoxHeight() {
        return boxHeight;
    }

    public boolean isMovable() {
        return movable;
    }

    protected float getBorderWidth() {
        return borderWidth;
    }
    //endregion

    //region setter
    public void setBoxWidth(float boxWidth) {
        if (boxWidth > 0) {
            this.boxWidth = boxWidth;
            box.right = box.left + boxWidth;
        }
    }

    public void setBoxHeight(float boxHeight) {
        if (boxHeight > 0) {
            this.boxHeight = boxHeight;
            box.bottom = box.top + boxHeight;
        }
    }

    public void setBoxPosition(BoxPosition boxPosition) {
        this.boxPosition = boxPosition;
        positionBox();
    }

    public void setBoxShape(BoxShape boxShape) {
        this.boxShape = boxShape;
        newBox();
    }

    public void setMovable(boolean movable) {
        this.movable = movable;
    }

    public void setBorderWidth(float borderWidth) {
        this.borderWidth = borderWidth;
        boxBorderPaint.setStrokeWidth(borderWidth);
        touchHandler.setBorderOffset(Math.round(borderWidth / 2));
    }

    public void setBorderColor(int borderColor) {
        this.borderColor = borderColor;
        boxBorderPaint.setColor(borderColor);
    }

    public void setOnTouchInsideListener(FocusFrameTouchHandler.OnTouchInsideListener onTouchInsideListener) {
        touchHandler.setOnTouchInsideListener(onTouchInsideListener);
    }

    public void setTouchListenerDelay(int delay){
        touchHandler.setTouchDelay(delay);
    }
    //endregion

    //endregion

    //region method
    public void offsetPosition(float deltaX, float deltaY){
        box.offset(deltaX, deltaY);
        invalidate();
    }

    public void offsetPositionTo(float newX, float newY){
        box.offsetTo(newX, newY);
        invalidate();
    }

    protected void applyColorChange(boolean isMoving){
        boxBorderPaint.setColor(isMoving ? movingBorderColor : borderColor);
        invalidate();
    }
    //endregion

    //region setup
    private void init(AttributeSet attrs, int defStyleAttr) {
        getAttributes(attrs, defStyleAttr);
        newPaint();
        newBox();
        positionBox();
        touchHandler = new FocusFrameTouchHandler(this);
        touchHandler.setOnTouchInsideListener(new FocusFrameTouchHandler.DefaultOnTouchInsideListener());
        touchHandler.setBorderOffset(Math.round(borderWidth / 2));
    }

    private void getAttributes(AttributeSet set, int defStyleAttr) {
        TypedArray attributes = getContext().obtainStyledAttributes(set, R.styleable.FocusFrame, defStyleAttr, 0);

        boxHeight = attributes.getDimension(R.styleable.FocusFrame_focusBox_height, pixelDimension(DEFAULT_HEIGHT_DIMENSION_DIP));
        boxWidth = attributes.getDimension(R.styleable.FocusFrame_focusBox_width, pixelDimension(DEFAULT_WIDTH_DIMENSION_DIP));
        boxPosition = BoxPosition.values()[attributes.getInt(R.styleable.FocusFrame_focusBox_position, BoxPosition.CENTER_CENTER.ordinal())];
        boxShape = BoxShape.values()[attributes.getInt(R.styleable.FocusFrame_focusBox_shape, BoxShape.RECTANGLE.ordinal())];
        movable = attributes.getBoolean(R.styleable.FocusFrame_movable, false);
        borderWidth = attributes.getDimension(R.styleable.FocusFrame_focusBox_borderWidth, 0);
        borderColor = attributes.getColor(R.styleable.FocusFrame_focusBox_borderColor, Color.WHITE);
        movingBorderColor = attributes.getColor(R.styleable.FocusFrame_focusBox_movingBorderColor, Color.WHITE);

        attributes.recycle();
    }


    private void newPaint() {
        backgroundPaint = new Paint();
        backgroundPaint.setColor(getContext().getResources().getColor(R.color.dark_overlay));
        backgroundPaint.setAntiAlias(true);

        boxBorderPaint = new Paint();
        boxBorderPaint.setColor(borderColor);
        boxBorderPaint.setStrokeWidth(borderWidth);
        boxBorderPaint.setStyle(Paint.Style.STROKE);
        boxBorderPaint.setAntiAlias(true);
    }

    private void newBox() {
        adjustDimensionsToShape();
        box = new RectF(0, 0, boxWidth, boxHeight);
    }
    //endregion

    //region helper
    private void adjustDimensionsToShape() {
        switch (boxShape) {
            case SQUARE:
            case CIRCLE:
                float newDimension = Math.min(boxHeight, boxWidth);
                boxHeight = newDimension;
                boxWidth = newDimension;
                break;
        }
    }

    protected void positionBox() {
        float x = 0, y = 0;
        float borderOffset = Math.round(borderWidth / 2);

        //Get x
        switch (boxPosition) {
            case TOP_LEFT:
            case CENTER_LEFT:
            case BOTTOM_LEFT:
                x = borderOffset;
                break;
            case TOP_CENTER:
            case CENTER_CENTER:
            case BOTTOM_CENTER:
                x = (getMeasuredWidth() / 2) - (boxWidth / 2);
                break;
            case TOP_RIGHT:
            case CENTER_RIGHT:
            case BOTTOM_RIGHT:
                x = getMeasuredWidth() - boxWidth - borderOffset;
                break;
        }

        //Get y
        switch (boxPosition) {
            case TOP_LEFT:
            case TOP_CENTER:
            case TOP_RIGHT:
                y = borderOffset;
                break;
            case CENTER_LEFT:
            case CENTER_CENTER:
            case CENTER_RIGHT:
                y = (getMeasuredHeight() / 2) - (boxHeight / 2);
                break;
            case BOTTOM_LEFT:
            case BOTTOM_CENTER:
            case BOTTOM_RIGHT:
                y = getMeasuredHeight() - boxHeight - borderOffset;
                break;
        }

        box.offsetTo(x, y);
    }

    private float pixelDimension(float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getContext().getResources().getDisplayMetrics());
    }
    //endregion

    //region override

    //region draw
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBackground(canvas);
        if (borderWidth != 0) {
            drawFrame(canvas);
        }
    }

    private void drawFrame(Canvas canvas) {
        switch (boxShape) {
            case RECTANGLE:
            case SQUARE:
                canvas.drawRect(box.left, box.top, box.right, box.bottom, boxBorderPaint);
                break;
            case CIRCLE:
                canvas.drawCircle(box.centerX(), box.centerY(), boxHeight / 2, boxBorderPaint);
                break;
        }
    }

    private void drawBackground(Canvas canvas) {


        switch (boxShape) {
            case RECTANGLE:
            case SQUARE:
                //Draw left bar
                canvas.drawRect(getX(), box.top, box.left, box.bottom, backgroundPaint);
                //Draw top bar
                canvas.drawRect(getX(), getY(), getWidth(), box.top, backgroundPaint);
                //Draw right bar
                canvas.drawRect(box.right, box.top, getWidth(), box.bottom, backgroundPaint);
                //Draw bottom bar
                canvas.drawRect(getX(), box.bottom, getWidth(), getHeight(), backgroundPaint);
                break;
            case CIRCLE:
                Path path = new Path();
                path.moveTo(0, 0);
                path.addRect(new RectF(0, 0, getWidth(), getHeight()), Path.Direction.CCW);
                path.addCircle(box.centerX(), box.centerY(), boxHeight / 2, Path.Direction.CW);
                path.setFillType(Path.FillType.EVEN_ODD);
                canvas.drawPath(path, backgroundPaint);
                break;
        }
    }
    //endregion

    //region measure
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        positionBox();
    }
    //endregion

    //region touch
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!movable) {
            return super.onTouchEvent(event);
        }
        return touchHandler.onTouchEvent(event);
    }
    //endregion

    //endregion

    //region inner class

    public enum BoxPosition {
        TOP_LEFT,
        TOP_CENTER,
        TOP_RIGHT,
        CENTER_LEFT,
        CENTER_CENTER,
        CENTER_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_CENTER,
        BOTTOM_RIGHT
    }

    public enum BoxShape {
        RECTANGLE,
        SQUARE,
        CIRCLE
    }
    //endregion
}
