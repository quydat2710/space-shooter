package quydat.com;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

public class AudioManager {
    private static AudioManager instance;

    // Sound effects
    private Sound laserSound;
    private Sound borderSound;
    private Sound explosionSound;
    private Sound gameOverSound;

    // Background music
    private Music backgroundMusic;
    private Music gameOverMusic;

    // Volume controls
    private float soundVolume = 1.0f;
    private float musicVolume = 1.0f;

    // New audio control logic
    private boolean allSoundEnabled = true;  // Tắt/bật tất cả âm thanh
    private boolean musicOnlyEnabled = true; // Tắt/bật chỉ nhạc nền (khi allSound = true)

    private AudioManager() {
        loadAudio();
    }

    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }

    private void loadAudio() {
        try {
            Gdx.app.log("AudioManager", "Starting to load audio files...");

            // Load sound effects
            try {
                laserSound = Gdx.audio.newSound(Gdx.files.internal("laser.mp3"));
                Gdx.app.log("AudioManager", "Laser sound loaded successfully");
            } catch (Exception e) {
                Gdx.app.error("AudioManager", "Failed to load laser.mp3: " + e.getMessage());
            }

            try {
                borderSound = Gdx.audio.newSound(Gdx.files.internal("cham_bien.mp3"));
                Gdx.app.log("AudioManager", "Border sound loaded successfully");
            } catch (Exception e) {
                Gdx.app.error("AudioManager", "Failed to load cham_bien.mp3: " + e.getMessage());
            }

            try {
                explosionSound = Gdx.audio.newSound(Gdx.files.internal("explosion.mp3"));
                Gdx.app.log("AudioManager", "Explosion sound loaded successfully");
            } catch (Exception e) {
                Gdx.app.error("AudioManager", "Failed to load explosion.mp3: " + e.getMessage());
            }

            try {
                gameOverSound = Gdx.audio.newSound(Gdx.files.internal("game_over.mp3"));
                Gdx.app.log("AudioManager", "Game over sound loaded successfully");
            } catch (Exception e) {
                Gdx.app.error("AudioManager", "Failed to load game_over.mp3: " + e.getMessage());
            }

            // Load background music
            try {
                backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("game_music.mp3"));
                backgroundMusic.setLooping(true);
                Gdx.app.log("AudioManager", "Background music loaded successfully");
            } catch (Exception e) {
                Gdx.app.error("AudioManager", "Failed to load game_music.mp3: " + e.getMessage());
                // Try alternative formats
                try {
                    backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("game_music.ogg"));
                    backgroundMusic.setLooping(true);
                    Gdx.app.log("AudioManager", "Background music loaded successfully (OGG format)");
                } catch (Exception e2) {
                    try {
                        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("game_music.wav"));
                        backgroundMusic.setLooping(true);
                        Gdx.app.log("AudioManager", "Background music loaded successfully (WAV format)");
                    } catch (Exception e3) {
                        Gdx.app.error("AudioManager", "Failed to load background music in any format");
                    }
                }
            }

            try {
                gameOverMusic = Gdx.audio.newMusic(Gdx.files.internal("game_over_music.mp3"));
                gameOverMusic.setLooping(false);
                Gdx.app.log("AudioManager", "Game over music loaded successfully");
            } catch (Exception e) {
                Gdx.app.error("AudioManager", "Failed to load game_over_music.mp3: " + e.getMessage());
            }

        } catch (Exception e) {
            Gdx.app.error("AudioManager", "General error loading audio: " + e.getMessage());
        }
    }

    // Updated sound effect methods with new logic
    public void playLaserSound() {
        if (allSoundEnabled && laserSound != null) {
            laserSound.play(soundVolume * 0.3f);
        }
    }

    public void playBorderSound() {
        if (allSoundEnabled && borderSound != null) {
            borderSound.play(soundVolume * 0.5f);
        }
    }

    public void playExplosionSound() {
        if (allSoundEnabled && explosionSound != null) {
            explosionSound.play(soundVolume * 0.6f);
        }
    }

    public void playGameOverSound() {
        if (allSoundEnabled && gameOverSound != null) {
            gameOverSound.play(soundVolume * 0.8f);
        }
    }

    // Updated music methods with new logic
    public void playBackgroundMusic() {
        Gdx.app.log("AudioManager", "playBackgroundMusic() called");
        Gdx.app.log("AudioManager", "All Sound enabled: " + allSoundEnabled);
        Gdx.app.log("AudioManager", "Music only enabled: " + musicOnlyEnabled);
        Gdx.app.log("AudioManager", "Background music null: " + (backgroundMusic == null));

        if (allSoundEnabled && musicOnlyEnabled && backgroundMusic != null) {
            if (!backgroundMusic.isPlaying()) {
                backgroundMusic.setVolume(musicVolume * 0.3f);
                backgroundMusic.play();
                Gdx.app.log("AudioManager", "Background music started playing");
            } else {
                Gdx.app.log("AudioManager", "Background music already playing");
            }
        } else {
            if (!allSoundEnabled) {
                Gdx.app.log("AudioManager", "All sound is disabled");
            } else if (!musicOnlyEnabled) {
                Gdx.app.log("AudioManager", "Music only is disabled");
            }
            if (backgroundMusic == null) {
                Gdx.app.log("AudioManager", "Background music file not loaded");
            }
        }
    }

    public void stopBackgroundMusic() {
        if (backgroundMusic != null && backgroundMusic.isPlaying()) {
            backgroundMusic.stop();
        }
    }

    public void playGameOverMusic() {
        stopBackgroundMusic();
        if (allSoundEnabled && musicOnlyEnabled && gameOverMusic != null) {
            gameOverMusic.setVolume(musicVolume * 0.5f);
            gameOverMusic.play();
        }
    }

    public void stopGameOverMusic() {
        if (gameOverMusic != null && gameOverMusic.isPlaying()) {
            gameOverMusic.stop();
        }
    }

    public void pauseMusic() {
        if (backgroundMusic != null && backgroundMusic.isPlaying()) {
            backgroundMusic.pause();
        }
    }

    public void resumeMusic() {
        if (allSoundEnabled && musicOnlyEnabled && backgroundMusic != null && !backgroundMusic.isPlaying()) {
            backgroundMusic.play();
        }
    }

    // New control methods
    public void setAllSoundEnabled(boolean enabled) {
        this.allSoundEnabled = enabled;
        if (!enabled) {
            // Tắt tất cả âm thanh
            stopBackgroundMusic();
            stopGameOverMusic();
        } else {
            // Bật lại nhạc nền nếu musicOnly cũng được bật
            if (musicOnlyEnabled) {
                playBackgroundMusic();
            }
        }
        Gdx.app.log("AudioManager", "All sound " + (enabled ? "enabled" : "disabled"));
    }

    public void setMusicOnlyEnabled(boolean enabled) {
        this.musicOnlyEnabled = enabled;
        if (allSoundEnabled) { // Chỉ áp dụng khi tổng âm thanh được bật
            if (!enabled) {
                stopBackgroundMusic();
                stopGameOverMusic();
            } else {
                playBackgroundMusic();
            }
        }
        Gdx.app.log("AudioManager", "Music only " + (enabled ? "enabled" : "disabled"));
    }

    // Legacy methods for backward compatibility
    public void setSoundEnabled(boolean enabled) {
        setAllSoundEnabled(enabled);
    }

    public void setMusicEnabled(boolean enabled) {
        setMusicOnlyEnabled(enabled);
    }

    public void setSoundVolume(float volume) {
        this.soundVolume = Math.max(0f, Math.min(1f, volume));
    }

    public void setMusicVolume(float volume) {
        this.musicVolume = Math.max(0f, Math.min(1f, volume));
        if (backgroundMusic != null) {
            backgroundMusic.setVolume(musicVolume * 0.3f);
        }
        if (gameOverMusic != null) {
            gameOverMusic.setVolume(musicVolume * 0.5f);
        }
    }

    // Updated getters
    public boolean isAllSoundEnabled() {
        return allSoundEnabled;
    }

    public boolean isMusicOnlyEnabled() {
        return musicOnlyEnabled;
    }

    // Legacy getters for backward compatibility
    public boolean isSoundEnabled() {
        return allSoundEnabled;
    }

    public boolean isMusicEnabled() {
        return musicOnlyEnabled;
    }

    public float getSoundVolume() {
        return soundVolume;
    }

    public float getMusicVolume() {
        return musicVolume;
    }

    public void dispose() {
        if (laserSound != null) laserSound.dispose();
        if (borderSound != null) borderSound.dispose();
        if (explosionSound != null) explosionSound.dispose();
        if (gameOverSound != null) gameOverSound.dispose();
        if (backgroundMusic != null) backgroundMusic.dispose();
        if (gameOverMusic != null) gameOverMusic.dispose();
    }
}
