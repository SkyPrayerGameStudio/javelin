package javelin.model.world.location.dungeon.feature;

import java.util.List;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.action.world.WorldMove;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.condition.TemporarySpell;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.old.RPG;
import javelin.view.screen.WorldScreen;

/**
 * Grants a {@link TemporarySpell} to any unit sitting in it, once every 24
 * hours.
 *
 * TODO may want to extract a common superclass MagicDevice with {@link Mirror}
 * (and make mirror activatable once every 24h).
 *
 * @author alex
 */
public class Throne extends Feature{
	/** Spell to be given as a {@link TemporarySpell}. */
	public Spell spell=null;
	/** Only activates after 24 hours of last use. */
	public long lastuse=-24;
	boolean revealed=false;

	/** @param dungeonlevel Caster level to base {@link #spell} selection on. */
	public Throne(int dungeonlevel){
		super("dungeonthrone");
		enter=true;
		remove=false;
		for(var level=dungeonlevel;spell==null;level--){
			var eligible=filterspells(level);
			if(!eligible.isEmpty()) spell=RPG.pick(eligible);
		}
	}

	/** Java Reflection-friendly constructor. */
	public Throne(){
		this(Dungeon.active.level);
	}

	/**
	 * @return Given a {@link Combatant} level or {@link Dungeon} level, return a
	 *         list of combat spells that are relevant. May be empty if cannot
	 *         find any, so decrementing the level until a valid result is given
	 *         is advised.
	 *
	 * @see Monster#cr
	 * @see Spell#castinbattle
	 */
	public static List<Spell> filterspells(final int level){
		return Spell.SPELLS.stream()
				.filter(s->s.castinbattle&&s.level==Spell.getmaxlevel(level))
				.collect(Collectors.toList());

	}

	@Override
	public boolean activate(){
		if(Javelin.gettime()-lastuse<24){
			String inert="You approach the throne but nothing happens right now...";
			Javelin.message(inert,false);
			return false;
		}
		var prompt="Do you want to sit on the magic throne?\n";
		String name=spell.name.toLowerCase();
		if(revealed) prompt+="The throne grants: "+name+".\n";
		prompt+="Press ENTER to confirm or any other key to cancel...";
		if(Javelin.prompt(prompt)!='\n') return false;
		var combatant=Mirror.selectmember("Who will sit on the throne?");
		if(combatant==null){
			WorldMove.abort=true;
			return false;
		}
		combatant.addcondition(new TemporarySpell("Throne gift",spell,combatant));
		lastuse=Javelin.gettime();
		var result=combatant+" has been gifted with a use of "+name+"!";
		Javelin.app.switchScreen(WorldScreen.current);
		Javelin.message(result,false);
		revealed=true;
		return true;
	}
}
