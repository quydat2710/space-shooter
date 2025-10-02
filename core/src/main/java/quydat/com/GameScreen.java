package quydat.com;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import quydat.com.Bomb;
import quydat.com.Missile;
import com.badlogic.gdx.math.Vector2;  // Cho homing
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class GameScreen implements Screen {
    private final MyGame game;
    private Stage stage;
    private OrthographicCamera camera;
    private FitViewport viewport;
    private SpaceShip spaceShip;
    private Array<UFO> ufos;
    private Array<Meteor> meteors;
    private Array<Shield> shields;
    private long lastUfoTime;
    private long lastMeteorTime;
    private long lastShieldTime;
    private int score = 0;
    private int shieldCount = 1; // Number of shield protections (max 3)
    private BitmapFont font;
    private BitmapFont titleFont;
    private BitmapFont smallFont;
    private SpriteBatch fontBatch;
    private Texture backgroundTexture;
    private Sprite backgroundSprite1;
    private Sprite backgroundSprite2;
    private float backgroundY1;
    private float backgroundY2;
    private float backgroundSpeed = 100f;
    private Touchpad touchpad;
    private TextButton shootButton;
    private Skin skin;
    private boolean uiLoaded = false;
    private boolean gameOver = false;
    private Stage gameOverStage;
    private Table gameOverTable;
    private TextButton playAgainButton;
    private static final float VIRTUAL_WIDTH = 1920f;
    private static final float VIRTUAL_HEIGHT = 1080f;

    private Music backgroundMusic;
    private Music gameOverMusic;

    private TextButton soundButton;
    private TextButton musicButton;

    private Array<Bomb> bombs;
    private Array<Missile> missiles;
    private Array<Trap> traps;  // Mới: Danh sách traps
    private long lastBombTime;
    private long lastMissileTime;
    private long lastTrapTime;
    private static final long BOMB_COOLDOWN = 3000000000L;  // 3 giây
    private static final long MISSILE_COOLDOWN = 2000000000L;  // 2 giây
    private static final long TRAP_COOLDOWN = 5000000000L;  // 5 giây cho trap

    private TextButton shootBombButton;
    private TextButton shootMissileButton;
    private TextButton shootTrapButton;  // Mới: Nút cho trap

    private ShapeRenderer shapeRenderer;

    private long startTime; // Thời gian bắt đầu game

    public GameScreen(MyGame game) {
        this.game = game;
        this.startTime = TimeUtils.millis();
        create();
    }

    private void create() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        stage = new Stage(viewport);
        camera.position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);

        bombs = new Array<>();
        missiles = new Array<>();
        traps = new Array<>();  // Mới: Khởi tạo traps
        lastBombTime = TimeUtils.nanoTime();
        lastMissileTime = TimeUtils.nanoTime();
        lastTrapTime = TimeUtils.nanoTime();

        // Tạo InputMultiplexer để xử lý cả keyboard và touch
        com.badlogic.gdx.InputMultiplexer multiplexer = new com.badlogic.gdx.InputMultiplexer();
        multiplexer.addProcessor(stage);
        multiplexer.addProcessor(new com.badlogic.gdx.InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                return false;
            }
        });
        Gdx.input.setInputProcessor(multiplexer);

        // Khởi tạo phi thuyền
        float shipSize = 64 * Math.min(Gdx.graphics.getWidth() / VIRTUAL_WIDTH, Gdx.graphics.getHeight() / VIRTUAL_HEIGHT);
        spaceShip = new SpaceShip(VIRTUAL_WIDTH / 2 - shipSize/2, 150, stage);
        stage.addActor(spaceShip);

        // Khởi tạo danh sách UFO, thiên thạch và khiên
        ufos = new Array<>();
        meteors = new Array<>();
        shields = new Array<>();
        spawnUfo();
        spawnMeteor();
        spawnShield();

        // Khởi tạo fonts (chỉnh scale lớn hơn)
        font = new BitmapFont();
        float screenScale = Math.min(Gdx.graphics.getWidth() / VIRTUAL_WIDTH, Gdx.graphics.getHeight() / VIRTUAL_HEIGHT);
        font.getData().setScale(2.0f * screenScale);  // Tăng từ 1.8f lên 2.0f
        titleFont = new BitmapFont();
        titleFont.getData().setScale(4.5f * screenScale);  // Tăng từ 4.0f lên 4.5f
        smallFont = new BitmapFont();
        smallFont.getData().setScale(1.8f * screenScale);  // Tăng từ 1.2f lên 1.4f
        fontBatch = new SpriteBatch();

        // Khởi tạo hình nền
        try {
            backgroundTexture = new Texture(Gdx.files.internal("hinh-nen-vu-tru.jpg"));
            backgroundSprite1 = new Sprite(backgroundTexture);
            backgroundSprite2 = new Sprite(backgroundTexture);
            backgroundSprite1.setSize(VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
            backgroundSprite2.setSize(VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
            backgroundY1 = 0;
            backgroundY2 = VIRTUAL_HEIGHT;
            backgroundSprite1.setPosition(0, backgroundY1);
            backgroundSprite2.setPosition(0, backgroundY2);
        } catch (Exception e) {
            Gdx.app.error("Main", "Error loading background texture: " + e.getMessage());
        }

        // Tạo UI Controls
        createUIControls();

        // Tạo và phát nhạc nền
        AudioManager.getInstance().playBackgroundMusic();

        // Khởi tạo ShapeRenderer
        shapeRenderer = new ShapeRenderer();
    }

    private void createUIControls() {
        skin = new Skin();
        try {
            Texture joystickBg, joystickKnob, shootBtn, bombBtn, missileBtn, trapBtn;
            try {
                joystickBg = new Texture(Gdx.files.internal("joystick_background.png"));
            } catch (Exception e) {
                joystickBg = createCircleTexture(80, new Color(0.3f, 0.3f, 0.3f, 0.8f));
                Gdx.app.log("Main", "Created default joystick background");
            }
            try {
                joystickKnob = new Texture(Gdx.files.internal("joystick_knob.png"));
            } catch (Exception e) {
                joystickKnob = createCircleTexture(35, new Color(0.8f, 0.8f, 0.8f, 0.9f));
                Gdx.app.log("Main", "Created default joystick knob");
            }
            try {
                shootBtn = new Texture(Gdx.files.internal("shoot_button.png"));
            } catch (Exception e) {
                shootBtn = createCircleTexture(60, new Color(1.0f, 0.2f, 0.2f, 0.9f));
                Gdx.app.log("Main", "Created default shoot button");
            }
            try {
                bombBtn = new Texture(Gdx.files.internal("bomb.png"));
            } catch (Exception e) {
                bombBtn = createCircleTexture(60, new Color(0.2f, 1.0f, 0.2f, 0.9f));  // Xanh lá
            }
            try {
                missileBtn = new Texture(Gdx.files.internal("missile.png"));
            } catch (Exception e) {
                missileBtn = createCircleTexture(60, new Color(0.2f, 0.2f, 1.0f, 0.9f));  // Xanh dương
            }
            try {
                trapBtn = new Texture(Gdx.files.internal("trap.png"));
            } catch (Exception e) {
                trapBtn = createCircleTexture(60, new Color(1.0f, 0.5f, 0.0f, 0.9f));  // Màu cam cho trap
            }
            skin.add("bomb_button", bombBtn);
            skin.add("missile_button", missileBtn);
            skin.add("trap_button", trapBtn);  // Mới: Thêm skin cho trap
            skin.add("joystick_background", joystickBg);
            skin.add("joystick_knob", joystickKnob);
            skin.add("shoot_button", shootBtn);

            float screenWidth = viewport.getWorldWidth();
            float screenHeight = viewport.getWorldHeight();
            float margin = screenWidth * 0.08f;

            Touchpad.TouchpadStyle touchpadStyle = new Touchpad.TouchpadStyle();
            touchpadStyle.background = skin.getDrawable("joystick_background");
            touchpadStyle.knob = skin.getDrawable("joystick_knob");
            touchpad = new Touchpad(15, touchpadStyle);
            float joystickSize = Math.min(screenWidth, screenHeight) * 0.18f;
            touchpad.setBounds(margin, margin, joystickSize, joystickSize);
            stage.addActor(touchpad);

            float buttonSize = joystickSize * 0.6f;  // Giảm kích thước nút nhỏ hơn (từ 0.85f xuống 0.6f)
            float buttonSpacing = buttonSize * 0.05f;  // Giảm spacing nhỏ hơn

            // Sắp xếp nút thành hình vuông 2x2 ở góc phải dưới
            // Dưới trái: shoot, dưới phải: bomb
            // Trên trái: missile, trên phải: trap

            // Shoot button (dưới trái)
            TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
            buttonStyle.up = skin.getDrawable("shoot_button");
            buttonStyle.font = font;
            shootButton = new TextButton("", buttonStyle);
            shootButton.setBounds(screenWidth - margin - 2 * buttonSize - buttonSpacing, margin, buttonSize, buttonSize);
            shootButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    spaceShip.shootLaser();
                    Gdx.app.log("Main", "Shoot button pressed!");
                }
            });
            stage.addActor(shootButton);

            // Bomb button (dưới phải)
            TextButton.TextButtonStyle bombStyle = new TextButton.TextButtonStyle();
            bombStyle.up = skin.getDrawable("bomb_button");
            bombStyle.font = font;
            shootBombButton = new TextButton("", bombStyle);
            shootBombButton.setBounds(screenWidth - margin - buttonSize, margin, buttonSize, buttonSize);
            shootBombButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    shootBomb();
                }
            });
            stage.addActor(shootBombButton);

            // Missile button (trên trái)
            TextButton.TextButtonStyle missileStyle = new TextButton.TextButtonStyle();
            missileStyle.up = skin.getDrawable("missile_button");
            missileStyle.font = font;
            shootMissileButton = new TextButton("", missileStyle);
            shootMissileButton.setBounds(screenWidth - margin - 2 * buttonSize - buttonSpacing, margin + buttonSize + buttonSpacing, buttonSize, buttonSize);
            shootMissileButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    shootMissile();
                }
            });
            stage.addActor(shootMissileButton);

            // Trap button (trên phải)
            TextButton.TextButtonStyle trapStyle = new TextButton.TextButtonStyle();
            trapStyle.up = skin.getDrawable("trap_button");
            trapStyle.font = font;
            shootTrapButton = new TextButton("", trapStyle);
            shootTrapButton.setBounds(screenWidth - margin - buttonSize, margin + buttonSize + buttonSpacing, buttonSize, buttonSize);
            shootTrapButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    shootTrap();
                }
            });
            stage.addActor(shootTrapButton);

            createAudioButtons();
            uiLoaded = true;
            Gdx.app.log("Main", "UI loaded successfully!");
        } catch (Exception e) {
            Gdx.app.error("Main", "Error creating UI controls: " + e.getMessage());
            uiLoaded = false;
        }
    }

    private void shootBomb() {
        if (TimeUtils.nanoTime() - lastBombTime > BOMB_COOLDOWN) {
            float x = spaceShip.getX() + spaceShip.getWidth() / 2 - 16;
            float y = spaceShip.getY() + spaceShip.getHeight();
            Bomb bomb = new Bomb(x, y, stage);
            bombs.add(bomb);
            stage.addActor(bomb);
            lastBombTime = TimeUtils.nanoTime();
        }
    }

    private void shootMissile() {
        if (TimeUtils.nanoTime() - lastMissileTime > MISSILE_COOLDOWN) {
            float x = spaceShip.getX() + spaceShip.getWidth() / 2 - 16;
            float y = spaceShip.getY() + spaceShip.getHeight();
            // Tìm UFO gần nhất làm mục tiêu (có thể trả về null)
            UFO target = findNearestUFO(x, y);
            Missile missile = new Missile(x, y, stage, target); // dùng constructor 4 param
            missiles.add(missile);
            stage.addActor(missile);
            lastMissileTime = TimeUtils.nanoTime();
        }
    }

    private void shootTrap() {
        if (TimeUtils.nanoTime() - lastTrapTime > TRAP_COOLDOWN) {
            float x = spaceShip.getX() + spaceShip.getWidth() / 2 - 32;  // Giả sử trap size 64x64
            float y = spaceShip.getY() + spaceShip.getHeight() / 2 - 32;
            Trap trap = new Trap(x, y, stage);
            traps.add(trap);
            stage.addActor(trap);
            lastTrapTime = TimeUtils.nanoTime();
        }
    }

    private UFO findNearestUFO(float x, float y) {
        UFO nearest = null;
        float minDist = Float.MAX_VALUE;
        Vector2 missilePos = new Vector2(x, y);
        for (UFO ufo : ufos) {
            Vector2 ufoPos = new Vector2(ufo.getX() + ufo.getWidth() / 2, ufo.getY() + ufo.getHeight() / 2);
            float dist = missilePos.dst(ufoPos);
            if (dist < minDist) {
                minDist = dist;
                nearest = ufo;
            }
        }
        return nearest;
    }

    private void createAudioButtons() {
        try {
            Texture soundOnTexture, soundOffTexture, musicOnTexture, musicOffTexture;
            try {
                soundOnTexture = new Texture(Gdx.files.internal("sound_on.png"));
            } catch (Exception e) {
                soundOnTexture = createCircleTexture(40, new Color(0.2f, 0.8f, 0.2f, 0.9f));
                Gdx.app.log("GameScreen", "Created default sound_on texture");
            }
            try {
                soundOffTexture = new Texture(Gdx.files.internal("sound_off.png"));
            } catch (Exception e) {
                soundOffTexture = createCircleTexture(40, new Color(0.8f, 0.2f, 0.2f, 0.9f));
                Gdx.app.log("GameScreen", "Created default sound_off texture");
            }
            try {
                musicOnTexture = new Texture(Gdx.files.internal("music_on.png"));
            } catch (Exception e) {
                musicOnTexture = createCircleTexture(40, new Color(0.2f, 0.2f, 0.8f, 0.9f));
                Gdx.app.log("GameScreen", "Created default music_on texture");
            }
            try {
                musicOffTexture = new Texture(Gdx.files.internal("music_off.png"));
            } catch (Exception e) {
                musicOffTexture = createCircleTexture(40, new Color(0.6f, 0.6f, 0.6f, 0.9f));
                Gdx.app.log("GameScreen", "Created default music_off texture");
            }
            skin.add("sound_on", soundOnTexture);
            skin.add("sound_off", soundOffTexture);
            skin.add("music_on", musicOnTexture);
            skin.add("music_off", musicOffTexture);

            float screenWidth = viewport.getWorldWidth();
            float screenHeight = viewport.getWorldHeight();
            float margin = screenWidth * 0.02f;
            float buttonSize = 80f;
            float buttonSpacing = buttonSize * 0.1f;  // Khoảng cách giữa 2 nút

            // Sound button (bên trái)
            TextButton.TextButtonStyle soundStyle = new TextButton.TextButtonStyle();
            soundStyle.up = skin.getDrawable(AudioManager.getInstance().isSoundEnabled() ? "sound_on" : "sound_off");
            soundStyle.font = smallFont;
            soundButton = new TextButton("S", soundStyle);
            soundButton.setBounds(screenWidth - margin - 2 * buttonSize - buttonSpacing, screenHeight - margin - buttonSize, buttonSize, buttonSize);
            soundButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    toggleSound();
                }
            });
            stage.addActor(soundButton);

            // Music button (bên phải)
            TextButton.TextButtonStyle musicStyle = new TextButton.TextButtonStyle();
            musicStyle.up = skin.getDrawable(AudioManager.getInstance().isMusicEnabled() ? "music_on" : "music_off");
            musicStyle.font = smallFont;
            musicButton = new TextButton("M", musicStyle);
            musicButton.setBounds(screenWidth - margin - buttonSize, screenHeight - margin - buttonSize, buttonSize, buttonSize);
            musicButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    toggleMusic();
                }
            });
            stage.addActor(musicButton);
        } catch (Exception e) {
            Gdx.app.error("GameScreen", "Error creating audio buttons: " + e.getMessage());
        }
    }

    private void toggleSound() {
        AudioManager audioManager = AudioManager.getInstance();
        boolean newState = !audioManager.isSoundEnabled();
        audioManager.setSoundEnabled(newState);
        soundButton.getStyle().up = skin.getDrawable(newState ? "sound_on" : "sound_off");
        if (newState) {
            audioManager.playLaserSound();
        }
        Gdx.app.log("GameScreen", "Sound " + (newState ? "enabled" : "disabled"));
    }

    private void toggleMusic() {
        AudioManager audioManager = AudioManager.getInstance();
        boolean newState = !audioManager.isMusicEnabled();
        audioManager.setMusicEnabled(newState);
        musicButton.getStyle().up = skin.getDrawable(newState ? "music_on" : "music_off");
        if (newState && !gameOver) {
            audioManager.playBackgroundMusic();
        } else {
            audioManager.stopBackgroundMusic();
        }
        Gdx.app.log("GameScreen", "Music " + (newState ? "enabled" : "disabled"));
    }

    private Texture createCircleTexture(int baseRadius, Color color) {
        float screenScale = Math.min(Gdx.graphics.getWidth() / VIRTUAL_WIDTH, Gdx.graphics.getHeight() / VIRTUAL_HEIGHT);
        int radius = (int)(baseRadius * screenScale * 1.5f);
        int size = radius * 2;
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(size, size, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(0, 0, 0, 0);
        pixmap.fill();
        pixmap.setColor(color.r * 0.5f, color.g * 0.5f, color.b * 0.5f, color.a);
        pixmap.fillCircle(radius, radius, radius);
        pixmap.setColor(color);
        pixmap.fillCircle(radius, radius, Math.max(radius - 4, 1));
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);
        camera.update();
        stage.getBatch().setProjectionMatrix(camera.combined);

        // Cập nhật và vẽ nền
        if (backgroundSprite1 != null && backgroundSprite2 != null && !gameOver) {
            float deltaTime = Gdx.graphics.getDeltaTime();
            backgroundY1 -= backgroundSpeed * deltaTime;
            backgroundY2 -= backgroundSpeed * deltaTime;
            if (backgroundY1 <= -VIRTUAL_HEIGHT) {
                backgroundY1 = VIRTUAL_HEIGHT;
            }
            if (backgroundY2 <= -VIRTUAL_HEIGHT) {
                backgroundY2 = VIRTUAL_HEIGHT;
            }
            backgroundSprite1.setPosition(0, backgroundY1);
            backgroundSprite2.setPosition(0, backgroundY2);
            stage.getBatch().begin();
            backgroundSprite1.draw(stage.getBatch());
            backgroundSprite2.draw(stage.getBatch());
            stage.getBatch().end();
        }

        if (!gameOver) {
            handleInput();
            stage.act();
            stage.draw();

            // Vẽ vòng tròn bảo vệ nếu có shield
            if (shieldCount > 0) {
                shapeRenderer.setProjectionMatrix(camera.combined);
                shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                shapeRenderer.setColor(Color.CYAN);
                float radius = Math.max(spaceShip.getWidth(), spaceShip.getHeight()) / 2 + 20; // Bán kính vòng tròn
                shapeRenderer.circle(spaceShip.getX() + spaceShip.getWidth() / 2, spaceShip.getY() + spaceShip.getHeight() / 2, radius);
                shapeRenderer.end();
            }
        } else if (gameOverStage != null) {
            gameOverStage.act();
            gameOverStage.draw();
        }

        // Vẽ UI text
        fontBatch.setProjectionMatrix(camera.combined);
        fontBatch.begin();
        if (!gameOver) {
            float margin = viewport.getWorldWidth() * 0.02f;
            font.setColor(Color.BLACK);
            font.draw(fontBatch, "Score: " + score, margin + 2, viewport.getWorldHeight() - margin - 2);
            font.setColor(Color.WHITE);
            font.draw(fontBatch, "Score: " + score, margin, viewport.getWorldHeight() - margin);
            // Hiển thị số lượng khiên bảo vệ
            font.setColor(Color.BLACK);
            font.draw(fontBatch, "Shields: " + shieldCount, margin + 2, viewport.getWorldHeight() - margin * 2 - 2);
            font.setColor(Color.CYAN);
            font.draw(fontBatch, "Shields: " + shieldCount, margin, viewport.getWorldHeight() - margin * 2);
            String controlText;
            if (Gdx.app.getType() == com.badlogic.gdx.Application.ApplicationType.Desktop) {
                controlText = "WASD/Arrow Keys: Move | Space: Shoot | ESC: Exit";
            } else {
                controlText = uiLoaded ? "Joystick: Move | Red Button: Shoot" : "Arrow Keys: Move | Space: Shoot";
            }
            font.setColor(Color.BLACK);
            font.draw(fontBatch, controlText, margin + 2, viewport.getWorldHeight() - margin * 4 - 2);
            font.setColor(Color.CYAN);
            font.draw(fontBatch, controlText, margin, viewport.getWorldHeight() - margin * 4);
            if (uiLoaded && Gdx.app.getLogLevel() == Application.LOG_DEBUG) {
                font.setColor(Color.YELLOW);
                font.draw(fontBatch, "Touch: X=" + String.format("%.2f", touchpad.getKnobPercentX()) +
                        " Y=" + String.format("%.2f", touchpad.getKnobPercentY()),
                    margin, viewport.getWorldHeight() - margin * 7);
            }
            float creditsMargin = viewport.getWorldWidth() * 0.02f;
            smallFont.setColor(Color.BLACK);
            smallFont.draw(fontBatch, "Made by KMA", viewport.getWorldWidth() - creditsMargin - 98, creditsMargin + 2);
            smallFont.setColor(Color.GRAY);
            smallFont.draw(fontBatch, "Made by KMA", viewport.getWorldWidth() - creditsMargin - 100, creditsMargin);
        }
        fontBatch.end();

        // Spawn UFO, Meteor, và Shield
        if (!gameOver) {
            if (TimeUtils.nanoTime() - lastUfoTime > 1000000000) {
                spawnUfo();
            }
            if (TimeUtils.nanoTime() - lastMeteorTime > 1500000000) {
                spawnMeteor();
            }
            if (TimeUtils.nanoTime() - lastShieldTime > MathUtils.random(3000000000L, 4000000000L)) {
                spawnShield();
            }
        }

        // Cập nhật countdown cho nút bomb, missile và trap
        if (uiLoaded && !gameOver) {
            updateButtonCountdowns();
        }

        checkCollisions();
        checkWinCondition();
    }

    private void checkWinCondition() {
        if (score >= 100 && !gameOver) {
            long endTime = TimeUtils.millis();
            long gameDuration = endTime - startTime;
            AudioManager.getInstance().stopBackgroundMusic();
            dispose();
            game.setScreen(new WinScreen(game, score, gameDuration));
        }
    }

    private void updateButtonCountdowns() {
        // Bomb button
        long bombTimeLeftNano = BOMB_COOLDOWN - (TimeUtils.nanoTime() - lastBombTime);
        if (bombTimeLeftNano > 0) {
            int bombSecondsLeft = (int) Math.ceil(bombTimeLeftNano / 1000000000.0);
            shootBombButton.setText(String.valueOf(bombSecondsLeft));
        } else {
            shootBombButton.setText("");
        }

        // Missile button
        long missileTimeLeftNano = MISSILE_COOLDOWN - (TimeUtils.nanoTime() - lastMissileTime);
        if (missileTimeLeftNano > 0) {
            int missileSecondsLeft = (int) Math.ceil(missileTimeLeftNano / 1000000000.0);
            shootMissileButton.setText(String.valueOf(missileSecondsLeft));
        } else {
            shootMissileButton.setText("");
        }

        // Trap button (Mới)
        long trapTimeLeftNano = TRAP_COOLDOWN - (TimeUtils.nanoTime() - lastTrapTime);
        if (trapTimeLeftNano > 0) {
            int trapSecondsLeft = (int) Math.ceil(trapTimeLeftNano / 1000000000.0);
            shootTrapButton.setText(String.valueOf(trapSecondsLeft));
        } else {
            shootTrapButton.setText("");
        }
    }

    private void handleInput() {
        if (!gameOver) {
            float delta = Gdx.graphics.getDeltaTime();
            float moveSpeed = 400f;
            float dx = 0, dy = 0;
            boolean shouldShoot = false;
            boolean useKeyboard = (Gdx.app.getType() == com.badlogic.gdx.Application.ApplicationType.Desktop) || !uiLoaded;

            if (!useKeyboard && uiLoaded && touchpad != null) {
                float knobX = touchpad.getKnobPercentX();
                float knobY = touchpad.getKnobPercentY();
                if (Math.abs(knobX) > 0.05f || Math.abs(knobY) > 0.05f) {
                    dx = moveSpeed * knobX * Math.abs(knobX);
                    dy = moveSpeed * knobY * Math.abs(knobY);
                }
                if (shootButton != null && shootButton.isPressed()) {
                    shouldShoot = true;
                }
            } else {
                if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
                    dx = -moveSpeed;
                }
                if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
                    dx = moveSpeed;
                }
                if (Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W)) {
                    dy = moveSpeed;
                }
                if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S)) {
                    dy = -moveSpeed;
                }
                if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                    shouldShoot = true;
                }
                if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                    Gdx.app.exit();
                }
                if (Gdx.input.isKeyJustPressed(Input.Keys.B)) {
                    shootBomb();
                }
                if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
                    shootMissile();
                }
                if (Gdx.input.isKeyJustPressed(Input.Keys.T)) {  // Giả sử phím T cho trap
                    shootTrap();
                }
            }
            if (dx != 0 || dy != 0) {
                spaceShip.moveByWithSpeed(dx, dy, delta);
            }
            if (shouldShoot) {
                spaceShip.shootLaser();
            }
        }
    }

    private void spawnUfo() {
        if (!gameOver) {
            float ufoSize = 64 * Math.min(Gdx.graphics.getWidth() / VIRTUAL_WIDTH, Gdx.graphics.getHeight() / VIRTUAL_HEIGHT);
            float x = MathUtils.random(0, VIRTUAL_WIDTH - ufoSize);
            float y = VIRTUAL_HEIGHT;
            UFO ufo = new UFO(x, y, stage); // Tăng tốc độ UFO
            ufos.add(ufo);
            stage.addActor(ufo);
            lastUfoTime = TimeUtils.nanoTime();
        }
    }

    private void spawnMeteor() {
        if (!gameOver) {
            float meteorSize = 64 * Math.min(Gdx.graphics.getWidth() / VIRTUAL_WIDTH, Gdx.graphics.getHeight() / VIRTUAL_HEIGHT);
            float x = MathUtils.random(0, VIRTUAL_WIDTH - meteorSize);
            float y = VIRTUAL_HEIGHT;
            Meteor meteor = new Meteor(x, y, stage);
            meteors.add(meteor);
            stage.addActor(meteor);
            lastMeteorTime = TimeUtils.nanoTime();
        }
    }

    private void spawnShield() {
        if (!gameOver) {
            float shieldSize = 64 * Math.min(Gdx.graphics.getWidth() / VIRTUAL_WIDTH, Gdx.graphics.getHeight() / VIRTUAL_HEIGHT);
            float x = MathUtils.random(0, VIRTUAL_WIDTH - shieldSize);
            float y = VIRTUAL_HEIGHT;
            Shield shield = new Shield(x, y, stage);
            shields.add(shield);
            stage.addActor(shield);
            lastShieldTime = TimeUtils.nanoTime();
        }
    }

    private void checkCollisions() {
        if (!gameOver) {
            // Kiểm tra va chạm với UFO
            for (int i = ufos.size - 1; i >= 0; i--) {
                UFO ufo = ufos.get(i);
                Rectangle ufoBounds = new Rectangle(ufo.getX(), ufo.getY(), ufo.getWidth(), ufo.getHeight());
                Rectangle shipBounds = new Rectangle(spaceShip.getX(), spaceShip.getY(), spaceShip.getWidth(), spaceShip.getHeight());
                if (ufoBounds.overlaps(shipBounds)) {
                    if (shieldCount > 0) {
                        shieldCount--;
                        ufo.destroy();
                        ufos.removeIndex(i);
                        AudioManager.getInstance().playExplosionSound();
                    } else {
                        gameOver = true;
                        showGameOverScreen();
                        return;
                    }
                }
                Array<laser> lasers = spaceShip.getLasers();
                for (int j = lasers.size - 1; j >= 0; j--) {
                    laser laser = lasers.get(j);
                    Rectangle laserBounds = new Rectangle(laser.getX(), laser.getY(), laser.getWidth(), laser.getHeight());
                    if (laserBounds.overlaps(ufoBounds)) {
                        AudioManager.getInstance().playExplosionSound();
                        ufo.destroy();
                        ufos.removeIndex(i);
                        laser.remove();
                        lasers.removeIndex(j);
                        score += 10;
                        break;
                    }
                }
                if (i < ufos.size && ufo.getY() + ufo.getHeight() < 0) {
                    ufo.remove();
                    ufos.removeIndex(i);
                }
            }
            // Kiểm tra va chạm với thiên thạch
            for (int i = meteors.size - 1; i >= 0; i--) {
                Meteor meteor = meteors.get(i);
                Rectangle meteorBounds = new Rectangle(meteor.getX(), meteor.getY(), meteor.getWidth(), meteor.getHeight());
                Rectangle shipBounds = new Rectangle(spaceShip.getX(), spaceShip.getY(), spaceShip.getWidth(), spaceShip.getHeight());
                if (meteorBounds.overlaps(shipBounds)) {
                    if (shieldCount > 0) {
                        shieldCount--;
                        meteor.destroy();
                        meteors.removeIndex(i);
                        AudioManager.getInstance().playExplosionSound();
                    } else {
                        gameOver = true;
                        showGameOverScreen();
                        return;
                    }
                }
                Array<laser> lasers = spaceShip.getLasers();
                for (int j = lasers.size - 1; j >= 0; j--) {
                    laser laser = lasers.get(j);
                    Rectangle laserBounds = new Rectangle(laser.getX(), laser.getY(), laser.getWidth(), laser.getHeight());
                    if (laserBounds.overlaps(meteorBounds)) {
                        AudioManager.getInstance().playExplosionSound();
                        meteor.destroy();
                        meteors.removeIndex(i);
                        laser.remove();
                        lasers.removeIndex(j);
                        score += 5;
                        break;
                    }
                }
                if (i < meteors.size && meteor.getY() + meteor.getHeight() < 0) {
                    meteor.remove();
                    meteors.removeIndex(i);
                }
            }
            // Kiểm tra va chạm với khiên
            for (int i = shields.size - 1; i >= 0; i--) {
                Shield shield = shields.get(i);
                Rectangle shieldBounds = new Rectangle(shield.getX(), shield.getY(), shield.getWidth(), shield.getHeight());
                Rectangle shipBounds = new Rectangle(spaceShip.getX(), spaceShip.getY(), spaceShip.getWidth(), spaceShip.getHeight());
                if (shieldBounds.overlaps(shipBounds)) {
                    shieldCount = Math.min(shieldCount + 1, 3);
                    shield.remove();
                    shields.removeIndex(i);
                }
                if (i < shields.size && shield.getY() + shield.getHeight() < 0) {
                    shield.remove();
                    shields.removeIndex(i);
                }
            }

            // Bomb collisions (nổ vùng rộng)
            for (int j = bombs.size - 1; j >= 0; j--) {
                Bomb bomb = bombs.get(j);
                Rectangle bombBounds = new Rectangle(bomb.getX(), bomb.getY(), bomb.getWidth(), bomb.getHeight());
                boolean exploded = false;

                // Kiểm tra UFO
                for (int i = ufos.size - 1; i >= 0; i--) {
                    UFO ufo = ufos.get(i);
                    Rectangle ufoBounds = new Rectangle(ufo.getX(), ufo.getY(), ufo.getWidth(), ufo.getHeight());
                    if (bombBounds.overlaps(ufoBounds)) {
                        exploded = true;
                        ufo.destroy();
                        ufos.removeIndex(i);
                        score += 10;
                    }
                }

                // Kiểm tra Meteor
                for (int i = meteors.size - 1; i >= 0; i--) {
                    Meteor meteor = meteors.get(i);
                    Rectangle meteorBounds = new Rectangle(meteor.getX(), meteor.getY(), meteor.getWidth(), meteor.getHeight());
                    if (bombBounds.overlaps(meteorBounds)) {
                        exploded = true;
                        meteor.destroy();
                        meteors.removeIndex(i);
                        score += 5;
                    }
                }

                if (exploded) {
                    // Nổ vùng rộng: Trigger blast
                    triggerExplosion(bomb.getX(), bomb.getY());
                    bomb.remove();
                    bombs.removeIndex(j);
                } else if (bomb.getY() > VIRTUAL_HEIGHT) {  // Xóa bomb nếu bay ra ngoài màn hình
                    bomb.remove();
                    bombs.removeIndex(j);
                }
            }

            // Missile collisions (với homing)
            for (int j = missiles.size - 1; j >= 0; j--) {
                Missile missile = missiles.get(j);
                Rectangle missileBounds = new Rectangle(missile.getX(), missile.getY(), missile.getWidth(), missile.getHeight());
                boolean hit = false;

                // Kiểm tra UFO
                for (int i = ufos.size - 1; i >= 0; i--) {
                    UFO ufo = ufos.get(i);
                    Rectangle ufoBounds = new Rectangle(ufo.getX(), ufo.getY(), ufo.getWidth(), ufo.getHeight());
                    if (missileBounds.overlaps(ufoBounds)) {
                        hit = true;
                        AudioManager.getInstance().playExplosionSound();
                        ufo.destroy();
                        ufos.removeIndex(i);
                        score += 15;
                        break;
                    }
                }

                // Kiểm tra Meteor nếu chưa hit UFO
                if (!hit) {
                    for (int i = meteors.size - 1; i >= 0; i--) {
                        Meteor meteor = meteors.get(i);
                        Rectangle meteorBounds = new Rectangle(meteor.getX(), meteor.getY(), meteor.getWidth(), meteor.getHeight());
                        if (missileBounds.overlaps(meteorBounds)) {
                            hit = true;
                            AudioManager.getInstance().playExplosionSound();
                            meteor.destroy();
                            meteors.removeIndex(i);
                            score += 10;
                            break;
                        }
                    }
                }

                if (hit) {
                    missile.remove();
                    missiles.removeIndex(j);
                } else if (missile.getY() > VIRTUAL_HEIGHT) {  // Xóa missile nếu bay ra ngoài màn hình
                    missile.remove();
                    missiles.removeIndex(j);
                }
            }

            // Trap collisions (Mới: Kiểm tra va chạm với trap)
            for (int j = traps.size - 1; j >= 0; j--) {
                Trap trap = traps.get(j);
                Rectangle trapBounds = new Rectangle(trap.getX(), trap.getY(), trap.getWidth(), trap.getHeight());
                boolean exploded = false;

                // Kiểm tra UFO
                for (int i = ufos.size - 1; i >= 0; i--) {
                    UFO ufo = ufos.get(i);
                    Rectangle ufoBounds = new Rectangle(ufo.getX(), ufo.getY(), ufo.getWidth(), ufo.getHeight());
                    if (trapBounds.overlaps(ufoBounds)) {
                        exploded = true;
                        ufo.destroy();
                        ufos.removeIndex(i);
                        score += 10;
                    }
                }

                // Kiểm tra Meteor
                for (int i = meteors.size - 1; i >= 0; i--) {
                    Meteor meteor = meteors.get(i);
                    Rectangle meteorBounds = new Rectangle(meteor.getX(), meteor.getY(), meteor.getWidth(), meteor.getHeight());
                    if (trapBounds.overlaps(meteorBounds)) {
                        exploded = true;
                        meteor.destroy();
                        meteors.removeIndex(i);
                        score += 5;
                    }
                }

                if (exploded) {
                    triggerExplosion(trap.getX(), trap.getY());
                    trap.remove();
                    traps.removeIndex(j);
                }
            }
        }
    }

    private void triggerExplosion(float x, float y) {
        // Tạo explosion với blast.png, thêm vào stage
        Explosion explosion = new Explosion(x, y);
        stage.addActor(explosion);

        AudioManager.getInstance().playExplosionSound();
        // Logic nổ vùng: Kiểm tra kẻ địch gần (bán kính 100) và phá hủy
        float blastRadius = 100f;
        for (int i = ufos.size - 1; i >= 0; i--) {
            UFO ufo = ufos.get(i);
            if (Vector2.dst(x, y, ufo.getX() + ufo.getWidth() / 2, ufo.getY() + ufo.getHeight() / 2) < blastRadius) {
                ufo.destroy();
                ufos.removeIndex(i);
                score += 5;  // Bonus cho nổ vùng
            }
        }
        for (int i = meteors.size - 1; i >= 0; i--) {
            Meteor meteor = meteors.get(i);
            if (Vector2.dst(x, y, meteor.getX() + meteor.getWidth() / 2, meteor.getY() + meteor.getHeight() / 2) < blastRadius) {
                meteor.destroy();
                meteors.removeIndex(i);
                score += 3;  // Bonus cho nổ vùng với meteor
            }
        }
    }

    private void showGameOverScreen() {
        dispose();
        game.setScreen(new GameOverScreen(game, score));
    }

    private void resetGame() {
        gameOver = false;
        score = 0;
        shieldCount = 0;
        ufos.clear();
        meteors.clear();
        shields.clear();
        bombs.clear();
        missiles.clear();
        traps.clear();  // Mới: Clear traps
        spaceShip.getLasers().clear();
        spaceShip.setPosition(VIRTUAL_WIDTH / 2 - 32, 100);
        lastUfoTime = TimeUtils.nanoTime();
        lastMeteorTime = TimeUtils.nanoTime();
        lastShieldTime = TimeUtils.nanoTime();
        lastBombTime = TimeUtils.nanoTime();
        lastMissileTime = TimeUtils.nanoTime();
        lastTrapTime = TimeUtils.nanoTime();
        Gdx.input.setInputProcessor(stage);
        if (gameOverStage != null) {
            gameOverStage.dispose();
            gameOverStage = null;
        }
        AudioManager.getInstance().stopGameOverMusic();
        AudioManager.getInstance().playBackgroundMusic();
        spawnUfo();
        spawnMeteor();
        spawnShield();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        if (gameOverStage != null) {
            gameOverStage.getViewport().update(width, height, true);
        }
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        AudioManager.getInstance().playBackgroundMusic();
    }

    @Override
    public void hide() {
    }

    @Override
    public void pause() {
        if (backgroundMusic != null && backgroundMusic.isPlaying()) {
            backgroundMusic.pause();
        }
    }

    @Override
    public void resume() {
        if (!gameOver && backgroundMusic != null && !backgroundMusic.isPlaying()) {
            backgroundMusic.play();
        }
    }

    @Override
    public void dispose() {
        if (stage != null) {
            stage.dispose();
        }
        if (font != null) {
            font.dispose();
        }
        if (titleFont != null) {
            titleFont.dispose();
        }
        if (smallFont != null) {
            smallFont.dispose();
        }
        if (fontBatch != null) {
            fontBatch.dispose();
        }
        if (spaceShip != null) {
            spaceShip.dispose();
        }
        if (ufos != null) {
            for (UFO ufo : ufos) {
                if (ufo != null) {
                    ufo.dispose();
                }
            }
            ufos.clear();
        }
        if (meteors != null) {
            for (Meteor meteor : meteors) {
                if (meteor != null) {
                    meteor.dispose();
                }
            }
            meteors.clear();
        }
        if (shields != null) {
            for (Shield shield : shields) {
                if (shield != null) {
                    shield.dispose();
                }
            }
            shields.clear();
        }

        for (Bomb bomb : bombs) bomb.dispose();
        bombs.clear();
        for (Missile missile : missiles) missile.dispose();
        missiles.clear();
        for (Trap trap : traps) {
            trap.remove();
            trap.dispose();  // Mới: Dispose traps
        }
        traps.clear();

        if (backgroundTexture != null) {
            backgroundTexture.dispose();
        }
        if (skin != null) {
            skin.dispose();
        }
        if (gameOverStage != null) {
            gameOverStage.dispose();
        }
        if (backgroundMusic != null) {
            backgroundMusic.dispose();
        }
        if (gameOverMusic != null) {
            gameOverMusic.dispose();
        }
        Explosion.disposeStatic();
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
    }

    // Lớp Explosion mới
    public static class Explosion extends Actor {
        private static Texture blastTexture;
        private float timer = 0f;
        private static final float DURATION = 0.5f; // Thời gian hiển thị hiệu ứng (0.5 giây)

        public Explosion(float x, float y) {
            if (blastTexture == null) {
                try {
                    blastTexture = new Texture(Gdx.files.internal("blast.png"));
                } catch (Exception e) {
                    Gdx.app.error("Explosion", "Error loading blast.png: " + e.getMessage());
                }
            }
            setPosition(x - blastTexture.getWidth() / 2, y - blastTexture.getHeight() / 2); // Căn giữa tại vị trí nổ
            setSize(blastTexture.getWidth(), blastTexture.getHeight());
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            timer += delta;
            if (timer >= DURATION) {
                remove();
            }
        }

        @Override
        public void draw(com.badlogic.gdx.graphics.g2d.Batch batch, float parentAlpha) {
            if (blastTexture != null) {
                batch.draw(blastTexture, getX(), getY(), getWidth(), getHeight());
            }
        }

        public static void disposeStatic() {
            if (blastTexture != null) {
                blastTexture.dispose();
                blastTexture = null;
            }
        }
    }
}
