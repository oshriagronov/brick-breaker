package Main;
import Render.*;
import GameObjects.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
/**
 * The GameManager class is the central component of the game.
 * It is responsible for initializing the game objects, managing the game state,
 * handling user input, and coordinating the overall game flow from the menu to the game's end.
 */
public class GameManager implements KeyListener{
    private final int Ball_DEFAULT_X = Screen.WINDOW_WIDTH / 2;
    private final int BALL_DEFAULT_Y = Screen.WINDOW_HEIGHT / 2;
    private final int PADDLE_DEFAULT_X = (Screen.WINDOW_WIDTH / 2) - (Paddle.getWidth() / 2);
    private final int PADDLE_DEFAULT_Y = Screen.WINDOW_HEIGHT - 70;
    //private final int numOfLinesOfBricks = 1;
    private Screen screen;
    private SoundEffect sound_effect;
    private Paddle paddle;
    private Ball ball;
    private Player player;
    private BrickLines lineOfBricks;
    private Gameplay gameplay;
    /** The number of lives the player has before the game is over. */
    private int life_points = 3;
    /** The points awarded for breaking a single brick. */
    private int score_points = 100;
    private int currentLevel = 1;
    /** Tracks if a key has been pressed to start the game from the menu. */
    private boolean key_pressed;
    /** Tracks if the game is waiting for a restart key after game over. */
    private boolean waitingForRestart;
    /**
     * Constructs a GameManager, initializing all game components.
     * It performs a pre-launch check for necessary asset files and exits if any are missing.
     * It sets up the screen, game objects (paddle, ball, player), and calculates brick layout.
     */
    public GameManager(){
        // Checks for the existence of all required game assets. If any are missing,
        // an error window is shown, and the constructor returns early.
        if(CheckPath()){
            return;
        }
        // Initialize core game components.
        screen = new Screen();
        paddle = new Paddle(PADDLE_DEFAULT_X, PADDLE_DEFAULT_Y);
        ball = new Ball(Ball_DEFAULT_X, BALL_DEFAULT_Y);
        player = new Player(life_points, score_points);
        lineOfBricks = new BrickLines(currentLevel);
        screen.addKeyListener(this);
        key_pressed = false;
        waitingForRestart = false;
    }

    /**
     * The main entry point of the application.
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        GameManager gameManager = new GameManager();
        gameManager.menu_screen();
    }

    /**
     * Displays the initial menu screen, waiting for player input to start the game.
     */
    public void menu_screen(){
        key_pressed = false;
        waitingForRestart = false;
        screen.menuScreen();
    }

    /**
     * Starts the main gameplay loop. This method is called once the player
     * initiates the game from the menu. It sets up the game level by creating bricks,
     * adding all game objects to the screen, and starting the Gameplay timer.
     */
    public void start(){
        waitingForRestart = false;
        screen.clearScreen();
        if(gameplay != null){
            screen.removeKeyListener(gameplay);
            gameplay = null;
        }
        screen.removeKeyListener(this); // Remove this listener to pass control to Gameplay's listener.
        // Render all game objects on the screen.
        screen.addPaddleLabel(Paddle.getIcon(), paddle.getX(), paddle.getY(), Paddle.getWidth(), Paddle.getHeight());
        screen.addBallLabel(Ball.getIcon(), ball.getX(), ball.getY(), Ball.getWidth(), Ball.getHeight());
        screen.addHeartLabels(player.getLifePoints(), player.getHeartIcon(), Player.getHeartWidth(), Player.getHeartHeight());
        screen.addBricksLabels(lineOfBricks);
        screen.addPlayerScore(player.getScore());
        // Initialize and run the core gameplay logic.
        gameplay = new Gameplay(player, screen, sound_effect, ball, paddle, lineOfBricks);
        // Set up a listener to handle game-end conditions (win or lose).
        gameplay.setGameEndListener(() -> {
        screen.removeKeyListener(gameplay);
        if (player.getLifePoints() == 0) {
            screen.clearScreen();
            screen.gameOverScreen();
            waitingForRestart = true;
            key_pressed = false;
            screen.addKeyListener(this);
        } else {
            currentLevel++;
            if(currentLevel <= 4){
                screen.clearScreen();
                ball.resetPosition();
                lineOfBricks.resetBricks(currentLevel);
                screen.addBricksLabels(lineOfBricks);
                start();
            }
            else{
                screen.clearScreen();
                screen.winingScreen();
                waitingForRestart = true;
                key_pressed = false;
                screen.addKeyListener(this);
            }
        }
        });
        gameplay.run();
    }

    private void resetGameState(){
        currentLevel = 1;
        player = new Player(life_points, score_points);
        paddle = new Paddle(PADDLE_DEFAULT_X, PADDLE_DEFAULT_Y);
        ball = new Ball(Ball_DEFAULT_X, BALL_DEFAULT_Y);
        lineOfBricks = new BrickLines(currentLevel);
    }
    /**
     * Invoked when a key has been pressed. Used here to detect the first key press
     * on the menu screen to start the game.
     * @param e The KeyEvent generated by the key press.
     */
    @Override
    public void keyPressed(KeyEvent e) {
        if(waitingForRestart){
            waitingForRestart = false;
            resetGameState();
            start();
            return;
        }
        if(!key_pressed){
            key_pressed = true;
            start();
        }
    }

    /**
     * This method is not used in this context.
     * @param e The KeyEvent.
     */
    @Override
    public void keyTyped(KeyEvent e) {
        // Intentionally left empty.
    }

    /**
     * This method is not used in this context.
     * @param e The KeyEvent.
     */
    @Override
    public void keyReleased(KeyEvent e) {
        // Intentionally left empty.
    }

    /**
     * Verifies the existence of all required asset files (images and sounds).
     * If a file is not found, it displays an error window.
     * @return true if an error occurred (e.g., a file is missing), false otherwise.
     */
    public boolean CheckPath(){
        // Check for image assets.
        if(!Files.exists(Paths.get(Paddle.getIconPath()))){
            new ErrorWindow("The paddle PNG file is missing from the assets folder.");
            return true;
        }
        if(!Files.exists(Paths.get(Ball.getIconPath()))){
            new ErrorWindow("The ball PNG file is missing from the assets folder.");
            return true;
        }
        if(!Files.exists(Paths.get(Brick.getIconPath()))){
            new ErrorWindow("The brick PNG file is missing from the assets folder.");
            return true;
        }
        if(!Files.exists(Paths.get(Player.getHeartIconPath()))){
            new ErrorWindow("The heart PNG file is missing from the assets folder.");
            return true;
        }
        // Check for sound assets.
        try {
            sound_effect = new SoundEffect();
        } catch (LineUnavailableException e) {
            new ErrorWindow("Audio line is unavailable. The sound device may be in use.");
            return true;
        } catch (UnsupportedAudioFileException e) {
            new ErrorWindow("One or more audio files are in an unsupported format.");
            return true;
        } catch (IOException e) {
            new ErrorWindow("An audio file is missing or cannot be read from the assets folder.");
            return true;
        }
        return false;
    }
}
