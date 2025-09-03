package quydat.com;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class laser extends Actor {
    private TextureRegion textureRegion;
    private Texture texture;
    private float speed = 500f;

    public laser(float x, float y, float angle, Stage stage) {
        try {
            texture = new Texture("laser.png");
            textureRegion = new TextureRegion(texture);
        } catch (Exception e) {
            Gdx.app.error("laser", "Error loading texture: " + e.getMessage());
        }
        setPosition(x, y);
        setSize(textureRegion.getRegionWidth(), textureRegion.getRegionHeight());
        setOrigin(getWidth() / 2, getHeight() / 2);
        setRotation(angle);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        moveBy(0, speed * delta); // Laser đi thẳng lên
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (textureRegion != null) {
            batch.draw(textureRegion, getX(), getY(), getOriginX(), getOriginY(),
                getWidth(), getHeight(), 1, 1, getRotation());
        }
    }

    public boolean isOutOfBounds() {
        return getX() < 0 || getX() > Gdx.graphics.getWidth() ||
            getY() < 0 || getY() > Gdx.graphics.getHeight();
    }

    public void dispose() {
        if (texture != null) {
            texture.dispose();
            texture = null;
        }
    }
}
