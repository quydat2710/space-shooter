package quydat.com;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;

public class Missile extends Actor {
    private Texture texture;
    private Vector2 velocity = new Vector2(0, 500f);  // initial upward speed (px/s)
    private Actor target;  // homing target (UFO or Meteor)
    private float speed = 900f; // constant speed magnitude
    private float steerStrength = 8f; // how quickly missile turns toward target

    // Constructor không target (fallback)
    public Missile(float x, float y, Stage stage) {
        this(x, y, stage, null);
    }

    // Constructor có target
    public Missile(float x, float y, Stage stage, Actor target) {
        try {
            texture = new Texture(Gdx.files.internal("missile.png"));
            // nếu muốn, bạn có thể setWidth/Height theo texture.getWidth()...
            setBounds(x, y, 32, 32);
        } catch (Exception e) {
            texture = createCircleTexture(10, Color.BLUE);
            setBounds(x, y, 20, 20);
        }
        this.target = target;

        // nếu đã có target lúc tạo, khởi tạo velocity hướng về target
        if (this.target != null) {
            Vector2 dir = new Vector2(
                target.getX() + target.getWidth() / 2f - (getX() + getWidth() / 2f),
                target.getY() + target.getHeight() / 2f - (getY() + getHeight() / 2f)
            ).nor();
            velocity.set(dir.scl(speed));
        }
    }

    // Cho phép set target sau khi tạo
    public void setTarget(Actor target) {
        this.target = target;
    }

    // Giữ phương thức updateTarget nếu bạn muốn tự tìm mục tiêu từ mảng ufos/meteors
    public void updateTarget(Array<UFO> ufos, Array<Meteor> meteors) {
        target = null;
        float minDist = Float.MAX_VALUE;
        for (UFO ufo : ufos) {
            float dist = Vector2.dst(getX(), getY(), ufo.getX(), ufo.getY());
            if (dist < minDist) {
                minDist = dist;
                target = ufo;
            }
        }
        for (Meteor meteor : meteors) {
            float dist = Vector2.dst(getX(), getY(), meteor.getX(), meteor.getY());
            if (dist < minDist) {
                minDist = dist;
                target = meteor;
            }
        }
        if (target != null) {
            Vector2 direction = new Vector2(
                target.getX() + target.getWidth()/2f - (getX()+getWidth()/2f),
                target.getY() + target.getHeight()/2f - (getY()+getHeight()/2f)
            ).nor();
            velocity.set(direction.scl(speed));
        }
    }

    @Override
    public void act(float delta) {
        // Nếu có target, điều chỉnh velocity dần dần (steering) để homing mượt
        if (target != null) {
            // Nếu target đã bị remove khỏi stage, target có thể null hoặc không còn parent -> xử lý
            if (target.getStage() == null) {
                // target đã bị xóa => không homing nữa
                target = null;
            } else {
                Vector2 toTarget = new Vector2(
                    target.getX() + target.getWidth()/2f - (getX() + getWidth()/2f),
                    target.getY() + target.getHeight()/2f - (getY() + getHeight()/2f)
                );
                if (toTarget.len() > 1f) {
                    Vector2 desired = toTarget.nor().scl(speed);
                    // Lerp velocity về desired (mượt, không quay giật)
                    velocity.lerp(desired, Math.min(1f, steerStrength * delta));
                }
            }
        }

        moveBy(velocity.x * delta, velocity.y * delta);
        super.act(delta);
    }

    @Override
    public void draw(com.badlogic.gdx.graphics.g2d.Batch batch, float parentAlpha) {
        batch.draw(texture, getX(), getY(), getWidth(), getHeight());
        super.draw(batch, parentAlpha);
    }

    public void dispose() {
        if (texture != null) texture.dispose();
    }

    private Texture createCircleTexture(int radius, Color color) {
        int size = radius * 2;
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fillCircle(radius, radius, radius);
        Texture tex = new Texture(pixmap);
        pixmap.dispose();
        return tex;
    }
}
