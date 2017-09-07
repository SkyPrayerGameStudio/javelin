package javelin.model.unit.abilities.spell.conjuration.teleportation;

import java.util.ArrayList;
import java.util.Collections;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.terrain.Terrain;
import javelin.model.Realm;
import javelin.model.unit.Squad;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.attack.Combatant;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.dungeon.temple.Temple;
import javelin.model.world.location.fortification.Fortification;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.military.Academy;
import javelin.model.world.location.unique.UniqueLocation;

/**
 * Teleports {@link Squad#active} to any named {@link Location}.
 * 
 * @author alex
 */
public class GreaterTeleport extends Spell {
	/**
	 * If <code>true</code> will show each target's {@link Terrain} for a better
	 * informed decision.
	 */
	public boolean showterrain = false;

	/** Constructor. */
	public GreaterTeleport() {
		super("Greater teleport", 7,
				ChallengeRatingCalculator.ratespelllikeability(7), Realm.MAGIC);
		castinbattle = false;
		castonallies = false;
		castoutofbattle = true;
		isritual = true;
		isscroll = true;
	}

	@Override
	public String castpeacefully(Combatant caster, Combatant target) {
		ArrayList<Actor> places = new ArrayList<Actor>();
		ArrayList<String> names = new ArrayList<String>();
		for (Actor a : World.getall()) {
			if (a instanceof Town || a instanceof UniqueLocation
					|| a instanceof Temple || a instanceof Academy) {
				places.add(a);
				Fortification f = a instanceof Fortification ? (Fortification) a
						: null;
				String choice = f == null ? a.toString() : f.descriptionknown;
				if (showterrain) {
					choice += " (" + Terrain.get(a.x, a.y) + ")";
				}
				names.add(choice);
			}
		}
		Collections.sort(names);
		Actor to = places.get(Javelin.choose("Where to?", names, true, true));
		Squad.active.x = to.x;
		Squad.active.y = to.y;
		while (World.get(Squad.active.x, Squad.active.y) != Squad.active) {
			Squad.active.displace();
		}
		Squad.active.place();
		return null;
	}
}
