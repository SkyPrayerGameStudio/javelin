package javelin.model.world.location.dungeon.temple.features;

import java.util.ArrayList;
import java.util.Collections;

import javelin.Javelin;
import javelin.JavelinApp;
import javelin.controller.Point;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.Feature;
import javelin.model.world.location.dungeon.Trap;
import javelin.model.world.location.dungeon.temple.GoodTemple;
import javelin.view.screen.DungeonScreen;

/**
 * @see GoodTemple
 * @author alex
 */
public class Spirit extends Feature {

	/** Constructor. */
	public Spirit(int xp, int yp) {
		super("dog", xp, yp, "dungeonspirit");
	}

	@Override
	public boolean activate() {
		ArrayList<Feature> features = new ArrayList<Feature>(
				Dungeon.active.features);
		Collections.shuffle(features);
		Feature show = null;
		for (Feature f : features) {
			if (!Dungeon.active.visible[f.x][f.y]) {
				show = f;
				break;
			}
			if (f instanceof Feature && !f.draw) {
				show = f;
				break;
			}
		}
		if (show == null) {
			Javelin.message("The spirit flees from your presence in shame...",
					false);
			return true;
		}
		Dungeon.active.setvisible(show.x, show.y);
		DungeonScreen.active.center(show.x, show.y);
		Javelin.message("'Hey, look!'", false);
		if (show instanceof Trap) {
			((Trap) show).discover();
		}
		Point p = JavelinApp.context.getherolocation();
		JavelinApp.context.view(p.x, p.y);
		return true;
	}
}
