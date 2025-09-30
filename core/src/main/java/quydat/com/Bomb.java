package quydat.com;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class Bomb extends Actor {
    private Texture texture;
    private float speed = 500f;  // Tốc độ bay lên

    public Bomb(float x, float y, Stage stage) {
        try {
            texture = new Texture(Gdx.files.internal("bomb.png"));  // Hình bomb.png
        } catch (Exception e) {
            texture = createCircleTexture(20, Color.RED);  // Fallback đỏ
        }
        setBounds(x, y, 64, 64);  // Kích thước
    }

    @Override
    public void act(float delta) {
        moveBy(0, speed * delta);  // Bay lên
        super.act(delta);
    }

    @Override
    public void draw(com.badlogic.gdx.graphics.g2d.Batch batch, float parentAlpha) {
        batch.draw(texture, getX(), getY(), getWidth(), getHeight());
        super.draw(batch, parentAlpha);
    }

    public void dispose() {
        texture.dispose();
    }

    // Helper fallback
    private Texture createCircleTexture(int radius, Color color) {
        int size = radius * 2;
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(size, size, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fillCircle(radius, radius, radius);
        Texture tex = new Texture(pixmap);
        pixmap.dispose();
        return tex;
    }
}
