package GameObjects;
import javax.swing.ImageIcon;
/**
 * The Ball class represents the ball object in the game.
 * It stores information about the ball's position, velocity, and appearance.
 */

import Render.AssetPaths;
import Render.Screen;
public class Ball {
    private static final String ICON_PATH = AssetPaths.BALL_ICON_PATH;
    private static final int WIDTH = 53;
    private static final int HEIGHT = 53;
    private static ImageIcon ballIcon;
    private int x;
    private int y;
    /** Sub-pixel position used by collision/movement to avoid rounding artifacts. */
    private double preciseX;
    private double preciseY;
    /**
     * The horizontal velocity of the ball in pixels per timer tick.
     * A larger number means a faster ball.
     */
    private double defaultXVelocity = 0;
    private double xVelocity = 0;
    /**
     * The vertical velocity of the ball in pixels per timer tick.
     * A larger number means a faster ball.
     */
    private double defaultYVelocity = 11;
    private double yVelocity = 0;

    /**
     * Constructs a new Ball object at the specified coordinates.
     * @param x The initial x-coordinate of the ball.
     * @param y The initial y-coordinate of the ball.
     */
    public Ball(int x, int y){
        ballIcon = new ImageIcon(ICON_PATH);
        this.x = x;
        this.y = y;
        this.preciseX = x;
        this.preciseY = y;
    }

    /**
     * Place the ball at the middle of the screen to wait for space to drop, use when the paddle miss the ball or it's new level
     */
    public void resetPosition(){
        this.setPosition(Screen.WINDOW_WIDTH / 2, Screen.WINDOW_HEIGHT / 2);
        this.setBallXVelocity(0);
        this.setBallYVelocity(0);
    }

    /**
     * Returns the width of the ball in pixels.
     * @return The width of the ball.
     */
    public static int getWidth() {
        return WIDTH;
    }

    /**
     * Returns the height of the ball in pixels.
     * @return The height of the ball.
     */
    public static int getHeight() {
        return HEIGHT;
    }

    /**
     * Returns the current x-coordinate of the ball.
     * @return The x-coordinate of the ball.
     */
    public int getX(){
        return x;
    }

    /**
     * Returns the current y-coordinate of the ball.
     * @return The y-coordinate of the ball.
     */
    public int getY(){
        return y;
    }

    /** Returns the sub-pixel x-coordinate used by the movement simulation. */
    public double getPreciseX(){
        return preciseX;
    }

    /** Returns the sub-pixel y-coordinate used by the movement simulation. */
    public double getPreciseY(){
        return preciseY;
    }

    /**
     * Returns the icon for the ball.
     * @return The ImageIcon of the ball.
     */
    public static ImageIcon getIcon(){
        return ballIcon;
    }

    /**
     * Returns the path to the ball's icon image.
     * @return The file path of the ball's icon.
     */
    public static String getIconPath(){
        return ICON_PATH;
    }

    /**
     * Returns the horizontal velocity of the ball.
     * @return The ball's horizontal velocity.
     */
    public double getBallXVelocity(){
        return xVelocity;
    }

    public double getDefaultBallXVelocity(){
        return defaultXVelocity;
    }
    /**
     * Sets the horizontal velocity of the ball.
     * @param xVelocity The new horizontal velocity.
     */
    public void setBallXVelocity(double xVelocity){
        this.xVelocity = xVelocity;
    }
    /**
     * Returns the vertical velocity of the ball.
     * @return The ball's vertical velocity.
     */
    public double getBallYVelocity(){
        return yVelocity;
    }

    public double getDefaultBallYVelocity(){
        return defaultYVelocity;
    }
    /**
     * Sets the vertical velocity of the ball.
     * @param yVelocity The new vertical velocity.
     */
    public void setBallYVelocity(double yVelocity){
        this.yVelocity = yVelocity;
    }
    /**
     * Sets the position of the ball to the specified coordinates.
     * This is used to update the ball's location after movement or collision calculations.
     * @param x The new x-coordinate.
     * @param y The new y-coordinate.
     */
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
        // Keep precise and integer positions in sync when hard-correcting position.
        this.preciseX = x;
        this.preciseY = y;
    }

    /**
     * Sets sub-pixel accurate position and updates the integer render position.
     * @param x The new precise x-coordinate.
     * @param y The new precise y-coordinate.
     */
    public void setPrecisePosition(double x, double y) {
        this.preciseX = x;
        this.preciseY = y;
        this.x = (int)Math.round(x);
        this.y = (int)Math.round(y);
    }
}
