package com.example.madcamp1st.games.breakout.views;

import android.graphics.RectF;

public class Paddle {

    // RectF is an object that holds four coordinates - just what we need
    private final RectF rect;

    // How long and high our paddle will be
    public static final float PADDLE_LENGTH = 130;
    public static final float PADDLE_HEIGHT = 20;

    private final float SCREEN_X;
    private final float SCREEN_Y;

    // This will hold the pixels per millisecond speed that the paddle will move
    private final float PADDLE_SPEED = 0.5f;

    // Which ways can the paddle move
    public enum Moving {STOPPED, LEFT, RIGHT}

    // Is the paddle moving and in which direction
    private Moving paddleMoving = Moving.STOPPED;

    // This the the constructor method
    // When we create an object from this class we will pass
    // in the screen width and height
    public Paddle(int screenX, int screenY){
        SCREEN_X = screenX;
        SCREEN_Y = screenY;

        rect = new RectF();
    }

    // This is a getter method to make the rectangle that
    // defines our paddle available in BreakoutView class
    public RectF getRect(){
        return rect;
    }

    public float getBallAngle(float ballLeft, float ballWidth) {
        return (rect.right - ballLeft) / (PADDLE_LENGTH + ballWidth) * (float)Math.PI;
    }

    // This method will be used to change/set if the paddle is going left, right or nowhere
    public void setMovementState(Moving state){
        paddleMoving = state;
    }

    // This update method will be called from update in BreakoutView
    // It determines if the paddle needs to move and changes the coordinates
    // contained in rect if necessary
    public void update(long frameTime){
        if(paddleMoving == Moving.LEFT)
            rect.left -= PADDLE_SPEED * frameTime;
        else if(paddleMoving == Moving.RIGHT)
            rect.left += PADDLE_SPEED * frameTime;

        rect.right = rect.left + PADDLE_LENGTH;
    }

    public void reset(){
        rect.left = (SCREEN_X - PADDLE_LENGTH) / 2;
        rect.top = SCREEN_Y - PADDLE_HEIGHT;
        rect.right = rect.left + PADDLE_LENGTH;
        rect.bottom = SCREEN_Y;
    }
}