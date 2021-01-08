package com.example.madcamp1st.games.breakout.views;

import android.graphics.RectF;

public class Brick {
    private final RectF rect;

    private boolean isVisible;

    public static int width;
    public static int height;
    public static int padding;

    public Brick(int row, int column){

        isVisible = true;

        rect = new RectF(column * width + padding,
                row * height + padding,
                column * width + width - padding,
                row * height + height - padding);
    }

    public RectF getRect(){
        return this.rect;
    }

    public void setInvisible(){
        isVisible = false;
    }

    public boolean getVisibility(){
        return isVisible;
    }
}