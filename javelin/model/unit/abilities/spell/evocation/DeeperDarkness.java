package javelin.model.unit.abilities.spell.evocation;

import java.util.List;

import javelin.Javelin;
import javelin.controller.ai.ChanceNode;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Spell;

/**
 * See the d20 SRD for more info.
 */
public class DeeperDarkness extends Spell{
	/** Constructor. */
	public DeeperDarkness(){
		super("Deeper darkness",3,.15f);
		castinbattle=true;
	}

	@Override
	public String cast(Combatant caster,Combatant target,boolean saved,
			BattleState s,ChanceNode cn){
		s.period=Javelin.PERIODNIGHT;
		return "Light dims!";
	}

	@Override
	public void filtertargets(final Combatant combatant,
			final List<Combatant> targets,final BattleState s){
		targetself(combatant,targets);
	}
}
