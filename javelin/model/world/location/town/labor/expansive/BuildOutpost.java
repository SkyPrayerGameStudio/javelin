package javelin.model.world.location.town.labor.expansive;

import java.util.ArrayList;

import javelin.controller.Point;
import javelin.model.world.Actor;
import javelin.model.world.location.Location;
import javelin.model.world.location.Outpost;
import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Build;

public class BuildOutpost extends Build {
	public BuildOutpost() {
		super("Build outpost", 5, null, Town.HAMLET);
	}

	@Override
	public Location getgoal() {
		return new Outpost();
	}

	@Override
	public boolean validate(District d) {
		if (!super.validate(d)) {
			return false;
		}
		if (site != null) {
			return true;
		}
		if (site == null && d.getlocationtype(Outpost.class)
				.size() >= d.town.getrank().rank) {
			return false;
		}
		return super.validate(d) && getsitelocation() != null;
	}

	@Override
	protected Point getsitelocation() {
		District d = town.getdistrict();
		ArrayList<Point> free = d.getfreespaces();
		for (Point p : free) {
			if (town.distance(p.x, p.y) == d.getradius()) {
				return p;
			}
		}
		return null;
	}

	@Override
	protected void done(Actor goal) {
		super.done(goal);
		if (!town.ishostile()) {
			Outpost.discover(goal.x, goal.y, Outpost.VISIONRANGE);
		}
	}
}