package javelin.controller.map.location.haunt;

import javelin.controller.map.location.LocationMap;
import javelin.view.Images;

public class WitchesHideoutMap extends LocationMap {
	public WitchesHideoutMap() {
		super("witcheshideout");
		wall = Images.getImage("terrainforest");
		floor = Images.getImage("terrainwoodfloor");
	}
}