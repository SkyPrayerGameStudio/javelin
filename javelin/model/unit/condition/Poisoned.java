package javelin.model.unit.condition;

import javelin.Javelin;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.unit.abilities.spell.conjuration.healing.NeutralizePoison.Neutralized;
import javelin.model.unit.skill.Heal;
import javelin.model.unit.skill.Skill;

/**
 * A poisoned unit takes a certain amount of constitution damage immediately and
 * a secondary amount after a short while.
 *
 * TODO implement {@link #merge(Condition)}
 *
 * @see Monster#changeconstitutionmodifier(Combatant, int)
 * @author alex
 */
public class Poisoned extends Condition{
	int secondary;
	int dc;
	/** See {@link Neutralized}. */
	public boolean neutralized;

	/**
	 * Constructor.
	 *
	 * @param secondary The positive amount of secondary damage to cause.
	 * @param dcp DC for a {@link Heal} check to ignore secondary effects of the
	 *          poison.
	 * @param casterlevelp
	 *
	 * @see Monster#changeconstitutionmodifier(Combatant, int)
	 */
	public Poisoned(float expireatp,Combatant c,Effect effectp,int secondary,
			int dcp,Integer casterlevelp){
		super(c,"poisoned",effectp,casterlevelp,expireatp,1);
		this.secondary=secondary;
		dc=dcp;
		neutralized=c.hascondition(Neutralized.class)!=null;
	}

	@Override
	public void start(Combatant c){
		if(neutralized)
			c.removecondition(this);
		else
			damage(c,3);
	}

	void damage(Combatant c,int d){
		d=d*2;
		int original=c.source.constitution;
		c.source.changeconstitutionscore(c,-d);
		c.source.poison+=original-c.source.constitution;
	}

	@Override
	public void end(Combatant c){
		int heal=Javelin.app.fight==null?Squad.active.heal():c.taketen(Skill.HEAL);
		if(!neutralized&&heal<dc) damage(c,secondary);
	}

	@Override
	public void transfer(Combatant from,Combatant to){
		int poison=from.source.poison;
		int original=to.source.constitution;
		to.source.changeconstitutionscore(to,-poison);
		to.source.poison=Math.min(poison,original-to.source.constitution);
		to.maxhp=from.maxhp;
		to.hp=from.hp;
	}

	@Override
	public void dispel(){
		neutralized=true;
	}
}
