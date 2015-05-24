package co.bravebunny.circular.entities.objects;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.equations.Back;
import aurelienribon.tweenengine.equations.Circ;
import aurelienribon.tweenengine.equations.Expo;
import aurelienribon.tweenengine.equations.Quad;
import co.bravebunny.circular.managers.ActorTween;
import co.bravebunny.circular.managers.Assets;
import co.bravebunny.circular.managers.Positions;
import co.bravebunny.circular.screens.GameScreen;

public class Coin extends Solid {
	private float h;
	private float angle = 0;
	private int type;
	private AnimatedImage animation;
	private float elapsedTime = 0;
	private Sound collectSound;
    private float bpm;

    private TweenCallback destroyCallback = new TweenCallback() {
		@Override
		public void onEvent(int type, BaseTween<?> source)
		{
			if(type == TweenCallback.END) {
				dispose();
			}
		}
	};

    public void setBPM(float bpm) {
        this.bpm = bpm;
    }

	public void init() {
		coll_on = false;
		type = MathUtils.random(1);
		h = 400 - type*60;
		
		collectSound = Assets.getSound("coin");
		
		//actors.setOrigin(actors.getWidth()/2, actors.getHeight()/2);
		actors.setScale(0);
		animation = Assets.getAnimation("level/coin");
		actors.addActor(animation);
		
	}

    public void grow() {
        //grow to initial size
		Tween.to(actors, ActorTween.SCALE, 60/bpm).target(1 - type*0.3f).delay(30/bpm)
		.ease(Back.OUT).start(GameScreen.getTweenManager());
		
        //turn on collisions
        Timer.schedule(new Task(){
            @Override
            public void run() {
                coll_on = true;
            }
        }, 90/bpm);
		
        //destroy the coin after some time
        Timer.schedule(new Task() {
            @Override
            public void run() {
                if (coll_on) destroy();
            }
        }, 3 * 60 / bpm);
    }
	
	public void destroy() {
        Tween.to(actors, ActorTween.SCALE, 60 / bpm).target(2 - type * 0.6f)
                .ease(Quad.IN).start(GameScreen.getTweenManager());

        actors.addAction(Actions.sequence(
                Actions.fadeOut(60 / bpm),
                Actions.delay(60 / bpm),
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        dispose();
                    }
                })
        ));
    }
	
	//coin beat effect
	public void beat(){
		//TODO
	}
	
	public void render(float delta) {
		//place the coin in the game screen, and rotate it to an angle in front of the ship
		Positions.setPolarPosition(actors, h*actors.getScaleX(), angle);
		coll_radius = 100*actors.getScaleX();

	}
	
	@Override
	public void setRotation(float degrees) {
		super.setRotation(degrees - 90);
		this.angle = degrees;
	}

    public void collect() {
        coll_on = false;
		collectSound.play();
		animation.setSpeed(2);
        Timeline.createSequence()
        .push(Tween.to(actors, ActorTween.SCALE, 0.3f).target(actors.getScaleX()*1.2f, actors.getScaleX()*1.2f).ease(Circ.OUT))
        .push(Tween.to(actors, ActorTween.SCALE, 0.5f).target(0f, 0f).ease(Expo.IN))
        .start(GameScreen.getTweenManager());
	}
	
	public boolean isDead() {
		return actors == null;
	}

}
