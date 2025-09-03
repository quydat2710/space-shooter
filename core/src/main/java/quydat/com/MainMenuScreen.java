package quydat.com;

import com.badlogic.gdx.Gdx;
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

public class MainMenuScreen implements Screen {
    private final MyGame game;
    private Stage stage;
    private OrthographicCamera camera;
    private FitViewport viewport;
    private BitmapFont font;
    private BitmapFont titleFont;
    private Skin skin;
    private static final float VIRTUAL_WIDTH = 1920f;
    private static final float VIRTUAL_HEIGHT = 1080f;

    public MainMenuScreen(MyGame game) {
        this.game = game;

        camera = new OrthographicCamera();
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        stage = new Stage(viewport);
        Gdx.input.setInputProcessor(stage);

        // Fonts responsive
        float screenScale = Math.min(Gdx.graphics.getWidth() / VIRTUAL_WIDTH, Gdx.graphics.getHeight() / VIRTUAL_HEIGHT);
        font = new BitmapFont();
        font.getData().setScale(2.0f * screenScale);

        titleFont = new BitmapFont();
        titleFont.getData().setScale(5.0f * screenScale);  // Font tiêu đề lớn

        // Skin cho buttons (tái sử dụng logic tạo texture từ code cũ của bạn)
        skin = new Skin();
        Texture buttonTexture = createCircleTexture(60, new Color(0.2f, 0.8f, 0.2f, 0.9f));  // Màu xanh cho buttons
        skin.add("button", buttonTexture);

        // Layout với Table
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // Title
        Label titleLabel = new Label("Space Shooter", new Label.LabelStyle(titleFont, Color.WHITE));
        table.add(titleLabel).padBottom(100).row();

        // Button Play
        TextButton playButton = new TextButton("Play", createButtonStyle());
        playButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new GameScreen(game));
            }
        });
        table.add(playButton).size(300, 100).padBottom(50).row();

        // Button Instructions
        TextButton instructionsButton = new TextButton("Instructions", createButtonStyle());
        instructionsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new InstructionsScreen(game));
            }
        });
        table.add(instructionsButton).size(300, 100).padBottom(50).row();

        // Button Exit
        TextButton exitButton = new TextButton("Exit", createButtonStyle());
        exitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.exit();
            }
        });
        table.add(exitButton).size(300, 100).row();
    }

    private TextButton.TextButtonStyle createButtonStyle() {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.up = skin.getDrawable("button");
        style.font = font;
        style.fontColor = Color.WHITE;
        return style;
    }

    // Tái sử dụng hàm createCircleTexture từ code cũ của bạn
    private Texture createCircleTexture(int baseRadius, Color color) {
        float screenScale = Math.min(Gdx.graphics.getWidth() / VIRTUAL_WIDTH, Gdx.graphics.getHeight() / VIRTUAL_HEIGHT);
        int radius = (int)(baseRadius * screenScale * 1.5f);
        int size = radius * 2;
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(size, size, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
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
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void show() {}
    @Override
    public void hide() {}
    @Override
    public void pause() {}
    @Override
    public void resume() {}

    @Override
    public void dispose() {
        stage.dispose();
        font.dispose();
        titleFont.dispose();
        skin.dispose();
    }
}
