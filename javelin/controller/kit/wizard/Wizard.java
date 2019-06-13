package javelin.controller.kit.wizard;

import javelin.controller.kit.Kit;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.ability.RaiseAbility;
import javelin.controller.upgrade.ability.RaiseIntelligence;
import javelin.controller.upgrade.classes.Aristocrat;
import javelin.model.unit.feat.CombatCasting;
import javelin.model.unit.skill.Skill;
import javelin.model.world.location.fortification.Academy;
import javelin.model.world.location.town.labor.cultural.MagesGuild;

/**
 * Ideally, a kit that represents one of the eight schools of magic.
 *
 * @author alex
 */
public abstract class Wizard extends Kit{
	/** Constructor. */
	protected Wizard(String name,RaiseAbility ability){
		super(name,Aristocrat.SINGLETON,ability,RaiseIntelligence.SINGLETON);
		var lower=name.toLowerCase();
		titles=new String[]{"Fledgling $ "+lower,"Apprentice $ "+lower,"$ "+lower,
				"$ grand-"+lower};
	}

	@Override
	protected void define(){
		basic.add(Skill.CONCENTRATION.getupgrade());
		basic.add(Skill.SPELLCRAFT.getupgrade());
	}

	@Override
	protected void extend(UpgradeHandler h){
		extension.add(CombatCasting.SINGLETON.toupgrade());
	}

	@Override
	public Academy createguild(){
		return new MagesGuild(this);
	}
}