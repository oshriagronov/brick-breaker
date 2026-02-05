package Render;
import javax.swing.JFrame;
import javax.swing.JLabel;
import GameObjects.Brick;
import GameObjects.BrickLines;
import Main.Player;

import javax.swing.ImageIcon;
import javax.swing.SwingConstants;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
/**
 * The Screen class represents the main game window.
 * It extends JFrame and is responsible for rendering all visual elements,
 * including the background, game objects (paddle, ball, bricks), UI elements (score, lives),
 * and various game state screens (menu, win, game over).
 */
public class Screen extends JFrame{
    /** The height of the game window in pixels. */
    public final static int WINDOW_HEIGHT = 720;
    /** The width of the game window in pixels. */
    public final static int WINDOW_WIDTH = 1280;
    // Asset paths for icons and backgrounds.
    private final String ICON_PATH = AssetPaths.ICON_PATH;
    private final String BACKGROUND_PATH = AssetPaths.BACKGROUND_PATH;
    private final String MENU_ICON_PATH = AssetPaths.MENU_ICON_PATH;
    private final String WINING_ICON_PATH = AssetPaths.WINING_ICON_PATH;
    private final String GAME_OVER_ICON_PATH = AssetPaths.GAME_OVER_ICON_PATH;

    // JLabels used to display game elements.
    public JLabel paddleLabel;
    public JLabel ballLabel;
    private JLabel backgroundLabel;
    private JLabel playerScore;
    private JLabel menuLogoLabel;
    private JLabel winingLogoLabel;
    private JLabel gameOverLogoLabel;
    private List <JLabel> heartLabels;
    private List <ArrayList<JLabel>> bricksLines = new ArrayList<>();

    /**
     * Constructs the main game screen (JFrame).
     * Initializes window properties, sets the background, and makes the window visible.
     */
    public Screen(){
        this.setTitle("Brick Breaker");
        this.setIconImage(new ImageIcon(ICON_PATH).getImage());
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.setLayout(null);
        this.setLocationRelativeTo(null);
        this.setSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));

        // Use a JLabel to display the background image.
        backgroundLabel = new JLabel(new ImageIcon(BACKGROUND_PATH));
        backgroundLabel.setLayout(null);
        backgroundLabel.setBounds(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        this.add(backgroundLabel);
        this.setVisible(true);
    }

    /**
     * Displays the main menu screen with the game logo and a prompt to start.
     */
    public void menuScreen(){
        menuLogoLabel = new JLabel(new ImageIcon(MENU_ICON_PATH));
        menuLogoLabel.setBounds((WINDOW_WIDTH / 2) - 400, 100, 800, 279);
        JLabel menuText = new JLabel("press any key to start!");
        menuText.setBounds(WINDOW_WIDTH / 4 + 125, WINDOW_HEIGHT - 600, 600, 600);
        menuText.setFont(new Font("Monospaced", Font.BOLD, 28));
        menuText.setForeground(new Color(0, 255, 180));
        backgroundLabel.add(menuLogoLabel);
        backgroundLabel.add(menuText);
        backgroundLabel.revalidate(); 
        backgroundLabel.repaint();
    }

    /**
     * Adds a JLabel for the paddle to the screen.
     * @param icon The icon for the label.
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     * @param width The width of the label.
     * @param height The height of the label.
     */

    public void addPaddleLabel(ImageIcon icon, int x, int y, int width, int height){
        paddleLabel = new JLabel(icon, JLabel.CENTER);
        paddleLabel.setBounds(x, y, width, height);
        backgroundLabel.add(paddleLabel);
        backgroundLabel.revalidate(); 
        backgroundLabel.repaint();
    }
    /**
     * Adds a JLabel for the ball to the screen.
     * @param icon The icon for the label.
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     * @param width The width of the label.
     * @param height The height of the label.
     */
    public void addBallLabel(ImageIcon icon, int x, int y, int width, int height){
        ballLabel = new JLabel(icon, JLabel.CENTER);
        ballLabel.setBounds(x, y, width, height);
        backgroundLabel.add(ballLabel);
        backgroundLabel.revalidate(); 
        backgroundLabel.repaint();
    }
    /**
     * Creates and adds JLabels for all bricks to the screen.
     * @param brick_array The list of Brick objects.
     * @param numOfBricks The total number of bricks.
     */
    public void addBricksLabels(BrickLines lineOfBricks){
        bricksLines.clear();
        for(int i = 0; i < lineOfBricks.getNumOfLines(); i++){
            bricksLines.add(new ArrayList<>());
            for(int j = 0; j < lineOfBricks.getLineByIndex(i).getNumOfBricks(); j++){
                JLabel brick = new JLabel(lineOfBricks.getLineByIndex(i).getBrickByIndex(j).getIcon(), JLabel.CENTER);
                brick.setBounds(lineOfBricks.getLineByIndex(i).getBrickByIndex(j).getX(), lineOfBricks.getLineByIndex(i).getBrickByIndex(j).getY(), Brick.getWidth(), Brick.getHeight());
                bricksLines.get(i).add(brick);
                backgroundLabel.add(brick);
            }
        }
        backgroundLabel.revalidate(); 
        backgroundLabel.repaint();
    }

    /**
     * Removes a brick's JLabel from the screen after it has been destroyed.
     * @param brickLineIndex The index of the line the brick belong
     * @param brickIndex The index of the brick to remove.
     */
    public void brickDestroy(int brickLineIndex, int brickIndex){
        backgroundLabel.remove(bricksLines.get(brickLineIndex).get(brickIndex));
        bricksLines.get(brickLineIndex).remove(brickIndex);
        if(bricksLines.get(brickLineIndex).isEmpty()){
            bricksLines.remove(brickLineIndex);
        }
        // Revalidate and repaint the screen (to ensure the changes are applied
        backgroundLabel.revalidate();
        backgroundLabel.repaint();
    }

    /**
     * Adds the player's score display to the screen.
     * @param score The initial score to display.
     */
    public void addPlayerScore(int score){
        playerScore = new JLabel("score: " + score);
        playerScore.setBounds(10, 0, 200, 50);
        playerScore.setFont(new Font("Monospaced", Font.BOLD, 28));
        playerScore.setForeground(new Color(0, 255, 180));
        backgroundLabel.add(playerScore);
        backgroundLabel.revalidate(); 
        backgroundLabel.repaint();
    }

    /**
     * Refreshes the score display with the current score.
     * @param score The new score to display.
     */
    public void refreshPlayerScore(int score){
        playerScore.setText("score: " + score);
        backgroundLabel.revalidate(); 
        backgroundLabel.repaint();
    }

    /**
     * Adds the heart icons to the screen to represent the player's lives.
     * @param numOfHearts The number of lives the player has.
     * @param icon The heart icon.
     * @param x The base x-coordinate for the first heart.
     * @param y The y-coordinate for the hearts.
     * @param width The width of a heart icon.
     * @param height The height of a heart icon.
     */
    public void addHeartLabels(int numOfHearts, ImageIcon icon, int width, int height){
        heartLabels = new ArrayList<>();
        for(int i = 0; i < numOfHearts; i++){
            heartLabels.add(new JLabel(icon,JLabel.CENTER));
            heartLabels.get(i).setBounds(WINDOW_WIDTH - (Player.getHeartWidth() * (i + 1)) , 0, width, height); // Hearts are placed side-by-side.
            backgroundLabel.add(heartLabels.get(i));
        }
        backgroundLabel.revalidate(); 
        backgroundLabel.repaint();
    }

    /**
     * Removes a heart icon from the screen when the player loses a life.
     * @param num The index of the heart label to remove.
     */
    public void removeHeartLabel(int index){
        backgroundLabel.remove(heartLabels.get(index));
        backgroundLabel.revalidate(); 
        backgroundLabel.repaint();
        heartLabels.remove(index);
    }

    /** Displays the winning screen. */
    public void winingScreen(){
        int winingLogoWidth = 500;
        int winingLogoHeight = 435;
        winingLogoLabel = new JLabel(new ImageIcon(WINING_ICON_PATH));
        winingLogoLabel.setBounds((WINDOW_WIDTH - winingLogoWidth) / 2, 100, winingLogoWidth, winingLogoHeight);
        JLabel winPrompt = new JLabel("press any key to play again!");
        winPrompt.setBounds(0, winingLogoLabel.getY() + winingLogoLabel.getHeight() + 15, WINDOW_WIDTH, 50);
        winPrompt.setHorizontalAlignment(SwingConstants.CENTER);
        winPrompt.setFont(new Font("Monospaced", Font.BOLD, 28));
        winPrompt.setForeground(new Color(0, 255, 180));
        backgroundLabel.add(winingLogoLabel);
        backgroundLabel.add(winPrompt);
        backgroundLabel.revalidate(); 
        backgroundLabel.repaint();
    }

    /** Displays the game over screen. */
    public void gameOverScreen(){
        int gameOverLogoWidth = 800;
        int gameOverLogoHeight = 800;
        gameOverLogoLabel = new JLabel(new ImageIcon(GAME_OVER_ICON_PATH));
        gameOverLogoLabel.setBounds((WINDOW_WIDTH - gameOverLogoWidth) / 2, -170, gameOverLogoWidth, gameOverLogoHeight);
        JLabel gameOverText = new JLabel("press any key to restart!");
        gameOverText.setBounds(0, gameOverLogoLabel.getY() + gameOverLogoLabel.getHeight() + 15, WINDOW_WIDTH, 50);
        gameOverText.setHorizontalAlignment(SwingConstants.CENTER);
        gameOverText.setFont(new Font("Monospaced", Font.BOLD, 28));
        gameOverText.setForeground(new Color(0, 255, 180));
        backgroundLabel.add(gameOverLogoLabel);
        backgroundLabel.add(gameOverText);
        backgroundLabel.revalidate(); 
        backgroundLabel.repaint();
    }

    /**
     * Clears all dynamic labels (like ball, paddle, bricks) from the screen,
     * typically used when transitioning between game states.
     */
    public void clearScreen(){
        backgroundLabel.removeAll();
        backgroundLabel.revalidate(); 
        backgroundLabel.repaint();
    }
}
