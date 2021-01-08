package com.example.madcamp1st.games.breakout;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.madcamp1st.games.breakout.views.Ball;
import com.example.madcamp1st.games.breakout.views.Brick;
import com.example.madcamp1st.games.breakout.views.Paddle;

class BreakoutEngine extends SurfaceView implements Runnable {
    // This is our thread
    private Thread gameThread = null;

    // This is new. We need a SurfaceHolder
    // When we use Paint and Canvas in a thread
    // We will see it in action in the draw method soon.
    private final SurfaceHolder ourHolder;

    // A boolean which we will set and unset
    // when the game is running- or not.
    private volatile boolean playing;

    // Game is paused at the start
    private boolean paused = true;

    private final Paint paint;

    // This is used to help calculate the fps
    private long timeThisFrame = 0;

    // The size of the screen in pixels
    private final int screenX;
    private final int screenY;

    // The player's paddle
    private final Paddle paddle;

    // A ball
    private final Ball ball;

    private final float BALL_CLEAR_OBSTACLE = 2;

    // Up to 200 bricks
    private final Brick[] bricks;
    private int numBricks = 0;
    private final int MAX_BRICK_NUM = 200;

    private final int BRICK_COLUMN = 8;
    private final int BRICK_ROW = 3;
    private final int BRICK_ROW_FULL_SCREEN = 10;

    // The score
    private int score = 0;

    // Lives
    private int lives = 3;

    // When the we initialize (call new()) on gameView
    // This special constructor method runs
    public BreakoutEngine(Context context, Point size) {
        // The next line of code asks the
        // SurfaceView class to set up our object.
        // How kind.
        super(context);

        screenX = size.x;
        screenY = size.y;

        paddle = new Paddle(screenX, screenY);

        // Create a ball
        ball = new Ball(screenX, screenY);

        bricks = new Brick[MAX_BRICK_NUM];

        Brick.width = screenX / BRICK_COLUMN;
        Brick.height = screenY / BRICK_ROW_FULL_SCREEN;
        Brick.padding = 1;

        // Initialize ourHolder and paint objects
        ourHolder = getHolder();
        paint = new Paint();

        createBricksAndRestart();
    }

    public void createBricksAndRestart(){
        // Put the ball back to the start
        ball.reset();
        paddle.reset();

        // Build a wall of bricks
        numBricks = 0;

        for(int column = 0; column < BRICK_COLUMN; column ++ ){
            for(int row = 0; row < BRICK_ROW; row ++ ){
                bricks[numBricks] = new Brick(row, column);
                numBricks++;
            }
        }

        // Reset scores and lives
        score = 0;
        lives = 3;
    }

    @Override
    public void run() {
        while (playing) {
            // Capture the current time in milliseconds in startFrameTime
            long startFrameTime = System.currentTimeMillis();

            // Update the frame
            // Update the frame
            if(!paused){
                update();
            }

            // Draw the frame
            draw();

            // Calculate the fps this frame
            // We can then use the result to
            // time animations and more.
            timeThisFrame = System.currentTimeMillis() - startFrameTime;
        }
    }

    // Everything that needs to be updated goes in here
    // Movement, collision detection etc.
    public void update() {
        paddle.update(timeThisFrame);
        ball.update(timeThisFrame);

        // Check for ball colliding with a brick
        for(int i = 0; i < numBricks; i++){
            if (bricks[i].getVisibility()){
                RectF brickRectF = bricks[i].getRect();
                RectF ballRectF = ball.getRect();

                if(RectF.intersects(brickRectF,ballRectF)) {
                    bricks[i].setInvisible();
                    score++;
                    float xVelocity = ball.getXVelocity();
                    float yVelocity = ball.getYVelocity();

                    if(xVelocity > 0){
                        float leftDiff = ballRectF.right - brickRectF.left;
                        float leftTime = leftDiff / xVelocity;

                        if(yVelocity > 0){
                            float topDiff = ballRectF.bottom - brickRectF.top;
                            float topTime = topDiff / yVelocity;

                            if(leftTime < topTime)
                                ball.reverseXVelocity();
                            else
                                ball.reverseYVelocity();
                        } else if(yVelocity < 0){
                            float bottomDiff = brickRectF.bottom - ballRectF.top;
                            float bottomTime = bottomDiff / -yVelocity;

                            if(leftTime < bottomTime)
                                ball.reverseXVelocity();
                            else
                                ball.reverseYVelocity();
                        } else
                            ball.reverseXVelocity();
                    } else if(xVelocity < 0){
                        float rightDiff = brickRectF.right - ballRectF.left;
                        float rightTime = rightDiff / -xVelocity;

                        if(yVelocity > 0){
                            float topDiff = ballRectF.bottom - brickRectF.top;
                            float topTime = topDiff / yVelocity;

                            if(rightTime < topTime)
                                ball.reverseXVelocity();
                            else
                                ball.reverseYVelocity();
                        } else if(yVelocity < 0){
                            float bottomDiff = brickRectF.bottom - ballRectF.top;
                            float bottomTime = bottomDiff / -yVelocity;

                            if(rightTime < bottomTime)
                                ball.reverseXVelocity();
                            else
                                ball.reverseYVelocity();
                        } else
                            ball.reverseXVelocity();
                    } else
                        ball.reverseYVelocity();
                }
            }
        }

        // Check for ball colliding with paddle
        if(RectF.intersects(paddle.getRect(),ball.getRect())) {
            float ballAngle = paddle.getBallAngle(ball.getRect().left, ball.BALL_WIDTH);
            ball.setAngle(ballAngle);
            ball.clearObstacleY(paddle.getRect().top - ball.BALL_HEIGHT - BALL_CLEAR_OBSTACLE);
        }

        // Bounce the ball back when it hits the bottom of screen
        // And deduct a life
        if(ball.getRect().bottom > screenY){
            ball.reset();
            paddle.reset();
            paused = true;

            // Lose a life
            lives --;

            if(lives == 0)
                createBricksAndRestart();
        }

        // Bounce the ball back when it hits the top of screen
        if(ball.getRect().top < 0){
            ball.reverseYVelocity();
            ball.clearObstacleY(BALL_CLEAR_OBSTACLE);
        }

        // If the ball hits left wall bounce
        if(ball.getRect().left < 0){
            ball.reverseXVelocity();
            ball.clearObstacleX(BALL_CLEAR_OBSTACLE);
        }

        // If the ball hits right wall bounce
        if(ball.getRect().right > screenX){
            ball.reverseXVelocity();
            ball.clearObstacleX(screenX - ball.BALL_WIDTH - BALL_CLEAR_OBSTACLE);
        }

        // Pause if cleared screen
        if(score == numBricks){
            paused = true;
            createBricksAndRestart();
        }
    }

    // Draw the newly updated scene
    public void draw() {
        // Make sure our drawing surface is valid or we crash
        if (ourHolder.getSurface().isValid()) {
            // Lock the canvas ready to draw
            // A Canvas and a Paint object
            Canvas canvas = ourHolder.lockCanvas();

            // Draw the background color
            canvas.drawColor(Color.argb(255,  26, 128, 182));

            // Choose the brush color for drawing
            paint.setColor(Color.argb(255,  255, 255, 255));

            // Draw the paddle
            canvas.drawRect(paddle.getRect(), paint);

            // Draw the ball
            canvas.drawRect(ball.getRect(), paint);

            // Change the brush color for drawing
            paint.setColor(Color.argb(255,  249, 129, 0));

            // Draw the bricks if visible
            for(int i = 0; i < numBricks; i++){
                if(bricks[i].getVisibility()) {
                    canvas.drawRect(bricks[i].getRect(), paint);
                }
            }

            // Draw the HUD
            // Choose the brush color for drawing
            paint.setColor(Color.argb(255,  255, 255, 255));

            // Draw the score
            paint.setTextSize(40);
            canvas.drawText("Score: " + score + "   Lives: " + lives, 10,50, paint);

            // Draw everything to the screen
            ourHolder.unlockCanvasAndPost(canvas);
        }
    }

    // If SimpleGameEngine Activity is paused/stopped
    // shutdown our thread.
    public void pause() {
        playing = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
        }
    }

    // If SimpleGameEngine Activity is started theb
    // start our thread.
    public void resume() {
        playing = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    // The SurfaceView class implements onTouchListener
    // So we can override this method and detect screen touches.
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        switch (motionEvent.getActionMasked()) {
            // Player has touched the screen
            case MotionEvent.ACTION_DOWN:
                paused = false;

                if(motionEvent.getX() > (float)screenX / 2)
                    paddle.setMovementState(Paddle.Moving.RIGHT);
                else
                    paddle.setMovementState(Paddle.Moving.LEFT);

                break;

            // Player has removed finger from screen
            case MotionEvent.ACTION_UP:
                paddle.setMovementState(Paddle.Moving.STOPPED);
                break;
        }

        return true;
    }
}