package Main;
import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import Render.AssetPaths;

/**
 * The SoundEffect class is responsible for loading and playing all sound effects used in the game.
 * It pre-loads audio clips for collisions to ensure they can be played without delay.
 */
public class SoundEffect {
    // Suggestion: These file paths could be made `private static final` as they are constants.
    private String COLLISION_SOUND_EFFECT_FILE_PATH = AssetPaths.COLLISION_SOUND_EFFECT_FILE_PATH;
    private String BRICK_COLLISION_SOUND_EFFECT_FILE_PATH = AssetPaths.BRICK_COLLISION_SOUND_EFFECT_FILE_PATH;
    private Clip collisionSoundEffect;
    private Clip brickCollisionSoundEffect;
    private static final long COLLISION_SOUND_COOLDOWN_NS = 25_000_000L;
    private static final long BRICK_SOUND_COOLDOWN_NS = 20_000_000L;
    private long lastCollisionSoundTimeNs = 0L;
    private long lastBrickSoundTimeNs = 0L;

    /**
     * Constructs a SoundEffect object and loads all the necessary audio files into memory.
     * @throws LineUnavailableException if a clip cannot be obtained due to resource restrictions.
     * @throws IOException if an I/O error occurs when reading the audio file.
     * @throws UnsupportedAudioFileException if the audio file format is not supported.
     */
    public SoundEffect() throws LineUnavailableException, IOException, UnsupportedAudioFileException{
        // Load the sound effect for general collisions (e.g., ball with paddle or walls).
        File collisionSoundEffectFile = new File(COLLISION_SOUND_EFFECT_FILE_PATH);
		AudioInputStream audioStream = AudioSystem.getAudioInputStream(collisionSoundEffectFile);
		collisionSoundEffect = AudioSystem.getClip();
		collisionSoundEffect.open(audioStream);

        // Load the sound effect for when the ball collides with a brick.
        File brickCollisionSoundEffectFile = new File(BRICK_COLLISION_SOUND_EFFECT_FILE_PATH);
		AudioInputStream brickAudioStream = AudioSystem.getAudioInputStream(brickCollisionSoundEffectFile);
		brickCollisionSoundEffect = AudioSystem.getClip();
		brickCollisionSoundEffect.open(brickAudioStream);
    }

    /**
     * Stops and closes the audio clips to release system resources.
     * This should be called when the game is shutting down.
     */
    public void close(){
        collisionSoundEffect.stop();
        collisionSoundEffect.close();
        brickCollisionSoundEffect.stop();
        brickCollisionSoundEffect.close();
    }

    /**
     * Plays the standard collision sound effect from the beginning.
     * If the clip is already playing, it is stopped and reset before playing again.
     */
    public void playCollisionSoundEffect(){
        long now = System.nanoTime();
        if(now - lastCollisionSoundTimeNs < COLLISION_SOUND_COOLDOWN_NS)
            return;
        lastCollisionSoundTimeNs = now;

        if(collisionSoundEffect.isRunning())
            collisionSoundEffect.stop();
        collisionSoundEffect.setFramePosition(0);
        collisionSoundEffect.start();
    }

    /**
     * Plays the brick collision sound effect from the beginning.
     * If the clip is already playing, it is stopped and reset before playing again.
     */
    public void playBrickCollisionSoundEffect(){
        long now = System.nanoTime();
        if(now - lastBrickSoundTimeNs < BRICK_SOUND_COOLDOWN_NS)
            return;
        lastBrickSoundTimeNs = now;

        if(brickCollisionSoundEffect.isRunning())
            brickCollisionSoundEffect.stop();
        brickCollisionSoundEffect.setFramePosition(0);
        brickCollisionSoundEffect.start();
    }
}
