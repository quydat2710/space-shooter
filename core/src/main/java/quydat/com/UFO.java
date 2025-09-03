package quydat.com;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class UFO extends Actor {
    private TextureRegion textureRegion;
    private Texture texture;
    private float speed = 200f;
    private Sound explosionSound;

    public UFO(float x, float y, Stage stage) {
        try {
            texture = new Texture("ufo.png");
            textureRegion = new TextureRegion(texture);
            explosionSound = Gdx.audio.newSound(Gdx.files.internal("explosion.mp3"));
        } catch (Exception e) {
            Gdx.app.error("UFO", "Error loading resources: " + e.getMessage());
        }
        setPosition(x, y);
        setSize(textureRegion.getRegionWidth(), textureRegion.getRegionHeight());
        setOrigin(getWidth() / 2, getHeight() / 2);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        moveBy(0, -speed * delta);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (textureRegion != null) {
            batch.draw(textureRegion, getX(), getY(), getOriginX(), getOriginY(),
                getWidth(), getHeight(), 1, 1, getRotation());
        }
    }

    public void destroy() {
        if (explosionSound != null) {
            explosionSound.play();
        }
        remove();
    }


    public void dispose() {
        if (texture != null) {
            texture.dispose();
            texture = null;
        }
        if (explosionSound != null) {
            explosionSound.dispose();
            explosionSound = null;
        }
    }
}
