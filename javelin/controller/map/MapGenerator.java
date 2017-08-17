package javelin.controller.map;

import javelin.controller.db.Preferences;
import javelin.controller.terrain.Terrain;

/**
 * Selects and generates a map for a battle.
 * 
 * @author alex
 */
public class MapGenerator {
	/** TODO "dark tower" doesn't work - try to fix it? */
	static public Map generatebattlemap(Terrain t, boolean dungeon) {
		if (Preferences.DEBUGMAPTYPE != null) {
			try {
				Map m = (Map) Class
						.forName(MapGenerator.class.getPackage().getName() + "."
								+ Preferences.DEBUGMAPTYPE)
						.newInstance();
				return m;
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(
						"Cannot load map: " + Preferences.DEBUGMAPTYPE);
			}
		}
		if (dungeon) {
			t = Terrain.UNDERGROUND;
		}
		return t.getmaps().pick();
	}
}
