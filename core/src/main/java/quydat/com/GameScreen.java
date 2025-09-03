package quydat.com;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
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

public class GameScreen implements Screen {
    private final MyGame game;
    private Stage stage;
    private OrthographicCamera camera;
    private FitViewport viewport;
    private SpaceShip spaceShip;
    private Array<UFO> ufos;
    private long lastUfoTime;
    private int score = 0;
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

    public GameScreen(MyGame game) {
        this.game = game;
        create();
    }

    private void create() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        stage = new Stage(viewport);
        camera.position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);

        // Tạo InputMultiplexer để xử lý cả keyboard và touch
        com.badlogic.gdx.InputMultiplexer multiplexer = new com.badlogic.gdx.InputMultiplexer();
        multiplexer.addProcessor(stage);
        multiplexer.addProcessor(new com.badlogic.gdx.InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                // Xử lý keyboard input ở đây nếu cần
                return false;
            }
        });
        Gdx.input.setInputProcessor(multiplexer);

        // Khởi tạo phi thuyền với kích thước responsive
        float shipSize = 64 * Math.min(Gdx.graphics.getWidth() / VIRTUAL_WIDTH, Gdx.graphics.getHeight() / VIRTUAL_HEIGHT);
        spaceShip = new SpaceShip(VIRTUAL_WIDTH / 2 - shipSize/2, 150, stage);
        stage.addActor(spaceShip);

        // Khởi tạo danh sách UFO
        ufos = new Array<>();
        spawnUfo();

        // Khởi tạo fonts với kích thước responsive
        font = new BitmapFont();
        float screenScale = Math.min(Gdx.graphics.getWidth() / VIRTUAL_WIDTH, Gdx.graphics.getHeight() / VIRTUAL_HEIGHT);
        font.getData().setScale(1.8f * screenScale); // Scale theo màn hình thực

        titleFont = new BitmapFont();
        titleFont.getData().setScale(4.0f * screenScale); // Font tiêu đề lớn hơn

        smallFont = new BitmapFont();
        smallFont.getData().setScale(1.2f * screenScale); // Font nhỏ

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
    }

    private void createUIControls() {
        skin = new Skin();

        try {
            // Tạo texture đơn giản nếu không có file
            Texture joystickBg, joystickKnob, shootBtn;

            try {
                joystickBg = new Texture(Gdx.files.internal("joystick_background.png"));
            } catch (Exception e) {
                joystickBg = createCircleTexture(80, new Color(0.3f, 0.3f, 0.3f, 0.8f)); // Lớn hơn và trong suốt
                Gdx.app.log("Main", "Created default joystick background");
            }

            try {
                joystickKnob = new Texture(Gdx.files.internal("joystick_knob.png"));
            } catch (Exception e) {
                joystickKnob = createCircleTexture(35, new Color(0.8f, 0.8f, 0.8f, 0.9f)); // Lớn hơn
                Gdx.app.log("Main", "Created default joystick knob");
            }

            try {
                shootBtn = new Texture(Gdx.files.internal("shoot_button.png"));
            } catch (Exception e) {
                shootBtn = createCircleTexture(60, new Color(1.0f, 0.2f, 0.2f, 0.9f)); // Màu đỏ đậm hơn
                Gdx.app.log("Main", "Created default shoot button");
            }

            skin.add("joystick_background", joystickBg);
            skin.add("joystick_knob", joystickKnob);
            skin.add("shoot_button", shootBtn);

            // Tính toán kích thước và vị trí cân đối hơn
            float screenWidth = viewport.getWorldWidth();
            float screenHeight = viewport.getWorldHeight();
            float margin = screenWidth * 0.08f; // Margin 8% của màn hình

            // Tạo Touchpad (joystick) với kích thước cân đối
            Touchpad.TouchpadStyle touchpadStyle = new Touchpad.TouchpadStyle();
            touchpadStyle.background = skin.getDrawable("joystick_background");
            touchpadStyle.knob = skin.getDrawable("joystick_knob");
            touchpad = new Touchpad(15, touchpadStyle); // Tăng dead zone

            float joystickSize = Math.min(screenWidth, screenHeight) * 0.18f; // 18% của kích thước nhỏ hơn
            touchpad.setBounds(margin, margin, joystickSize, joystickSize);
            stage.addActor(touchpad);

            // Tạo nút bắn cân đối với joystick
            TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
            buttonStyle.up = skin.getDrawable("shoot_button");
            buttonStyle.font = font;
            shootButton = new TextButton("", buttonStyle);

            float buttonSize = joystickSize * 0.85f; // Nhỏ hơn joystick một chút
            shootButton.setBounds(screenWidth - margin - buttonSize, margin, buttonSize, buttonSize);
            shootButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    spaceShip.shootLaser();
                    Gdx.app.log("Main", "Shoot button pressed!");
                }
            });
            stage.addActor(shootButton);

            uiLoaded = true;
            Gdx.app.log("Main", "UI loaded successfully!");

        } catch (Exception e) {
            Gdx.app.error("Main", "Error creating UI controls: " + e.getMessage());
            uiLoaded = false;
        }
    }

    // Tạo texture hình tròn với alpha channel, responsive size
    private Texture createCircleTexture(int baseRadius, Color color) {
        float screenScale = Math.min(Gdx.graphics.getWidth() / VIRTUAL_WIDTH, Gdx.graphics.getHeight() / VIRTUAL_HEIGHT);
        int radius = (int)(baseRadius * screenScale * 1.5f); // Tăng kích thước UI
        int size = radius * 2;
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(size, size, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);

        // Vẽ hình tròn với gradient và viền
        pixmap.setColor(0, 0, 0, 0); // Trong suốt
        pixmap.fill();

        // Vẽ viền
        pixmap.setColor(color.r * 0.5f, color.g * 0.5f, color.b * 0.5f, color.a);
        pixmap.fillCircle(radius, radius, radius);

        // Vẽ phần trong
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
        } else if (gameOverStage != null) {
            gameOverStage.act();
            gameOverStage.draw();
        }

        // Vẽ UI text với vị trí cân đối hơn
        fontBatch.setProjectionMatrix(camera.combined);
        fontBatch.begin();

        if (!gameOver) {
            // Vẽ score với shadow effect, kích thước responsive
            float margin = viewport.getWorldWidth() * 0.02f;
            font.setColor(Color.BLACK);
            font.draw(fontBatch, "Score: " + score, margin + 2, viewport.getWorldHeight() - margin - 2);
            font.setColor(Color.WHITE);
            font.draw(fontBatch, "Score: " + score, margin, viewport.getWorldHeight() - margin);

            // Vẽ hướng dẫn controls theo platform
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

            // Debug touchpad (chỉ hiển thị khi cần thiết)
            if (uiLoaded && Gdx.app.getLogLevel() == Application.LOG_DEBUG) {
                font.setColor(Color.YELLOW);
                font.draw(fontBatch, "Touch: X=" + String.format("%.2f", touchpad.getKnobPercentX()) +
                        " Y=" + String.format("%.2f", touchpad.getKnobPercentY()),
                    margin, viewport.getWorldHeight() - margin * 7);
            }

            // Vẽ credits ở góc dưới phải, responsive position
            float creditsMargin = viewport.getWorldWidth() * 0.02f;
            smallFont.setColor(Color.BLACK);
            smallFont.draw(fontBatch, "Made by KMA", viewport.getWorldWidth() - creditsMargin - 98, creditsMargin + 2);
            smallFont.setColor(Color.GRAY);
            smallFont.draw(fontBatch, "Made by KMA", viewport.getWorldWidth() - creditsMargin - 100, creditsMargin);
        }
        fontBatch.end();

        // Spawn UFO
        if (!gameOver && TimeUtils.nanoTime() - lastUfoTime > 1000000000) {
            spawnUfo();
        }

        checkCollisions();
    }

    private void handleInput() {
        if (!gameOver) {
            float delta = Gdx.graphics.getDeltaTime();
            float moveSpeed = 400f; // Tăng tốc độ di chuyển

            float dx = 0, dy = 0;
            boolean shouldShoot = false;

            // Ưu tiên keyboard trên desktop, touch trên mobile
            boolean useKeyboard = (Gdx.app.getType() == com.badlogic.gdx.Application.ApplicationType.Desktop) || !uiLoaded;

            if (!useKeyboard && uiLoaded && touchpad != null) {
                // Touch controls
                float knobX = touchpad.getKnobPercentX();
                float knobY = touchpad.getKnobPercentY();

                // Tăng sensitivity và smooth movement
                if (Math.abs(knobX) > 0.05f || Math.abs(knobY) > 0.05f) {
                    dx = moveSpeed * knobX * Math.abs(knobX); // Quadratic response
                    dy = moveSpeed * knobY * Math.abs(knobY);
                }

                if (shootButton != null && shootButton.isPressed()) {
                    shouldShoot = true;
                }
            } else {
                // Keyboard controls (luôn hoạt động trên desktop)
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

                // Thêm ESC để thoát game
                if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                    Gdx.app.exit();
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
            UFO ufo = new UFO(x, y, stage);
            ufos.add(ufo);
            stage.addActor(ufo);
            lastUfoTime = TimeUtils.nanoTime();
        }
    }

    private void checkCollisions() {
        if (!gameOver) {
            for (int i = ufos.size - 1; i >= 0; i--) {
                UFO ufo = ufos.get(i);
                Rectangle ufoBounds = new Rectangle(ufo.getX(), ufo.getY(), ufo.getWidth(), ufo.getHeight());

                Rectangle shipBounds = new Rectangle(spaceShip.getX(), spaceShip.getY(), spaceShip.getWidth(), spaceShip.getHeight());
                if (ufoBounds.overlaps(shipBounds)) {
                    gameOver = true;
                    showGameOverScreen();
                    return;
                }

                Array<laser> lasers = spaceShip.getLasers();
                for (int j = lasers.size - 1; j >= 0; j--) {
                    laser laser = lasers.get(j);
                    Rectangle laserBounds = new Rectangle(laser.getX(), laser.getY(), laser.getWidth(), laser.getHeight());

                    if (laserBounds.overlaps(ufoBounds)) {
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
        }
    }

    private void showGameOverScreen() {
        gameOverStage = new Stage(new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, new OrthographicCamera()));
        gameOverTable = new Table();
        gameOverTable.setFillParent(true);

        // Tạo background mờ cho game over screen
        com.badlogic.gdx.graphics.Pixmap bgPixmap = new com.badlogic.gdx.graphics.Pixmap(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        bgPixmap.setColor(0, 0, 0, 0.7f);
        bgPixmap.fill();
        Texture bgTexture = new Texture(bgPixmap);
        bgPixmap.dispose();
        skin.add("game-over-bg", bgTexture);
        gameOverTable.setBackground(skin.getDrawable("game-over-bg"));

        // Game Over title
        com.badlogic.gdx.scenes.scene2d.ui.Label gameOverLabel = new com.badlogic.gdx.scenes.scene2d.ui.Label("GAME OVER",
            new com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle(titleFont, Color.RED));
        gameOverTable.add(gameOverLabel).padBottom(40).row();

        // Final Score
        com.badlogic.gdx.scenes.scene2d.ui.Label scoreLabel = new com.badlogic.gdx.scenes.scene2d.ui.Label("Final Score: " + score,
            new com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle(font, Color.WHITE));
        gameOverTable.add(scoreLabel).padBottom(30).row();

        // Credits
        com.badlogic.gdx.scenes.scene2d.ui.Label creditsLabel = new com.badlogic.gdx.scenes.scene2d.ui.Label("Made by KMA",
            new com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle(font, Color.GRAY));
        gameOverTable.add(creditsLabel).padBottom(50).row();

        // Play Again Button với style đẹp hơn
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.up = skin.getDrawable("shoot_button");
        buttonStyle.font = font;
        buttonStyle.fontColor = Color.WHITE;
        playAgainButton = new TextButton("PLAY AGAIN", buttonStyle);
        playAgainButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                resetGame();
            }
        });
        gameOverTable.add(playAgainButton).size(200, 80).padTop(30);

        gameOverStage.addActor(gameOverTable);

        // Tạo InputMultiplexer cho game over screen
        com.badlogic.gdx.InputMultiplexer gameOverMultiplexer = new com.badlogic.gdx.InputMultiplexer();
        gameOverMultiplexer.addProcessor(gameOverStage);
        gameOverMultiplexer.addProcessor(new com.badlogic.gdx.InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ENTER || keycode == Input.Keys.SPACE) {
                    resetGame();
                    return true;
                }
                if (keycode == Input.Keys.ESCAPE) {
                    Gdx.app.exit();
                    return true;
                }
                return false;
            }
        });
        Gdx.input.setInputProcessor(gameOverMultiplexer);
    }

    private void resetGame() {
        gameOver = false;
        score = 0;
        ufos.clear();
        spaceShip.getLasers().clear();
        spaceShip.setPosition(VIRTUAL_WIDTH / 2 - 32, 100);
        lastUfoTime = TimeUtils.nanoTime();
        Gdx.input.setInputProcessor(stage);
        if (gameOverStage != null) {
            gameOverStage.dispose();
            gameOverStage = null;
        }
        spawnUfo();
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
    }

    @Override
    public void hide() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
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
        if (backgroundTexture != null) {
            backgroundTexture.dispose();
        }
        if (skin != null) {
            skin.dispose();
        }
        if (gameOverStage != null) {
            gameOverStage.dispose();
        }
    }
}
