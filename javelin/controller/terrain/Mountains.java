package javelin.controller.terrain;

import java.util.Set;

import javelin.controller.Point;
import javelin.controller.map.Maps;
import javelin.controller.map.terrain.hill.Rugged;
import javelin.controller.map.terrain.mountain.Forbidding;
import javelin.controller.map.terrain.mountain.Meadow;
import javelin.controller.terrain.hazard.Break;
import javelin.controller.terrain.hazard.Cold;
import javelin.controller.terrain.hazard.Hazard;
import javelin.controller.terrain.hazard.Rockslide;
import javelin.model.world.World;

/**
 * High altitude, snowy on winter.
 * 
 * @author alex
 */
public class Mountains extends Terrain {
	/** Constructor. */
	public Mountains() {
		this.name = "mountains";
		this.difficulty = +1;
		this.difficultycap = -2;
		this.speedtrackless = 1 / 2f;
		this.speedroad = 3 / 4f;
		this.speedhighway = 3 / 4f;
		this.visionbonus = +4;
		representation = 'M';
	}

	@Override
	public Maps getmaps() {
		Maps m = new Maps();
		m.add(new Meadow());
		m.add(new Rugged());
		m.add(new Forbidding());
		return m;
	}

	@Override
	protected Point generatesource(World w) {
		Point source = super.generatesource(w);
		while (!w.map[source.x][source.y].equals(Terrain.FOREST)
				&& search(source, MOUNTAINS, 1, w) == 0) {
			source = super.generatesource(w);
		}
		return source;
	}

	@Override
	public Set<Hazard> gethazards(boolean special) {
		Set<Hazard> hazards = super.gethazards(special);
		hazards.add(new Cold());
		if (special) {
			hazards.add(new Rockslide());
			hazards.add(new Break());
		}
		return hazards;
	}
}