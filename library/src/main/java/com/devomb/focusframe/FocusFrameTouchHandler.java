package com.devomb.focusframe;

import android.graphics.RectF;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.os.Handler;

/**
 * Created by Ombrax on 20/06/2015.
 */
public class FocusFrameTouchHandler {

    //region declaration

    //region constant
    private static final int DEFAULT_TOUCH_DELAY = 300;
    private final Runnable delayedExecution = new Runnable() {
        @Override
        public void run() {
            if (touchDown) {
                touchDownMinReached = true;
                onTouchInsideListener.onTouchDown();
            }
        }
    };
    //endregion

    //region variable
    private static FocusFrame focusFrame;
    private OnTouchInsideListener onTouchInsideListener;
    private float borderOffset;
    private int touchDelay;
    //endregion

    //region inner field
    private RectF box;
    private float originalBoxWidth, originalBoxHeight;
    private GestureDetector gestureDetector;
    private float xOrigin = 0, yOrigin = 0;
    private Handler handler;
    private boolean touchInside;
    private boolean touchDown;
    private boolean touchDownMinReached;
    //endregion

    //endregion

    //region constructor
    public FocusFrameTouchHandler(FocusFrame focusFrame) {
        this.focusFrame = focusFrame;
        handler = new Handler();
        touchDelay = DEFAULT_TOUCH_DELAY;
        box = focusFrame.getBox();
        originalBoxWidth = box.width();
        originalBoxHeight = box.height();
        gestureDetector = new GestureDetector(focusFrame.getContext(), new DoubleTapTouchListener());
    }
    //endregion

    //region setter
    public void setBorderOffset(float borderOffset) {
        this.borderOffset = borderOffset;
    }

    public void setOnTouchInsideListener(OnTouchInsideListener onTouchInsideListener) {
        this.onTouchInsideListener = onTouchInsideListener;
    }

    public void setTouchDelay(int touchDelay) {
        this.touchDelay = touchDelay;
    }
    //endregion

    //region touch
    public boolean onTouchEvent(MotionEvent event) {
        if (touchInside = box.contains(event.getX(), event.getY())) {
            gestureDetector.onTouchEvent(event);
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!touchInside) {
                    return true;
                }
                focusFrame.applyColorChange(true);
                xOrigin = event.getX();
                yOrigin = event.getY();
                touchDown = true;
                handler.postDelayed(delayedExecution, touchDelay);
                return true;
            case MotionEvent.ACTION_MOVE:
                if (!touchInside) {
                    break;
                }
                float xDestination = event.getX();
                float yDestination = event.getY();

                float deltaX = xDestination - xOrigin;
                float deltaY = yDestination - yOrigin;

                xOrigin = xDestination;
                yOrigin = yDestination;

                move(deltaX, deltaY);
                onTouchInsideListener.onMove();
                break;
            case MotionEvent.ACTION_UP:
                focusFrame.applyColorChange(false);
                touchDown = false;
                handler.removeCallbacks(delayedExecution);
                if (touchDownMinReached) {
                    onTouchInsideListener.onTouchUp();
                    touchDownMinReached = false;
                }
                break;
        }
        return false;
    }
    //endregion

    //region helper
    private void move(float xDistance, float yDistance) {
        box.offset(xDistance, yDistance);

        if (box.left <= borderOffset) {
            box.left = borderOffset;
            box.right = originalBoxWidth + borderOffset;
        } else if (box.right >= focusFrame.getWidth() - borderOffset) {
            box.right = focusFrame.getWidth() - borderOffset;
            box.left = focusFrame.getWidth() - originalBoxWidth - borderOffset;
        }

        if (box.top <= borderOffset) {
            box.top = borderOffset;
            box.bottom = originalBoxHeight - borderOffset;
        } else if (box.bottom >= focusFrame.getHeight() - borderOffset) {
            box.bottom = focusFrame.getHeight() - borderOffset;
            box.top = focusFrame.getHeight() - originalBoxHeight - borderOffset;
        }

        focusFrame.invalidate();
    }
    //endregion

    //region inner class
    private class DoubleTapTouchListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            onTouchInsideListener.onDoubleTap();
            return true;
        }
    }

    public interface OnTouchInsideListener {
        void onTouchDown();

        void onMove();

        void onTouchUp();

        void onDoubleTap();
    }

    public static class DefaultOnTouchInsideListener implements OnTouchInsideListener {

        @Override
        public void onTouchDown() {
            //Do Nothing
        }

        @Override
        public void onMove() {
            //Do Nothing
        }

        @Override
        public void onTouchUp() {
            //Do Nothing
        }

        @Override
        public void onDoubleTap() {
            //Restore original position
            focusFrame.positionBox();
            focusFrame.invalidate();
        }
    }
    //endregion

}
