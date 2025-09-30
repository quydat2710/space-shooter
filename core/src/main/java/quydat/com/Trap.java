package quydat.com;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.TimeUtils;

public class Trap extends Actor {
    private Texture texture;
    private long startTime;
    private static final long DURATION = 10000000000L;  // 10 giây nano

    public Trap(float x, float y, Stage stage) {
        startTime = TimeUtils.nanoTime();
        try {
            texture = new Texture(Gdx.files.internal("trap.png"));
        } catch (Exception e) {
            texture = new Texture(createPixmap(64, 64, new com.badlogic.gdx.graphics.Color(1.0f, 0.5f, 0.0f, 1f)));  // Default cam
        }
        setBounds(x, y, texture.getWidth(), texture.getHeight());
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (TimeUtils.nanoTime() - startTime > DURATION) {
            remove();
        }
    }

    @Override
    public void draw(com.badlogic.gdx.graphics.g2d.Batch batch, float parentAlpha) {
        batch.draw(texture, getX(), getY(), getWidth(), getHeight());
    }

    public void dispose() {
        if (texture != null) {
            texture.dispose();
        }
    }

    private static com.badlogic.gdx.graphics.Pixmap createPixmap(int width, int height, com.badlogic.gdx.graphics.Color color) {
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(width, height, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        return pixmap;
    }
}
