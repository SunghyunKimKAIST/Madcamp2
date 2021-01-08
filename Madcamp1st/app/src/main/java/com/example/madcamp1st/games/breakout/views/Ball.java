package com.example.madcamp1st.games.breakout.views;

import android.graphics.RectF;

public class Ball {
    private final RectF rect;
    private double angle;

    private final float VELOCITY = 0.5f;
    private final double DEFAULT_ANGLE;

    public final float BALL_WIDTH = 10;
    public final float BALL_HEIGHT = 10;

    private final float DEFAULT_X;
    private final float DEFAULT_Y;


    public Ball(int screenX, int screenY){
        DEFAULT_X = (screenX - BALL_WIDTH) / 2;
        DEFAULT_Y = screenY - BALL_HEIGHT - Paddle.PADDLE_HEIGHT;

        DEFAULT_ANGLE = Math.PI / 4;

        // Place the ball in the centre of the screen at the bottom
        // Make it a 10 pixel x 10 pixel square
        rect = new RectF();
    }

    public RectF getRect(){
        return rect;
    }

    public float getXVelocity(){
        return (float)(Math.cos(angle) * VELOCITY);
    }

    public float getYVelocity(){
        return -(float)(Math.sin(angle) * VELOCITY);
    }

    public void update(long frameTime){
        rect.left += Math.cos(angle) * VELOCITY * frameTime;
        rect.top -= Math.sin(angle) * VELOCITY * frameTime;
        rect.right = rect.left + BALL_WIDTH;
        rect.bottom = rect.top + BALL_HEIGHT;
    }

    public void reverseYVelocity(){
        angle = -angle;
    }

    public void reverseXVelocity(){
        angle = -angle + (angle > 0 ? Math.PI : -Math.PI);
    }

    public void setAngle(double angle){
        this.angle = angle;
    }

    public void clearObstacleY(float y){
        rect.top = y;
        rect.bottom = y + BALL_HEIGHT;
    }

    public void clearObstacleX(float x){
        rect.left = x;
        rect.right = x + BALL_WIDTH;
    }

    public void reset(){
        rect.left = DEFAULT_X;
        rect.top = DEFAULT_Y;
        rect.right = DEFAULT_X + BALL_WIDTH;
        rect.bottom = DEFAULT_Y + BALL_HEIGHT;

        angle = DEFAULT_ANGLE;
    }
}