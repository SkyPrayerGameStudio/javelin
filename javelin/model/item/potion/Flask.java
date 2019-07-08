package javelin.model.item.potion;

import java.util.List;

import javelin.model.item.Item;
import javelin.model.item.Recharger;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Spell;

/**
 * Flaks are {@link Potion}s that can be {@link #refresh(int)}-ed.
 *
 * @author alex
 */
public class Flask extends Potion{
	/**
	 * How many types of Flasks to generate, {@link #capacity}-wise.
	 *
	 * TODO eventually want 1-5 enabled, but cannot overwhelmed other {@link Item}
	 * types.
	 */
	public static final List<Integer> VARIATIONS=List.of(5);

	Recharger charges;

	/** Constructor. */
	public Flask(Spell s,int capacity){
		super("Flask",s,
				s.level*s.casterlevel*2000/(5.0/capacity)+s.components*capacity,true);
		charges=new Recharger(capacity);
		consumable=false;
		waste=true;
	}

	void quaff(){
		if(charges.discharge()){
			usedinbattle=false;
			usedoutofbattle=false;
		}
	}

	@Override
	public boolean use(Combatant user){
		if(!super.use(user)) return false;
		quaff();
		return true;
	}

	@Override
	public boolean usepeacefully(Combatant user){
		if(!super.usepeacefully(user)) return false;
		quaff();
		return true;
	}

	@Override
	public void refresh(int hours){
		super.refresh(hours);
		if(charges.recharge(hours)){
			usedinbattle=spell.castinbattle;
			usedoutofbattle=spell.castoutofbattle;
		}
	}

	@Override
	public String toString(){
		return super.toString()+" "+charges;
	}
}