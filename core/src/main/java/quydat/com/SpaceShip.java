package quydat.com;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;

public class SpaceShip extends Actor {
    private TextureRegion textureRegion;
    private Texture texture;
    private Array<laser> lasers;
    private float shootDelay = 0.2f;
    private float timeSinceLastShot = 0;
    private Sound laserSound;

    public SpaceShip(float x, float y, Stage s) {
        try {
            texture = new Texture("spaceship.png");
            textureRegion = new TextureRegion(texture);
            laserSound = Gdx.audio.newSound(Gdx.files.internal("laser.mp3"));
        } catch (Exception e) {
            Gdx.app.error("SpaceShip", "Error loading resources: " + e.getMessage());
        }
        setPosition(x, y);
        setSize(textureRegion.getRegionWidth(), textureRegion.getRegionHeight());
        setOrigin(getWidth() / 2, getHeight() / 2);
        lasers = new Array<>();
        s.addActor(this);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        timeSinceLastShot += delta;

        for (int i = lasers.size - 1; i >= 0; i--) {
            laser laser = lasers.get(i);
            laser.act(delta);
            if (laser.isOutOfBounds()) {
                lasers.removeIndex(i);
                laser.remove();
            }
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (textureRegion != null) {
            batch.draw(textureRegion, getX(), getY(), getOriginX(), getOriginY(),
                getWidth(), getHeight(), 1, 1, 0); // Không xoay
        }
    }

    public void moveByWithSpeed(float dx, float dy, float delta) {
        float newX = getX() + dx * delta;
        float newY = getY() + dy * delta;

        if (newX < 0) newX = 0;
        if (newX + getWidth() > Gdx.graphics.getWidth()) newX = Gdx.graphics.getWidth() - getWidth();
        if (newY < 0) newY = 0;
        if (newY + getHeight() > Gdx.graphics.getHeight()) newY = Gdx.graphics.getHeight() - getHeight();

        setPosition(newX, newY);
    }

    public void shootLaser() {
        if (timeSinceLastShot >= shootDelay) {
            float laserX = getX() + getWidth() / 2;
            float laserY = getY() + getHeight() / 2;
            laser laser = new laser(laserX, laserY, 0, getStage());
            lasers.add(laser);
            getStage().addActor(laser);
            if (laserSound != null) {
                laserSound.play();
            }
            timeSinceLastShot = 0;
        }
    }

    public Array<laser> getLasers() {
        return lasers;
    }

    public void dispose() {
        if (texture != null) {
            texture.dispose();
            texture = null;
        }
        if (laserSound != null) {
            laserSound.dispose();
            laserSound = null;
        }
        lasers.clear();
    }
}
