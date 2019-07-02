package javelin.model.unit.abilities.spell.transmutation;

import java.util.List;

import javelin.controller.ai.ChanceNode;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.Realm;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.abilities.spell.Touch;
import javelin.model.unit.condition.Condition;

/**
 * See the d20 SRD for more info.
 */
public class Darkvision extends Touch{
	class DarkvisionCondition extends Condition{
		int original;

		/**
		 * Constructor.
		 *
		 * @param casterlevelp
		 */
		public DarkvisionCondition(Combatant c,Integer casterlevelp){
			super(c,"darkvision",Effect.NEUTRAL,casterlevelp,Float.MAX_VALUE,3);
		}

		@Override
		public void start(Combatant c){
			original=c.source.vision;
			c.source.vision=Monster.VISION_DARK;
		}

		@Override
		public void end(Combatant c){
			c.source.vision=Math.min(original,c.source.vision);
		}
	}

	/** Constructor. */
	public Darkvision(){
		super("Darkvision",2,ChallengeCalculator.ratespell(2),Realm.EVIL);
		castinbattle=true;
		castonallies=true;
		castoutofbattle=true;
		ispotion=true;
	}

	@Override
	public String castpeacefully(Combatant caster,Combatant target,
			List<Combatant> squad){
		target.addcondition(new DarkvisionCondition(target,casterlevel));
		return target+"'s eyes glow!";
	}

	@Override
	public String cast(Combatant caster,Combatant target,boolean saved,
			BattleState s,ChanceNode cn){
		return castpeacefully(caster,target,null);
	}
}
