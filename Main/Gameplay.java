package Main;
import Render.Screen;
import GameObjects.Paddle;
import GameObjects.Ball;
import GameObjects.BrickLines;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.Timer;

/**
 * The Gameplay class implements the core logic of the game.
 * It handles the game loop, listens for player input, and manages interactions
 * between the ball, paddle, and bricks, including collision detection.
 */
public class Gameplay implements KeyListener, ActionListener{
    /** The x-coordinate where the ball will bounce off the right edge of the screen. */
    private final int BALL_SCREEN_COLLISION_X = Screen.WINDOW_WIDTH - Ball.getWidth();
    /** The leftmost limit for the paddle's movement. */
    private final int PADDLE_SCREEN_LEFT_LIMIT = 0;
    /** The rightmost limit for the paddle's movement. */
    private final int PADDLE_SCREEN_RIGHT_LIMIT = Screen.WINDOW_WIDTH - Paddle.getWidth();
    /** The y-coordinate at which the ball is considered missed, resulting in a loss of life. */
    private static final int BALL_MISS_FORGIVENESS_PX = 16;
    private final int MISS_HEIGHT = Screen.WINDOW_HEIGHT + BALL_MISS_FORGIVENESS_PX;
    /** determines the delay of the game's timer. A smaller number results in a faster the movement of objects. */
    private static final int TIMER_DELAY_MS = 10;
    /** Maximum paddle bounce angle from vertical, reached at paddle edges. */
    private static final double PADDLE_MAX_BOUNCE_ANGLE_DEG = 67.0;
    /** Minimum angle from vertical for any non-center paddle hit. */
    private static final double PADDLE_MIN_OFFCENTER_ANGLE_DEG = 8.0;
    /** Keeps the game responsive if speed ever drops too much. */
    private static final double MIN_BALL_SPEED = 6.0;
    /** Any non-center paddle hit should keep at least this horizontal return speed. */
    private static final double MIN_PADDLE_RETURN_X_VELOCITY = 1.8;
    /** Prevent top paddle hits from becoming almost horizontal. */
    private static final double MIN_PADDLE_TOP_Y_VELOCITY = 2.5;
    /** Minimum horizontal deflection for paddle side collisions. */
    private static final double MIN_PADDLE_SIDE_X_VELOCITY = 2.0;
    /** Small separation gap to keep ball and paddle from re-overlapping on side hits. */
    private static final int PADDLE_ESCAPE_GAP = 2;
    /** Prevents top-corner wall hits from collapsing into a straight vertical drop. */
    private static final double MIN_CORNER_X_VELOCITY = 2.0;
    private static final double EPSILON = 1e-9;
    private GameEndListener gameEndListener;
    private Player player;
    private Screen screen;
    private SoundEffect soundEffect;
    private Paddle paddle;
    private Ball ball;
    private BrickLines lineOfBricks;
    /** The main game timer that triggers an action event at a regular interval to drive the game's state. */
    private Timer timer;
    private Rectangle paddleBounds;
    /** Tracks if the paddle should be moving left. */
    private boolean movingLeft = false;
    /** Tracks if the paddle should be moving right. */
    private boolean movingRight = false;
    private boolean ballDefaultPosition = true;
    private boolean spacePressed = false;

    /**
     * Constructs the Gameplay object.
     * @param player The player object, containing score and life data.
     * @param screen The screen where the game is rendered.
     * @param soundEffect The object for playing sound effects.
     * @param ball The ball object.
     * @param paddle The paddle object.
     * @param brickArrayList The list of bricks in the level.
     */
    public Gameplay(Player player, Screen screen, SoundEffect soundEffect, Ball ball, Paddle paddle, BrickLines lineOfBricks){
        screen.addKeyListener(this);
        this.player = player;
        this.screen = screen;
        this.soundEffect = soundEffect;
        this.ball = ball;
        this.paddle = paddle;
        this.lineOfBricks = lineOfBricks;
        paddleBounds = new Rectangle();
    }
    /**
     * Sets a listener that will be notified when the game ends (either by winning or losing).
     * @param listener The listener to be notified.
     */
    public void setGameEndListener(GameEndListener listener) {
        this.gameEndListener = listener;
    }

    /**
     * Starts the game loop by initializing and starting the timer.
     * The timer's delay is determined by the ball's speed property.
     */
    public void run(){
        timer = new Timer(TIMER_DELAY_MS, this);
        timer.start();
    }
    /**
     * This method is called by the Timer at each interval. It serves as the main game loop,
     * updating the game state, checking for collisions, and determining if the game is over.
     * @param e The ActionEvent triggered by the timer.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if(!isGameOver()){
            updatePaddlePosition(); // Update paddle position every frame for smooth movement.
            ballReset();

            ballMovement();

            // Check miss only after collision resolution, so last-moment paddle saves count.
            if(isBallMissed()){
                ball.resetPosition();
                ballDefaultPosition = true;
                screen.removeHeartLabel(player.getLifePoints() - 1);
                player.loseLifePoint();
                screen.ballLabel.setLocation(ball.getX(), ball.getY());
            }
        }
        else{
            // Stop the game and notify the listener that the game has ended.
            timer.stop();
            if (gameEndListener != null) {
                gameEndListener.onGameEnd();
            }
        }
    }
    /** Reverses the ball's horizontal velocity to simulate a bounce. */
    private void ballBounceX(){
        ball.setBallXVelocity(ball.getBallXVelocity() * (-1)) ;
    }

    /** Reverses the ball's vertical velocity to simulate a bounce. */
    private void ballBounceY(){
        ball.setBallYVelocity(ball.getBallYVelocity() * (-1));
    }

    /**
     * Manages the ball's movement and checks for collisions with the paddle, bricks, and screen boundaries.
     */
    private void ballMovement(){
        double velocityX = ball.getBallXVelocity();
        double velocityY = ball.getBallYVelocity();
        int steps = Math.max(1, (int)Math.ceil(Math.max(Math.abs(velocityX), Math.abs(velocityY))));
        double stepX = velocityX / (double) steps;
        double stepY = velocityY / (double) steps;
        double nextX = ball.getPreciseX();
        double nextY = ball.getPreciseY();
        boolean playCollisionSound = false;
        boolean playBrickCollisionSound = false;
        paddleBounds.setBounds(paddle.getX(), paddle.getY(), Paddle.getWidth(), Paddle.getHeight());

        for(int i = 0; i < steps; i++){
            Rectangle previousBallBounds = new Rectangle(ball.getX(), ball.getY(), Ball.getWidth(), Ball.getHeight());
            nextX += stepX;
            nextY += stepY;
            ball.setPrecisePosition(nextX, nextY);

            boolean stepHadCollision = false;
            if(screenWallCollision()){
                playCollisionSound = true;
                stepHadCollision = true;
            }

            if(paddleCollision(paddleBounds, previousBallBounds)){
                playCollisionSound = true;
                stepHadCollision = true;
            }
            else if(isBrickCollision(previousBallBounds)){
                player.addScore();
                screen.refreshPlayerScore(player.getScore());
                playBrickCollisionSound = true;
                stepHadCollision = true;
            }

            // Keep sub-pixel movement unless collision explicitly corrected position.
            if(stepHadCollision){
                nextX = ball.getPreciseX();
                nextY = ball.getPreciseY();
            }
            stepX = ball.getBallXVelocity() / (double) steps;
            stepY = ball.getBallYVelocity() / (double) steps;
        }

        // Play at most one collision SFX per frame to avoid audio spam stalls on the EDT.
        if(playBrickCollisionSound){
            soundEffect.playBrickCollisionSoundEffect();
        }
        else if(playCollisionSound){
            soundEffect.playCollisionSoundEffect();
        }

        screen.ballLabel.setLocation(ball.getX(), ball.getY());
    }
    /**
     * Checks for and handles collision between the ball and the paddle.
     * @param paddleBounds The bounding rectangle of the paddle.
     * @return true if a collision occurred, false otherwise.
     */
    private boolean paddleCollision(Rectangle paddleBounds, Rectangle previousBallBounds){
            if(!isCircleIntersectsRect(paddleBounds) || ball.getBallYVelocity() <= 0){
                return false;
            }

            Rectangle currentBallBounds = new Rectangle(ball.getX(), ball.getY(), Ball.getWidth(), Ball.getHeight());
            boolean crossedPaddleTop = previousBallBounds.y + previousBallBounds.height <= paddleBounds.y
                    && currentBallBounds.y + currentBallBounds.height >= paddleBounds.y;
            boolean edgeTopContact = currentBallBounds.getCenterY() <= paddleBounds.getCenterY();

            if(crossedPaddleTop || edgeTopContact){
                ball.setPrecisePosition(ball.getPreciseX(), paddle.getY() - Ball.getHeight());
                return applyPaddleBounceByHitPosition();
            }

            // Side contact fallback (for example, paddle moved into the ball):
            // force an escape path so a moving paddle cannot trap the ball.
            double ballCenterX = currentBallBounds.getCenterX();
            double paddleCenterX = paddleBounds.getCenterX();
            boolean hitLeftSide = ballCenterX < paddleCenterX;
            return applyPaddleSideBounce(hitLeftSide);
    }

    private boolean isBallMissed(){
            paddleBounds.setBounds(paddle.getX(), paddle.getY(), Paddle.getWidth(), Paddle.getHeight());
            return ball.getPreciseY() > MISS_HEIGHT && !isCircleIntersectsRect(paddleBounds);
    }

    private boolean applyPaddleBounceByHitPosition(){
            double ballCenterX = ball.getX() + (Ball.getWidth() / 2.0);
            double paddleCenterX = paddle.getX() + (Paddle.getWidth() / 2.0);
            double centerDelta = ballCenterX - paddleCenterX;
            double hitRatio = clamp(centerDelta / (Paddle.getWidth() / 2.0), -1.0, 1.0);

            double currentSpeed = Math.hypot(ball.getBallXVelocity(), ball.getBallYVelocity());
            double speed = Math.max(currentSpeed, MIN_BALL_SPEED);
            double nextXVelocity;
            double nextYVelocity;

            // Only exact center (sub-pixel) returns straight.
            if(Math.abs(centerDelta) < EPSILON){
                nextXVelocity = 0.0;
                nextYVelocity = -speed;
            }
            else{
                double absHit = Math.abs(hitRatio);
                double bounceAngleDeg = PADDLE_MIN_OFFCENTER_ANGLE_DEG
                        + (PADDLE_MAX_BOUNCE_ANGLE_DEG - PADDLE_MIN_OFFCENTER_ANGLE_DEG) * absHit;
                double bounceAngleRad = Math.toRadians(bounceAngleDeg);
                double xMagnitude = Math.max(MIN_PADDLE_RETURN_X_VELOCITY, Math.sin(bounceAngleRad) * speed);
                double yMagnitude = Math.cos(bounceAngleRad) * speed;
                if(yMagnitude < MIN_PADDLE_TOP_Y_VELOCITY){
                    yMagnitude = Math.min(speed, MIN_PADDLE_TOP_Y_VELOCITY);
                    xMagnitude = Math.sqrt(Math.max(EPSILON, (speed * speed) - (yMagnitude * yMagnitude)));
                }
                nextXVelocity = (hitRatio < 0) ? -xMagnitude : xMagnitude;
                nextYVelocity = -Math.abs(yMagnitude);
            }
            ball.setBallXVelocity(nextXVelocity);
            ball.setBallYVelocity(nextYVelocity);
            return true;
    }

    private boolean applyPaddleSideBounce(boolean hitLeftSide){
            double sideDirection = hitLeftSide ? -1.0 : 1.0;
            double reversedX = -ball.getBallXVelocity();
            double xMagnitude = Math.max(Math.abs(reversedX), MIN_PADDLE_SIDE_X_VELOCITY + paddle.getSpeed() * 0.35);
            double nextXVelocity = sideDirection * xMagnitude;
            double nextYVelocity = Math.max(Math.abs(ball.getBallYVelocity()), MIN_BALL_SPEED * 0.75);

            int escapedX = hitLeftSide
                    ? paddle.getX() - Ball.getWidth() - PADDLE_ESCAPE_GAP
                    : paddle.getX() + Paddle.getWidth() + PADDLE_ESCAPE_GAP;
            int escapedY = ball.getY();
            int paddleBottom = paddle.getY() + Paddle.getHeight();
            if(escapedY < paddleBottom + PADDLE_ESCAPE_GAP){
                escapedY = paddleBottom + PADDLE_ESCAPE_GAP;
            }
            ball.setPosition(escapedX, escapedY);
            ball.setBallXVelocity(nextXVelocity);
            ball.setBallYVelocity(nextYVelocity);
            return true;
    }
    /**
     * Checks if the ball has collided with any of the bricks. If a collision occurs,
     * the brick is removed from the game.
     * @param previousBallBounds The ball rectangle before the current sub-step movement.
     * @return true if a collision with a brick occurred, false otherwise.
     */
    private boolean isBrickCollision(Rectangle previousBallBounds){
        int numOfLines = lineOfBricks.getNumOfLines();
        Rectangle currentBallBounds = new Rectangle(ball.getX(), ball.getY(), Ball.getWidth(), Ball.getHeight());

        for(int i = 0; i < numOfLines; i++){
            int numOfBricks = lineOfBricks.getLineByIndex(i).getNumOfBricks();
            for(int j = 0; j < numOfBricks; j++){
                Rectangle brick = lineOfBricks.getLineByIndex(i).getBrickByIndex(j).getRectangleBrick();
                if(isCircleIntersectsRect(brick)){
                    if(shouldBounceX(previousBallBounds, currentBallBounds, brick)){
                        placeBallOutsideBrickOnX(previousBallBounds, brick);
                        ballBounceX();
                    }
                    else{
                        placeBallOutsideBrickOnY(previousBallBounds, brick);
                        ballBounceY();
                    }
                    screen.brickDestroy(i, j);
                    lineOfBricks.removeBrickFromLineByIndex(i,j);
                    return true;
                }
            }
        }
        return false;
    }

    /** Checks and resolves collision with the left/right/top screen bounds. */
    private boolean screenWallCollision(){
        boolean collided = false;
        boolean hitLeftWall = false;
        boolean hitRightWall = false;
        boolean hitTopWall = false;
        int ballX = ball.getX();
        int ballY = ball.getY();
        boolean touchingLeftWall = ballX <= 0;
        boolean touchingRightWall = ballX >= BALL_SCREEN_COLLISION_X;
        boolean touchingTopWall = ballY <= 0;

        // Only resolve when entering/outside the wall, not while already moving away from it.
        if(ballX < 0 || (touchingLeftWall && ball.getBallXVelocity() < 0)){
            ball.setPrecisePosition(0, ball.getPreciseY());
            if(ball.getBallXVelocity() < 0){
                ballBounceX();
            }
            hitLeftWall = true;
            collided = true;
        }
        else if(ballX > BALL_SCREEN_COLLISION_X || (touchingRightWall && ball.getBallXVelocity() > 0)){
            ball.setPrecisePosition(BALL_SCREEN_COLLISION_X, ball.getPreciseY());
            if(ball.getBallXVelocity() > 0){
                ballBounceX();
            }
            hitRightWall = true;
            collided = true;
        }

        if(ballY < 0 || (touchingTopWall && ball.getBallYVelocity() < 0)){
            ball.setPrecisePosition(ball.getPreciseX(), 0);
            if(ball.getBallYVelocity() < 0){
                ballBounceY();
            }
            hitTopWall = true;
            collided = true;
        }

        // Corner guard: top-left/top-right hits should not become a straight vertical fall.
        if(hitTopWall && (hitLeftWall || hitRightWall) && Math.abs(ball.getBallXVelocity()) < MIN_CORNER_X_VELOCITY){
            ball.setBallXVelocity(hitRightWall ? -MIN_CORNER_X_VELOCITY : MIN_CORNER_X_VELOCITY);
        }

        return collided;
    }

    /** Circle-vs-rectangle check for the ball against a target rectangle. */
    private boolean isCircleIntersectsRect(Rectangle rect){
        double radius = Ball.getWidth() / 2.0;
        double centerX = ball.getX() + radius;
        double centerY = ball.getY() + (Ball.getHeight() / 2.0);
        double closestX = clamp(centerX, rect.getX(), rect.getX() + rect.getWidth());
        double closestY = clamp(centerY, rect.getY(), rect.getY() + rect.getHeight());
        double deltaX = centerX - closestX;
        double deltaY = centerY - closestY;

        return (deltaX * deltaX) + (deltaY * deltaY) <= radius * radius;
    }

    private boolean shouldBounceX(Rectangle previousBallBounds, Rectangle currentBallBounds, Rectangle brick){
        double centerX = currentBallBounds.getCenterX();
        double centerY = currentBallBounds.getCenterY();
        double closestX = clamp(centerX, brick.getX(), brick.getX() + brick.getWidth());
        double closestY = clamp(centerY, brick.getY(), brick.getY() + brick.getHeight());
        double deltaX = centerX - closestX;
        double deltaY = centerY - closestY;

        // If contact normal clearly points left/right, bounce on X.
        if(Math.abs(deltaX) - Math.abs(deltaY) > EPSILON){
            return true;
        }
        if(Math.abs(deltaY) - Math.abs(deltaX) > EPSILON){
            return false;
        }

        // Resolve ambiguous corner/overlap cases using swept entry.
        boolean crossedVerticalFace = (previousBallBounds.x + previousBallBounds.width <= brick.x
                && currentBallBounds.x + currentBallBounds.width >= brick.x)
                || (previousBallBounds.x >= brick.x + brick.width
                && currentBallBounds.x <= brick.x + brick.width);
        boolean crossedHorizontalFace = (previousBallBounds.y + previousBallBounds.height <= brick.y
                && currentBallBounds.y + currentBallBounds.height >= brick.y)
                || (previousBallBounds.y >= brick.y + brick.height
                && currentBallBounds.y <= brick.y + brick.height);

        if(crossedVerticalFace && !crossedHorizontalFace){
            return true;
        }
        if(crossedHorizontalFace && !crossedVerticalFace){
            return false;
        }

        // Final fallback for exact corner equality.
        if(Math.abs(ball.getBallXVelocity()) < EPSILON && Math.abs(ball.getBallYVelocity()) < EPSILON){
            return false;
        }
        return Math.abs(ball.getBallXVelocity()) >= Math.abs(ball.getBallYVelocity());
    }

    private void placeBallOutsideBrickOnX(Rectangle previousBallBounds, Rectangle brick){
        if(ball.getBallXVelocity() > EPSILON){
            ball.setPosition(brick.x - Ball.getWidth(), ball.getY());
        }
        else if(ball.getBallXVelocity() < -EPSILON){
            ball.setPosition(brick.x + brick.width, ball.getY());
        }
        else if(previousBallBounds.x + previousBallBounds.width <= brick.x){
            ball.setPosition(brick.x - Ball.getWidth(), ball.getY());
        }
        else if(previousBallBounds.x >= brick.x + brick.width){
            ball.setPosition(brick.x + brick.width, ball.getY());
        }
        else{
            int ballCenterX = ball.getX() + (Ball.getWidth() / 2);
            if(ballCenterX <= brick.getCenterX()){
                ball.setPosition(brick.x - Ball.getWidth(), ball.getY());
            }
            else{
                ball.setPosition(brick.x + brick.width, ball.getY());
            }
        }
    }

    private void placeBallOutsideBrickOnY(Rectangle previousBallBounds, Rectangle brick){
        if(ball.getBallYVelocity() > EPSILON){
            ball.setPosition(ball.getX(), brick.y - Ball.getHeight());
        }
        else if(ball.getBallYVelocity() < -EPSILON){
            ball.setPosition(ball.getX(), brick.y + brick.height);
        }
        else if(previousBallBounds.y + previousBallBounds.height <= brick.y){
            ball.setPosition(ball.getX(), brick.y - Ball.getHeight());
        }
        else if(previousBallBounds.y >= brick.y + brick.height){
            ball.setPosition(ball.getX(), brick.y + brick.height);
        }
        else{
            int ballCenterY = ball.getY() + (Ball.getHeight() / 2);
            if(ballCenterY <= brick.getCenterY()){
                ball.setPosition(ball.getX(), brick.y - Ball.getHeight());
            }
            else{
                ball.setPosition(ball.getX(), brick.y + brick.height);
            }
        }
    }

    private double clamp(double value, double min, double max){
        return Math.max(min, Math.min(max, value));
    }
    /**
     * Determines if the game has ended, either by destroying all bricks (win)
     * or losing all life points (lose).
     * @return true if the game is over, false otherwise.
     */
    private boolean isGameOver(){
        if(lineOfBricks.getNumOfLines() == 0){
            return true;
        }
        else if(player.getLifePoints() == 0){
            return true;
        }
        return false;
    }
    /**
     * Updates the paddle's position based on the current movement flags.
     * This method is called in the game loop to ensure smooth, continuous movement.
     */
    private void updatePaddlePosition() {
        int paddlePositionX = paddle.getX();
        if (movingLeft && paddlePositionX > PADDLE_SCREEN_LEFT_LIMIT) {
            paddlePositionX -= paddle.getSpeed();
        }
        if (movingRight && paddlePositionX < PADDLE_SCREEN_RIGHT_LIMIT) {
            paddlePositionX += paddle.getSpeed();
        }
        paddlePositionX = (int)Math.round(clamp(paddlePositionX, PADDLE_SCREEN_LEFT_LIMIT, PADDLE_SCREEN_RIGHT_LIMIT));
        paddle.setX(paddlePositionX);
        screen.paddleLabel.setLocation(paddle.getX(), paddle.getY());
    }
    private void ballReset(){
        if(ballDefaultPosition && spacePressed){
            ball.setBallXVelocity(ball.getDefaultBallXVelocity());
            ball.setBallYVelocity(ball.getDefaultBallYVelocity());
            ballDefaultPosition = false;
        }
    }
    /**
     * Handles key presses for paddle movement.
     * Sets boolean flags to indicate the start of movement.
     * @param e The KeyEvent generated by the key press.
     */
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT)
            movingLeft = true;
        else if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT)
            movingRight = true;
        else if(key == KeyEvent.VK_SPACE)
            spacePressed = true;

    }

    /** Handles key releases for paddle movement. Sets boolean flags to indicate the end of movement. */
    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT)
            movingLeft = false;
        else if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT)
            movingRight = false;
        else if(key == KeyEvent.VK_SPACE)
            spacePressed = false;
    }
    /** This method is intentionally left empty as it is not needed. */
    @Override
    public void keyTyped(KeyEvent e) {}
}
