package javelin.model.world.location.fortification;

import java.util.Collections;
import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.fight.Siege;
import javelin.controller.terrain.Terrain;
import javelin.model.item.Item;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.world.Actor;
import javelin.old.RPG;

/**
 * Single creature that guards an {@link Item}.
 *
 * @author alex
 */
public class Guardian extends Fortification{
	private Item loot;

	/** Constructor. */
	public Guardian(){
		super(null,null,0,0);
		generate();
		generategarrison(0,0);
		descriptionknown="A guardian ("+loot.toString().toLowerCase()+")";
		descriptionunknown="A guardian";
		discard=true;
		link=false;
		vision=0;
	}

	@Override
	protected void generategarrison(int minel,int maxel){
		while(garrison.isEmpty()){
			loot=RPG.pick(Item.randomize(Item.ARTIFACT));
			int cr=ChallengeCalculator.goldtocr(loot.price);
			Combatant c=findnativemonster(cr,this);
			if(c!=null){
				garrison.add(c);
				targetel=new Integer(ChallengeCalculator.crtoel(cr));
			}
		}
	}

	/**
	 * @param cr Given a challenge rating target...
	 * @param spot and a world location...
	 * @return a monster from that location and that challenge rating or
	 *         <code>null</code> if couldn't find one.
	 */
	static public Combatant findnativemonster(float cr,Actor spot){
		String terrain=Terrain.get(spot.x,spot.y).toString();
		while(true){
			if(Monster.BYCR.descendingKeySet().first()<cr) return null;
			List<Monster> candidates=Monster.BYCR.get(cr);
			if(candidates!=null){
				Collections.shuffle(candidates);
				for(Monster m:candidates)
					if(m.getterrains().contains(terrain))
						return new Combatant(m.clone(),true);
			}
			cr+=1;
		}
	}

	@Override
	protected Siege fight(){
		Siege fight=new Siege(this);
		fight.rewardgold=false;
		return fight;
	}

	@Override
	public boolean interact(){
		if(!super.interact()) return false;
		loot.grab();
		return true;
	}

	@Override
	public List<Combatant> getcombatants(){
		return garrison;
	}
}
