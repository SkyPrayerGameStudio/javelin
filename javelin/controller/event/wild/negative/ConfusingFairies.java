package javelin.controller.event.wild.negative;

import javelin.Javelin;
import javelin.controller.event.wild.WildEvent;
import javelin.controller.terrain.Terrain;
import javelin.model.unit.Squad;
import javelin.model.unit.skill.Concentration;
import javelin.model.unit.skill.Skill;
import javelin.model.world.location.PointOfInterest;

/**
 * {@link Squad} loses a number of hours depending on highest
 * {@link Concentration}.
 *
 * @author alex
 */
public class ConfusingFairies extends WildEvent{
	public ConfusingFairies(){
		super("Confusing fairies");
	}

	@Override
	public boolean validate(Squad s,int squadel,PointOfInterest l){
		return Terrain.get(l.x,l.y).equals(Terrain.FOREST);
	}

	@Override
	public void happen(Squad s,PointOfInterest l){
		int timelost=30-s.getbest(Skill.CONCENTRATION).roll(Skill.CONCENTRATION);
		if(timelost<1) timelost=1;
		Squad.active.hourselapsed+=timelost;
		Javelin.message("A group of fairies decides to play a trick on you.\n"
				+"You lose "+timelost+" hours while confused by their glamours.",true);
	}
}