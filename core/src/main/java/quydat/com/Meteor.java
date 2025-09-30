package quydat.com;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class Meteor extends Actor {
    private Sprite sprite;
    private float speed;
    private float rotationSpeed;
    private Texture texture;

    public Meteor(float x, float y, Stage stage) {
        try {
            texture = new Texture(Gdx.files.internal("meteor.png"));
        } catch (Exception e) {
            Gdx.app.error("Meteor", "Error loading meteor texture: " + e.getMessage());
            // Fallback texture nếu không tìm thấy file
            texture = new Texture(64, 64, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        }
        sprite = new Sprite(texture);

        float size = MathUtils.random(40f, 80f);
        sprite.setSize(size, size);
        sprite.setOrigin(size/2, size/2);
        setPosition(x, y);
        setSize(size, size);

        speed = 450f; // Nhanh hơn phi thuyền (400f)
        rotationSpeed = MathUtils.random(-180f, 180f);
        sprite.setPosition(x, y);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        moveBy(0, -speed * delta);
        sprite.rotate(rotationSpeed * delta);
        setRotation(sprite.getRotation());
        sprite.setPosition(getX(), getY());
        if (getY() + getHeight() < 0) {
            remove();
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        sprite.draw(batch, parentAlpha);
    }

    public void destroy() {
        AudioManager.getInstance().playExplosionSound();
        remove();
    }

    public void dispose() {
        if (texture != null) {
            texture.dispose();
        }
    }
}
