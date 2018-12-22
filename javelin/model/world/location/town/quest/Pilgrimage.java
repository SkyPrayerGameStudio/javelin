package javelin.model.world.location.town.quest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.challenge.Difficulty;
import javelin.controller.kit.Cleric;
import javelin.controller.kit.Monk;
import javelin.controller.kit.Paladin;
import javelin.controller.terrain.Terrain;
import javelin.controller.upgrade.Upgrade;
import javelin.model.item.Item;
import javelin.model.item.artifact.Artifact;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.world.location.town.Town;
import javelin.old.RPG;

/**
 * Visit holy sites and bring the faithful back to safety.
 *
 * @author alex
 */
public class Pilgrimage extends Quest{
	static final String DISBAND="Pilgrimage deadline expired, all pilgrims disband...";

	class Pilgrim extends Combatant{
		Pilgrim(Monster m){
			super(m,true);
			setmercenary(true);
			source.customName="Pilgrim";
		}

		@Override
		public int pay(){
			return 0;
		}

		@Override
		public void bury(){
			followers.remove(this);
		}

		@Override
		public String toString(){
			return source.customName;
		}

		@Override
		public void dismiss(Squad s){
			super.dismiss(s);
			followers.remove(this);
		}
	}

	class HolySite extends QuestMarker{
		static final String ESCORT="Do you want to escort the pilgrims leaving this holy site?\n"
				+"Press ENTER to confirm or any other key to cancel...";

		public HolySite(){
			super("holy site");
		}

		@Override
		public boolean interact(){
			if(Javelin.prompt(ESCORT)!='\n') return false;
			var pilgrims=generatepilgrims();
			Squad.active.members.addAll(pilgrims);
			followers.addAll(pilgrims);
			var result="A group of pilgrims joins you for protection.\n";
			remove();
			next=null;
			if(remaining==0)
				result+="Bring them back safely to "+town+".";
			else{
				next=new HolySite();
				next.place();
				remaining-=1;
				result+="They indicate the way to their next holy site.";
			}
			Javelin.message(result,true);
			return true;
		}
	}

	static HashSet<Upgrade> upgrades=new HashSet<>();

	static{
		upgrades.addAll(Cleric.INSTANCE.getupgrades());
		upgrades.addAll(Paladin.INSTANCE.getupgrades());
		upgrades.addAll(Monk.INSTANCE.getupgrades());
	}

	List<Combatant> followers=new ArrayList<>(0);
	HolySite next=null;
	int remaining;
	int targetpilgrimel;
	List<Monster> candidates;

	/** Reflection constructor. */
	public Pilgrimage(Town t){
		super(t);
		remaining=t.getrank().rank;
		remaining+=RPG.randomize(remaining);
		targetpilgrimel=Math.max(2,town.population+4+Difficulty.VERYEASY);
		distance=t.getdistrict().getradius()*3;
		candidates=Terrain.get(town.x,town.y).getmonsters().stream()
				.filter(m->m.cr<=targetpilgrimel&&!m.alignment.isevil()&&m.think(-1))
				.collect(Collectors.toList());
	}

	@Override
	protected void define(){
		super.define();
		remaining-=1;
		next=new HolySite();
		next.place();
	}

	List<Combatant> generatepilgrims(){
		var npilgrims=RPG.rolldice(2,4);
		var group=new ArrayList<Combatant>(npilgrims);
		while(group.isEmpty()
				||ChallengeCalculator.calculateel(group)<targetpilgrimel){
			group.add(new Pilgrim(RPG.pick(candidates)));
			npilgrims-=1;
			if(npilgrims==0) break;
		}
		while(ChallengeCalculator.calculateel(group)<targetpilgrimel)
			Combatant.upgradeweakest(group,upgrades);
		return group;
	}

	@Override
	public boolean validate(){
		return !candidates.isEmpty();
	}

	@Override
	protected String getname(){
		return "Pilgrimage";
	}

	@Override
	public boolean complete(){
		if(next!=null) return false;
		for(var c:followers)
			if(!Squad.active.members.contains(c)) return false;
		removepilgrims();
		return true;
	}

	@Override
	public boolean cancel(){
		var cancelled=super.cancel()||next==null&&followers.isEmpty();
		if(cancelled){
			if(next!=null) next.remove();
			if(removepilgrims()) Javelin.message(DISBAND,true);
		}
		return cancelled;
	}

	boolean removepilgrims(){
		var inprogress=false;
		for(var s:Squad.getsquads()){
			var items=new ArrayList<Item>(0);
			for(var pilgrim:new ArrayList<>(s.members)){
				if(!followers.contains(pilgrim)) continue;
				inprogress=true;
				for(var i:s.equipment.get(pilgrim)){
					if(i instanceof Artifact) ((Artifact)i).remove(pilgrim);
					items.add(i);
				}
				s.remove(pilgrim);
				followers.remove(pilgrim);
			}
			for(var i:items)
				s.equipment.get(RPG.pick(s.members)).add(i);
		}
		return inprogress;
	}
}