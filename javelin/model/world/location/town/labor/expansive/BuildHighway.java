package javelin.model.world.location.town.labor.expansive;

import javelin.controller.Point;
import javelin.model.world.World;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.Town;

public class BuildHighway extends BuildRoad {

	public BuildHighway() {
		super("Build highway");
	}

	@Override
	protected boolean hasroad(Point p) {
		return World.seed.highways[p.x][p.y];
	}

	@Override
	protected String name(Town target) {
		return "Build highway to " + target;
	}

	@Override
	protected void build(Point p) {
		World.seed.highways[p.x][p.y] = true;
	}

	@Override
	protected float getcost(Point p) {
		float cost = super.getcost(p);
		if (!super.hasroad(p)) {
			cost += cost;
		}
		return cost;
	}

	@Override
	protected void define() {
		if (town.getrank().rank >= Rank.TOWN.rank) {
			super.define();
		}
	}
}
