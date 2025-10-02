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
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.Preferences;

public class WinScreen implements Screen {
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

    private long gameTime; // Thời gian chơi (millis)
    private Stage highScoreStage;
    private boolean showingHighScores = false;

    public WinScreen(MyGame game, int score, long gameTime) {
        this.game = game;
        this.finalScore = score;
        this.gameTime = gameTime;
        saveHighScore(gameTime);
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
        font.getData().setScale(2.8f * screenScale);
        smallFont = new BitmapFont();
        smallFont.getData().setScale(2.0f * screenScale);

        // Khởi tạo skin
        skin = new Skin();

        // Tạo background
        com.badlogic.gdx.graphics.Pixmap bgPixmap = new com.badlogic.gdx.graphics.Pixmap(
            1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        bgPixmap.setColor(0, 0.2f, 0.3f, 0.8f); // Màu xanh đậm cho màn thắng
        bgPixmap.fill();
        Texture bgTexture = new Texture(bgPixmap);
        bgPixmap.dispose();
        skin.add("win-bg", bgTexture);

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

        // Phát nhạc chiến thắng (nếu có)
        AudioManager.getInstance().stopBackgroundMusic();
        AudioManager.getInstance().stopGameOverMusic();
        // TODO: Thêm AudioManager.getInstance().playWinMusic(); khi có file âm thanh
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

        // High Score button
        try {
            Texture highScoreTexture = new Texture(Gdx.files.internal("highScore.png"));
            skin.add("high_score_btn", highScoreTexture);
        } catch (Exception e) {
            skin.add("high_score_btn", createCircleTexture(buttonSize/2,
                new Color(0.8f, 0.8f, 0.2f, 0.9f)));
        }

        // High Score button
        try {
            Texture backTexture = new Texture(Gdx.files.internal("back.png"));
            skin.add("back_btn", backTexture);
        } catch (Exception e) {
            skin.add("back_btn", createCircleTexture(buttonSize/2,
                new Color(0.8f, 0.8f, 0.2f, 0.9f)));
        }
    }

    private void createUI() {
        table = new Table();
        table.setFillParent(true);
        table.setBackground(skin.getDrawable("win-bg"));

        // Victory title
//        Label victoryLabel = new Label("CONGRATULATION!",
//            new Label.LabelStyle(titleFont, Color.GOLD));
//        table.add(victoryLabel).padBottom(20).row();

        // Thay Congratulations bằng ảnh
        Texture congratsTexture;
        try {
            congratsTexture = new Texture(Gdx.files.internal("congratulations.png"));
        } catch (Exception e) {
            // Nếu không load được, dùng placeholder
            com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(200, 100, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.WHITE);
            pixmap.fill();
            congratsTexture = new Texture(pixmap);
            pixmap.dispose();
        }
        Image congratsImage = new Image(congratsTexture);
        table.add(congratsImage).padBottom(30).row();

        // Score
        Label scoreLabel = new Label("Final Score: " + finalScore,
            new Label.LabelStyle(font, Color.YELLOW));
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

        // High Score button
        TextButton.TextButtonStyle highScoreStyle = new TextButton.TextButtonStyle();
        highScoreStyle.up = skin.getDrawable("high_score_btn");
        highScoreStyle.font = smallFont;
        highScoreStyle.fontColor = Color.WHITE;
        TextButton highScoreButton = new TextButton("", highScoreStyle);
        highScoreButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showHighScores();
            }
        });
        table.add(highScoreButton).size(buttonSize, buttonSize).padTop(20).row();

        Label highScoreLabel = new Label("High Scores",
            new Label.LabelStyle(smallFont, Color.WHITE));
        table.add(highScoreLabel).padTop(10).row();

        stage.addActor(table);
    }

    private void showHighScores() {
        if (showingHighScores) return;
        showingHighScores = true;

        OrthographicCamera camera = new OrthographicCamera();
        FitViewport viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        highScoreStage = new Stage(viewport);
        camera.position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);

        Table hsTable = new Table();
        hsTable.setFillParent(true);
        hsTable.setBackground(skin.getDrawable("win-bg"));

        Label title = new Label("High Scores (Fastest Times)", new Label.LabelStyle(titleFont, Color.GOLD));
        hsTable.add(title).padBottom(50).row();

        List<Long> highScores = loadHighScores();
        for (int i = 0; i < Math.min(6, highScores.size()); i++) {
            long time = highScores.get(i);
            int minutes = (int) (time / 60000);
            int seconds = (int) ((time % 60000) / 1000);
            Label scoreLabel = new Label(String.format("%d. %02d:%02d", i + 1, minutes, seconds),
                new Label.LabelStyle(font, Color.WHITE));
            hsTable.add(scoreLabel).padBottom(20).row();
        }

        TextButton.TextButtonStyle backStyle = new TextButton.TextButtonStyle();
        backStyle.up = skin.getDrawable("back_btn"); // Reuse play again button texture for back
        backStyle.font = smallFont;
        backStyle.fontColor = Color.WHITE;
        TextButton backButton = new TextButton("Back", backStyle);
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                hideHighScores();
            }
        });
        hsTable.add(backButton).padTop(50).row();

        highScoreStage.addActor(hsTable);

        com.badlogic.gdx.InputMultiplexer multiplexer = (com.badlogic.gdx.InputMultiplexer) Gdx.input.getInputProcessor();
        multiplexer.addProcessor(highScoreStage);
    }

    private void hideHighScores() {
        showingHighScores = false;
        com.badlogic.gdx.InputMultiplexer multiplexer = (com.badlogic.gdx.InputMultiplexer) Gdx.input.getInputProcessor();
        multiplexer.removeProcessor(highScoreStage);
        highScoreStage.dispose();
        highScoreStage = null;
    }

    private void saveHighScore(long time) {
        Preferences prefs = Gdx.app.getPreferences("SpaceShooterHighScores");
        List<Long> highScores = loadHighScores();
        highScores.add(time);
        Collections.sort(highScores);
        while (highScores.size() > 6) {
            highScores.remove(highScores.size() - 1);
        }
        for (int i = 0; i < highScores.size(); i++) {
            prefs.putLong("score" + i, highScores.get(i));
        }
        prefs.putInteger("count", highScores.size());
        prefs.flush();
    }

    private List<Long> loadHighScores() {
        Preferences prefs = Gdx.app.getPreferences("SpaceShooterHighScores");
        int count = prefs.getInteger("count", 0);
        List<Long> highScores = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            highScores.add(prefs.getLong("score" + i, Long.MAX_VALUE));
        }
        Collections.sort(highScores);
        return highScores;
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
        String shareText = "I won Space Shooter with " + finalScore + " points! Can you beat my score?";
        Gdx.app.log("WinScreen", "Share score: " + shareText);

        if (Gdx.app.getType() == com.badlogic.gdx.Application.ApplicationType.Desktop) {
            Gdx.app.getClipboard().setContents(shareText);
            Gdx.app.log("WinScreen", "Score copied to clipboard!");
        }
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);
        if (showingHighScores && highScoreStage != null) {
            highScoreStage.act(delta);
            highScoreStage.draw();
        } else {
            stage.act(delta);
            stage.draw();
        }
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        if (highScoreStage != null) {
            highScoreStage.getViewport().update(width, height, true);
        }
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
        if (highScoreStage != null) {
            highScoreStage.dispose();
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
    }
}
