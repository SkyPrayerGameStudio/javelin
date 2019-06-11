package javelin.controller.challenge.factor.quality;

import javelin.controller.challenge.factor.CrFactor;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.model.unit.Monster;
import javelin.model.unit.abilities.BreathWeapon;
import javelin.model.unit.abilities.BreathWeapon.BreathArea;

/**
 * @see CrFactor
 */
public class BreathFactor extends CrFactor{

	int[] CONERANGE=new int[]{5,10,15,20,30,40,50,60,70};
	int[] LINERANGE=new int[]{10,20,30,40,60,80,100,120,140};

	@Override
	public float calculate(final Monster monster){
		float total=0;
		for(BreathWeapon breath:monster.breaths){
			final float damage=breath.damage[0]*(breath.damage[1]+1)/2f;
			float breathcr=.03f*damage;
			final float typical=(breath.type==BreathArea.CONE?CONERANGE
					:LINERANGE)[monster.size];
			float doubling=0;
			if(breath.range>typical)
				doubling=breath.range/typical/2f;
			else if(breath.range<typical) doubling=-(typical/breath.range)/2f;
			breathcr+=.2f*doubling;
			if(breathcr<0) breathcr=0;
			if(!breath.delay) breathcr=breathcr*1.5f;
			total+=breathcr;
		}
		return total;
	}

	@Override
	public void registerupgrades(UpgradeHandler handler){
	}

	@Override
	public String log(Monster m){
		return m.breaths.isEmpty()?"":m.breaths.toString();
	}
}
