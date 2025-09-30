package quydat.com;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class Shield extends Actor {
    private Sprite sprite;
    private float speed;
    private Texture texture;

    public Shield(float x, float y, Stage stage) {
        try {
            texture = new Texture(Gdx.files.internal("shield.png"));
        } catch (Exception e) {
            Gdx.app.error("Shield", "Error loading shield texture: " + e.getMessage());
            // Fallback texture nếu không tìm thấy file
            texture = new Texture(64, 64, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        }
        sprite = new Sprite(texture);

        float size = 50f;
        sprite.setSize(size, size);
        sprite.setOrigin(size/2, size/2);
        setPosition(x, y);
        setSize(size, size);

        speed = 450f; // Nhanh hơn phi thuyền (400f)
        sprite.setPosition(x, y);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        moveBy(0, -speed * delta);
        sprite.setPosition(getX(), getY());
        if (getY() + getHeight() < 0) {
            remove();
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        sprite.draw(batch, parentAlpha);
    }

    public void dispose() {
        if (texture != null) {
            texture.dispose();
        }
    }
}
