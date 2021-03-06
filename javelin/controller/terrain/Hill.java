package javelin.controller.terrain;

import java.util.Set;

import javelin.controller.terrain.hazard.Hazard;
import javelin.controller.terrain.hazard.Rockslide;
import javelin.controller.terrain.map.Maps;
import javelin.controller.terrain.map.hill.Gentle;
import javelin.controller.terrain.map.hill.Rugged;
import javelin.controller.terrain.map.tyrant.Graveyard;
import javelin.model.world.World;
import tyrant.mikera.engine.Point;

/**
 * Similar to {@link Plains}.
 * 
 * @author alex
 */
public class Hill extends Terrain {
	/** Constructor. */
	public Hill() {
		this.name = "hill";
		this.difficulty = -1;
		this.difficultycap = -4;
		this.speedtrackless = 1 / 2f;
		this.speedroad = 3 / 4f;
		this.speedhighway = 1f;
		this.visionbonus = +2;
		representation = '^';
	}

	@Override
	public Maps getmaps() {
		Maps m = new Maps();
		m.add(new Gentle());
		m.add(new Rugged());
		m.add(new Graveyard());
		return m;
	}

	@Override
	protected Point generatesource(World w) {
		Point source = super.generatesource(w);
		while (!w.map[source.x][source.y].equals(Terrain.FOREST)
				|| (checkadjacent(source, Terrain.MOUNTAINS, w, 1) == 0
						&& checkadjacent(source, Terrain.PLAIN, w, 1) == 0)) {
			source = super.generatesource(w);
		}
		return source;
	}

	@Override
	public Set<Hazard> gethazards(boolean special) {
		Set<Hazard> hazards = super.gethazards(special);
		if (special) {
			hazards.add(new Rockslide());
		}
		return hazards;
	}
}