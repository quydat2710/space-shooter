package quydat.com;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
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

public class GameOverScreen implements Screen {
    private final MyGame game;
    private Stage stage;
    private Table table;
    private Skin skin;
    private int finalScore;
    private BitmapFont titleFont;
    private BitmapFont font;
    private BitmapFont smallFont;
    private static final float VIRTUAL_WIDTH = 1920f;
    private static final float VIRTUAL_HEIGHT = 1080f;

    public GameOverScreen(MyGame game, int score) {
        this.game = game;
        this.finalScore = score;
        create();
    }

    private void create() {
        OrthographicCamera camera = new OrthographicCamera();
        FitViewport viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        stage = new Stage(viewport);
        camera.position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);

        // Khởi tạo fonts
        float screenScale = Math.min(Gdx.graphics.getWidth() / VIRTUAL_WIDTH,
            Gdx.graphics.getHeight() / VIRTUAL_HEIGHT);
        titleFont = new BitmapFont();
        titleFont.getData().setScale(4.5f * screenScale);
        font = new BitmapFont();
        font.getData().setScale(2.0f * screenScale);
        smallFont = new BitmapFont();
        smallFont.getData().setScale(1.8f * screenScale);

        // Khởi tạo skin
        skin = new Skin();

        // Tạo background
        com.badlogic.gdx.graphics.Pixmap bgPixmap = new com.badlogic.gdx.graphics.Pixmap(
            1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        bgPixmap.setColor(0, 0, 0, 0.7f);
        bgPixmap.fill();
        Texture bgTexture = new Texture(bgPixmap);
        bgPixmap.dispose();
        skin.add("game-over-bg", bgTexture);

        // Load textures cho các nút
        loadButtonTextures();

        // Tạo UI
        createUI();

        // Input processor
        com.badlogic.gdx.InputMultiplexer multiplexer = new com.badlogic.gdx.InputMultiplexer();
        multiplexer.addProcessor(stage);
        multiplexer.addProcessor(new com.badlogic.gdx.InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ENTER || keycode == Input.Keys.SPACE) {
                    playAgain();
                    return true;
                }
                if (keycode == Input.Keys.ESCAPE) {
                    goHome();
                    return true;
                }
                return false;
            }
        });
        Gdx.input.setInputProcessor(multiplexer);

        // Phát nhạc game over
        AudioManager.getInstance().playGameOverMusic();
        AudioManager.getInstance().playGameOverSound();
    }

    private void loadButtonTextures() {
        int buttonSize = 100;

        // Play Again button
        try {
            Texture playAgainTexture = new Texture(Gdx.files.internal("play_again_button.png"));
            skin.add("play_again_btn", playAgainTexture);
        } catch (Exception e) {
            skin.add("play_again_btn", createCircleTexture(buttonSize/2,
                new Color(0.2f, 0.8f, 0.2f, 0.9f)));
        }

        // Home button
        try {
            Texture homeTexture = new Texture(Gdx.files.internal("home_button.png"));
            skin.add("home_btn", homeTexture);
        } catch (Exception e) {
            skin.add("home_btn", createCircleTexture(buttonSize/2,
                new Color(0.2f, 0.5f, 1.0f, 0.9f)));
        }

        // Share button
        try {
            Texture shareTexture = new Texture(Gdx.files.internal("share_button.png"));
            skin.add("share_btn", shareTexture);
        } catch (Exception e) {
            skin.add("share_btn", createCircleTexture(buttonSize/2,
                new Color(1.0f, 0.6f, 0.0f, 0.9f)));
        }
    }

    private void createUI() {
        table = new Table();
        table.setFillParent(true);
        table.setBackground(skin.getDrawable("game-over-bg"));

        // Game Over title
        Label gameOverLabel = new Label("GAME OVER",
            new Label.LabelStyle(titleFont, Color.RED));
        table.add(gameOverLabel).padBottom(40).row();

        // Score
        Label scoreLabel = new Label("Final Score: " + finalScore,
            new Label.LabelStyle(font, Color.WHITE));
        table.add(scoreLabel).padBottom(30).row();

        // Credits
        Label creditsLabel = new Label("Made by KMA",
            new Label.LabelStyle(font, Color.GRAY));
        table.add(creditsLabel).padBottom(50).row();

        int buttonSize = 100;

        // Play Again button
        TextButton.TextButtonStyle playAgainStyle = new TextButton.TextButtonStyle();
        playAgainStyle.up = skin.getDrawable("play_again_btn");
        playAgainStyle.font = smallFont;
        playAgainStyle.fontColor = Color.WHITE;
        TextButton playAgainButton = new TextButton("", playAgainStyle);
        playAgainButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                playAgain();
            }
        });
        table.add(playAgainButton).size(buttonSize, buttonSize).padTop(30).row();

        Label playAgainLabel = new Label("Play Again",
            new Label.LabelStyle(smallFont, Color.WHITE));
        table.add(playAgainLabel).padTop(10).row();

        // Home button
        TextButton.TextButtonStyle homeStyle = new TextButton.TextButtonStyle();
        homeStyle.up = skin.getDrawable("home_btn");
        homeStyle.font = smallFont;
        homeStyle.fontColor = Color.WHITE;
        TextButton homeButton = new TextButton("", homeStyle);
        homeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                goHome();
            }
        });
        table.add(homeButton).size(buttonSize, buttonSize).padTop(20).row();

        Label homeLabel = new Label("Home",
            new Label.LabelStyle(smallFont, Color.WHITE));
        table.add(homeLabel).padTop(10).row();

        // Share button
        TextButton.TextButtonStyle shareStyle = new TextButton.TextButtonStyle();
        shareStyle.up = skin.getDrawable("share_btn");
        shareStyle.font = smallFont;
        shareStyle.fontColor = Color.WHITE;
        TextButton shareButton = new TextButton("", shareStyle);
        shareButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                shareScore();
            }
        });
        table.add(shareButton).size(buttonSize, buttonSize).padTop(20).row();

        Label shareLabel = new Label("Share Score",
            new Label.LabelStyle(smallFont, Color.WHITE));
        table.add(shareLabel).padTop(10);

        stage.addActor(table);
    }

    private Texture createCircleTexture(int baseRadius, Color color) {
        float screenScale = Math.min(Gdx.graphics.getWidth() / VIRTUAL_WIDTH,
            Gdx.graphics.getHeight() / VIRTUAL_HEIGHT);
        int radius = (int)(baseRadius * screenScale * 1.5f);
        int size = radius * 2;
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(
            size, size, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
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

    private void playAgain() {
        dispose();
        game.setScreen(new GameScreen(game));
    }

    private void goHome() {
        dispose();
        game.setScreen(new MainMenuScreen(game));
    }

    private void shareScore() {
        String shareText = "I scored " + finalScore + " points in Space Shooter! Can you beat my score?";
        Gdx.app.log("GameOverScreen", "Share score: " + shareText);

        if (Gdx.app.getType() == com.badlogic.gdx.Application.ApplicationType.Desktop) {
            Gdx.app.getClipboard().setContents(shareText);
            Gdx.app.log("GameOverScreen", "Score copied to clipboard!");
        }
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        if (stage != null) {
            stage.dispose();
        }
        if (titleFont != null) {
            titleFont.dispose();
        }
        if (font != null) {
            font.dispose();
        }
        if (smallFont != null) {
            smallFont.dispose();
        }
        if (skin != null) {
            skin.dispose();
        }
        AudioManager.getInstance().stopGameOverMusic();
    }
}
