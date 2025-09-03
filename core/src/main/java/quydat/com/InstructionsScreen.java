package quydat.com;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
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
    private com.badlogic.gdx.graphics.g2d.BitmapFont font;
    private static final float VIRTUAL_WIDTH = 1920f;
    private static final float VIRTUAL_HEIGHT = 1080f;

    public InstructionsScreen(MyGame game) {
        this.game = game;

        camera = new OrthographicCamera();
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        stage = new Stage(viewport);
        Gdx.input.setInputProcessor(stage);

        font = new com.badlogic.gdx.graphics.g2d.BitmapFont();
        float screenScale = Math.min(Gdx.graphics.getWidth() / VIRTUAL_WIDTH, Gdx.graphics.getHeight() / VIRTUAL_HEIGHT);
        font.getData().setScale(2.0f * screenScale);

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        Label titleLabel = new Label("Instructions", new Label.LabelStyle(font, Color.WHITE));
        table.add(titleLabel).padBottom(50).row();

        Label instructionsLabel = new Label("Use joystick or keys to move.\nPress shoot button or space to fire.\nAvoid UFOs and shoot them down!",
            new Label.LabelStyle(font, Color.CYAN));
        instructionsLabel.setWrap(true);
        table.add(instructionsLabel).width(VIRTUAL_WIDTH * 0.8f).padBottom(50).row();

        TextButton backButton = new TextButton("Back", new TextButton.TextButtonStyle());  // Có thể dùng skin như menu
        backButton.getStyle().font = font;
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new MainMenuScreen(game));
            }
        });
        table.add(backButton).size(200, 80);
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
    }
}
