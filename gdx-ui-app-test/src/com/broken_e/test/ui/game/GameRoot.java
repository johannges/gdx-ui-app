package com.broken_e.test.ui.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;
import com.broken_e.test.ui.TestApp;
import com.broken_e.test.ui.game.Mob.MobTouchedEvent;

public class GameRoot extends Group {

	private TestApp app;

	private OrthographicCamera cam = new OrthographicCamera();
	private float screenW = Gdx.graphics.getWidth();
	private float screenH = Gdx.graphics.getHeight();
	private Vector3 v3 = new Vector3();
	private Matrix4 tmpMatrix4 = new Matrix4();

	public Stats stats = new Stats();

	// Timer timer = new Timer();

	public GameRoot(TestApp app) {
		this.app = app;
	}

	/** used because actors usually need to run the full constructor before adding things to them */
	public GameRoot init() {
		this.setSize(16, (screenH / screenW) * 16);
		cam.setToOrtho(false, getWidth(), getHeight());
		cam.update();
		this.addListener(new EventListener() {
			@Override
			public boolean handle(Event event) {
				if (event instanceof MobTouchedEvent) {
					Mob mob = (Mob) event.getTarget();
					mob.remove();
					Pools.free(mob);
					stats.pointUp();
					app.getGameScreen().pointsChanged(stats.getPoints());
				}
				return false;
			}
		});
		return this;
	}

	/** changes coordinates from screen to game units */
	@Override
	public Actor hit(float x, float y, boolean touchable) {
		v3.set(x, screenH - y, 0f);
		cam.unproject(v3);
		return super.hit(v3.x, v3.y, touchable);
	}

	/** sets batch to game units to draw and then back to screen */
	public void draw(SpriteBatch batch, float parentAlpha) {
		tmpMatrix4.set(batch.getProjectionMatrix());
		batch.setProjectionMatrix(cam.combined);
		super.draw(batch, parentAlpha);
		batch.setProjectionMatrix(tmpMatrix4);
	}

	private float accum = 420f, end = 1f, totalTime;

	@Override
	public void act(float delta) {
		totalTime += delta;
		accum += delta;
		if (accum > end) {
			accum = 0;
			if (end > .3f)
				end -= .001f * Math.sqrt(totalTime);

			this.addActor(Pools.obtain(Mob.class).init(app.skin.getAtlas().findRegion("white-pixel"), 1f));
		}
		super.act(delta);
	}

	public float getTotalTime() {
		return totalTime;
	}
}