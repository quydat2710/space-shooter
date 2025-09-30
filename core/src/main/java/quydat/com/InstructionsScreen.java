package quydat.com;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class InstructionsScreen implements Screen {
    private final MyGame game;
    private Stage stage;
    private OrthographicCamera camera;
    private FitViewport viewport;
    private BitmapFont font;
    private BitmapFont titleFont;
    private Skin skin;
    private Music backgroundMusic;
    private static final float VIRTUAL_WIDTH = 1920f;
    private static final float VIRTUAL_HEIGHT = 1080f;

    public InstructionsScreen(MyGame game) {
        this.game = game;
        create();
    }

    private void create() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        stage = new Stage(viewport);
        Gdx.input.setInputProcessor(stage);

        // Initialize fonts with responsive scaling
        float screenScale = Math.min(Gdx.graphics.getWidth() / VIRTUAL_WIDTH, Gdx.graphics.getHeight() / VIRTUAL_HEIGHT);

        font = new BitmapFont();
        font.getData().setScale(2.0f * screenScale);

        titleFont = new BitmapFont();
        titleFont.getData().setScale(4.0f * screenScale);

        // Create skin for UI styling
        createSkin();

        // Load and play background music
        loadBackgroundMusic();

        // Create UI
        createUI();
    }

    private void createSkin() {
        skin = new Skin();

        // Create button texture
        Texture buttonTexture = createButtonTexture(200, 80, new Color(0.2f, 0.6f, 1.0f, 0.8f));
        skin.add("button", buttonTexture);

        // Create button style
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.up = skin.getDrawable("button");
        buttonStyle.font = font;
        buttonStyle.fontColor = Color.WHITE;
        skin.add("default", buttonStyle);
    }

    private Texture createButtonTexture(int width, int height, Color color) {
        float screenScale = Math.min(Gdx.graphics.getWidth() / VIRTUAL_WIDTH, Gdx.graphics.getHeight() / VIRTUAL_HEIGHT);
        int scaledWidth = (int)(width * screenScale);
        int scaledHeight = (int)(height * screenScale);

        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(scaledWidth, scaledHeight,
            com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);

        // Fill with background color
        pixmap.setColor(color);
        pixmap.fill();

        // Add border
        pixmap.setColor(color.r + 0.2f, color.g + 0.2f, color.b + 0.2f, color.a);
        pixmap.drawRectangle(0, 0, scaledWidth, scaledHeight);
        pixmap.drawRectangle(1, 1, scaledWidth - 2, scaledHeight - 2);

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private void loadBackgroundMusic() {
        try {
            // Try to load background music - replace "background_music.mp3" with your music file
            backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("background_music.mp3"));
            // Alternative file formats you can try:
            // backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("background_music.ogg"));
            // backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("background_music.wav"));

            backgroundMusic.setLooping(true);
            backgroundMusic.setVolume(0.5f); // Set volume to 50%
            backgroundMusic.play();

            Gdx.app.log("InstructionsScreen", "Background music loaded successfully");
        } catch (Exception e) {
            Gdx.app.error("InstructionsScreen", "Could not load background music: " + e.getMessage());
            backgroundMusic = null;
        }
    }

    private void createUI() {
        Table mainTable = new Table();
        mainTable.setFillParent(true);
        stage.addActor(mainTable);

        // Title
        Label titleLabel = new Label("HOW TO PLAY", new Label.LabelStyle(titleFont, Color.CYAN));
        mainTable.add(titleLabel).padBottom(80).row();

        // Instructions content
        Table instructionsTable = new Table();

        // Movement instructions
        Label moveTitle = new Label("MOVEMENT:", new Label.LabelStyle(font, Color.YELLOW));
        instructionsTable.add(moveTitle).left().padBottom(20).row();

        Label moveInstructions = new Label(
            "Desktop: Use WASD or Arrow Keys to move your spaceship\n" +
                "Mobile: Use the virtual joystick on the bottom-left",
            new Label.LabelStyle(font, Color.WHITE));
        moveInstructions.setWrap(true);
        instructionsTable.add(moveInstructions).width(VIRTUAL_WIDTH * 0.8f).left().padBottom(40).row();

        // Shooting instructions
        Label shootTitle = new Label("SHOOTING:", new Label.LabelStyle(font, Color.YELLOW));
        instructionsTable.add(shootTitle).left().padBottom(20).row();

        Label shootInstructions = new Label(
            "Desktop: Press SPACEBAR to shoot lasers\n" +
                "Mobile: Tap the red button on the bottom-right",
            new Label.LabelStyle(font, Color.WHITE));
        shootInstructions.setWrap(true);
        instructionsTable.add(shootInstructions).width(VIRTUAL_WIDTH * 0.8f).left().padBottom(40).row();

        // Objective
        Label objectiveTitle = new Label("OBJECTIVE:", new Label.LabelStyle(font, Color.YELLOW));
        instructionsTable.add(objectiveTitle).left().padBottom(20).row();

        Label objectiveText = new Label(
            "• Destroy UFOs to earn points (10 points each)\n" +
                "• Avoid colliding with UFOs or you'll lose\n" +
                "• Try to achieve the highest score possible!",
            new Label.LabelStyle(font, Color.WHITE));
        objectiveText.setWrap(true);
        instructionsTable.add(objectiveText).width(VIRTUAL_WIDTH * 0.8f).left().padBottom(60).row();

        mainTable.add(instructionsTable).padBottom(60).row();

        // Back button
        TextButton backButton = new TextButton("BACK TO MENU", skin);
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Stop music when leaving screen
                if (backgroundMusic != null && backgroundMusic.isPlaying()) {
                    backgroundMusic.stop();
                }
                game.setScreen(new MainMenuScreen(game));
            }
        });

        mainTable.add(backButton).size(300, 100).pad(20);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);

        camera.update();
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);

        // Resume music if it was paused
        if (backgroundMusic != null && !backgroundMusic.isPlaying()) {
            backgroundMusic.play();
        }
    }

    @Override
    public void hide() {
        // Pause music when screen is hidden
        if (backgroundMusic != null && backgroundMusic.isPlaying()) {
            backgroundMusic.pause();
        }
    }

    @Override
    public void pause() {
        if (backgroundMusic != null && backgroundMusic.isPlaying()) {
            backgroundMusic.pause();
        }
    }

    @Override
    public void resume() {
        if (backgroundMusic != null && !backgroundMusic.isPlaying()) {
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
        if (skin != null) {
            skin.dispose();
        }
        if (backgroundMusic != null) {
            backgroundMusic.dispose();
        }
    }
}
