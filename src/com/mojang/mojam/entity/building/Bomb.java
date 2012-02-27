package com.mojang.mojam.entity.building;

import java.util.Set;

import com.mojang.mojam.MojamComponent;
import com.mojang.mojam.Options;
import com.mojang.mojam.entity.Entity;
import com.mojang.mojam.entity.Player;
import com.mojang.mojam.entity.animation.LargeBombExplodeAnimation;
import com.mojang.mojam.entity.mob.Mob;
import com.mojang.mojam.entity.mob.Team;
import com.mojang.mojam.gui.Notifications;
import com.mojang.mojam.level.tile.Tile;
import com.mojang.mojam.screen.Art;
import com.mojang.mojam.screen.Bitmap;
import com.mojang.mojam.level.tile.RailTile;

/**
 * Bomb object. Triggered by bullets, destroys special wall tiles
 */
public class Bomb extends Building {
	/** Affected destruction radius around the bomb in pixels*/
	public static final double BOMB_DISTANCE = 50;
	
	private boolean hit = false;
	
	public static boolean creative = Options.getAsBoolean(Options.CREATIVE);
	private boolean doWarn = true;

	/**
	 * Constructor
	 * 
	 * @param x X coordinate
	 * @param y Y Coordinate
	 */
	public Bomb(double x, double y) {
		super(x, y, Team.Neutral);
		
		setStartHealth(8);
		yOffs = 2;
		setSize(7, 7);
		doShowHealthBar = false;
		buildingRegen(false);
	}

	@Override
	public void die() {
		level.addEntity(new LargeBombExplodeAnimation(pos.x, pos.y));
		MojamComponent.soundPlayer.playSound("/sound/Explosion 2.wav",
				(float) pos.x, (float) pos.y);

		Set<Entity> entities = level.getEntities(pos.x - BOMB_DISTANCE, pos.y
				- BOMB_DISTANCE, pos.x + BOMB_DISTANCE, pos.y + BOMB_DISTANCE,
				Mob.class);
		for (Entity e : entities) {
			double distSqr = pos.distSqr(e.pos);
			if (distSqr < (BOMB_DISTANCE * BOMB_DISTANCE)) {
				((Mob) e).hurt(this, 5);
			}
		}
	}

	@Override
	public boolean isNotFriendOf(Mob m) {
	    return true;
	}

	@Override
	public void onPickup(Mob mob) {
	    super.onPickup(mob);
	    yOffs = 4;
	}

	@Override
	public void onDrop() {
	    super.onDrop();
	    yOffs = 2;
	}

	public void tick() {
		if (!hit && carriedBy == null && level.getTile((int) pos.x/Tile.WIDTH, (int) pos.y/Tile.HEIGHT) 
				instanceof RailTile) railify(); else doWarn = true;
	    if (hit) {
	        if (freezeTime <= 0) {
	            hurtTime = 40;
	            freezeTime = 5;
	            health-=2;
	        }
	        if (health < 0) health = 0;
	        }

	    if (health <= 0) {
			if (--hurtTime <= 0) {
				die();
				remove();
				if (isCarried()) carriedBy.drop();
			}
			return;
		}

		super.tick();
		if (--freezeTime > 0)
			return;

	}

	public Bitmap getSprite() {
		return Art.bomb;
	}

	@Override
	public void hurt(Entity source, float damage) {
	}

	/**
	 * Mark this object as hit
	 */
	public void hit() {
		hit = true;
	}
	
	public void railify() {
		System.out.println("Railify "+this.getClass().getName());
		if (((Player)lastCarrying).getScore() > RailBomb.cost || creative) {
			((Player)lastCarrying).payCost(RailBomb.cost);
		  this.remove();
		  level.removeEntity(this);
		  level.removeFromEntityMap(this);
		  level.addEntity(new RailBomb(pos.x, pos.y, team));
		} else {
		  if (doWarn) Notifications.getInstance().add(
					MojamComponent.texts.upgradeNotEnoughMoney(RailBomb.cost));
		  doWarn = false;
		}
	}
}
