package javelin.controller.terrain;

import java.util.Set;

import javelin.controller.Point;
import javelin.controller.map.Maps;
import javelin.controller.map.terrain.marsh.Moor;
import javelin.controller.map.terrain.marsh.Swamp;
import javelin.controller.terrain.hazard.Break;
import javelin.controller.terrain.hazard.Cold;
import javelin.controller.terrain.hazard.Flood;
import javelin.controller.terrain.hazard.GettingLost;
import javelin.controller.terrain.hazard.Hazard;
import javelin.model.world.World;

/**
 * Bog, swamp.
 *
 * @author alex
 */
public class Marsh extends Terrain {
	/** Constructor. */
	public Marsh() {
		name = "marsh";
		difficulty = +2;
		difficultycap = -1;
		speedtrackless = 1 / 2f;
		speedroad = 3 / 4f;
		speedhighway = 1f;
		visionbonus = -2;
		representation = 'm';
		liquid = true;
	}

	@Override
	public Maps getmaps() {
		Maps m = new Maps();
		m.add(new Moor());
		m.add(new Swamp());
		return m;
	}

	@Override
	protected Point generatesource(World w) {
		Point source = super.generatesource(w);
		while (!w.map[source.x][source.y].equals(Terrain.FOREST)
				&& search(source, WATER, 1, w) == 0) {
			source = super.generatesource(w);
		}
		return source;
	}

	@Override
	public Set<Hazard> gethazards(boolean special) {
		Set<Hazard> hazards = super.gethazards(special);
		hazards.add(new GettingLost(10));
		hazards.add(new Cold());
		if (special) {
			hazards.add(new Flood());
			hazards.add(new Break());
		}
		return hazards;
	}
}