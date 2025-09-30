package quydat.com;

import com.badlogic.gdx.Gdx;
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

    public SpaceShip(float x, float y, Stage s) {

        try {
            texture = new Texture("spaceship.png");
            textureRegion = new TextureRegion(texture);
        } catch (Exception e) {
            Gdx.app.error("SpaceShip", "Error loading texture: " + e.getMessage());
        }

        setPosition(x, y);
//        setSize(textureRegion.getRegionWidth(), textureRegion.getRegionHeight());
        setBounds(x, y, 90, 90);  // Kích thước
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

        // Lấy kích thước stage (world coordinates)
        float worldWidth = getStage().getViewport().getWorldWidth();
        float worldHeight = getStage().getViewport().getWorldHeight();

        boolean hitBorder = false;

        if (newX < 0) {
            newX = 0;
            hitBorder = true;
        }
        if (newX + getWidth() > worldWidth) {
            newX = worldWidth - getWidth();
            hitBorder = true;
        }
        if (newY < 0) {
            newY = 0;
            hitBorder = true;
        }
        if (newY + getHeight() > worldHeight) {
            newY = worldHeight - getHeight();
            hitBorder = true;
        }

        setPosition(newX, newY);

        // Sử dụng AudioManager thay vì âm thanh riêng
        if (hitBorder) {
            AudioManager.getInstance().playBorderSound();
        }
    }

    public void shootLaser() {
        if (timeSinceLastShot >= shootDelay) {
            float laserX = getX() + getWidth() / 2;
            float laserY = getY() + getHeight() / 2;
            laser laser = new laser(laserX, laserY, 0, getStage());
            lasers.add(laser);
            getStage().addActor(laser);

            // Sử dụng AudioManager để phát âm thanh
            AudioManager.getInstance().playLaserSound();

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
        lasers.clear();
        // Không cần dispose sound nữa vì AudioManager quản lý
    }
}
