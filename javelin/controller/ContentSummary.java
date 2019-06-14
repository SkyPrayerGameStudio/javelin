package javelin.controller;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.event.urban.UrbanEvents;
import javelin.controller.event.wild.WildEvents;
import javelin.controller.kit.Kit;
import javelin.controller.terrain.Terrain;
import javelin.controller.upgrade.Upgrade;
import javelin.model.diplomacy.mandate.Mandate;
import javelin.model.item.Item;
import javelin.model.item.Tier;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.abilities.spell.conjuration.Summon;
import javelin.model.unit.skill.Skill.SkillUpgrade;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.town.labor.Deck;
import javelin.model.world.location.town.quest.Quest;
import javelin.model.world.location.unique.UniqueLocation;

/**
 * Procuces a summary log of {@link Upgrade}s, {@link Item}s and general
 * content.
 *
 * @author alex
 */
public class ContentSummary{
	FileWriter out;

	void print(String line) throws IOException{
		out.write(line+"\n");
	}

	void print() throws IOException{
		print("");
	}

	void printitems() throws IOException{
		for(var t:Tier.TIERS){
			var items=Item.BYTIER.get(t);
			print(t+"-tier items ("+items.size()+")");
			for(var i:items.sort())
				print(" - "+i+" ($"+Javelin.format(i.price)+")");
			print();
		}
	}

	void printkits() throws IOException{
		var kits=new ArrayList<>(Kit.KITS);
		kits.sort((a,b)->a.name.compareTo(b.name));
		for(var k:kits){
			var prestige=k.prestige?", prestige":"";
			var nupgrades=k.basic.size()+k.extension.size();
			var title=k.name+" ("+nupgrades+prestige+")";
			print(title);
			for(var u:k.basic)
				print(" - "+u);
			for(var u:k.extension)
				print(" + "+u);
			print();
		}
	}

	/** stats */
	public void produce(){
		if(!Javelin.DEBUG) return;
		try{
			out=new FileWriter("content.log");
			printmisc();
			print();
			printitems();
			printkits();
			out.close();
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	static int countsummon(Collection<Spell> spells){
		int summon=0;
		for(Spell s:spells)
			if(s instanceof Summon) summon+=1;
		return summon;
	}

	void printmisc() throws IOException{
		print(Javelin.ALLMONSTERS.size()+" monsters");
		print(Item.ALL.size()-Item.ARTIFACT.size()+" items, "+Item.ARTIFACT.size()
				+" artifacts, 7 relics");
		Collection<Spell> spells=Spell.BYNAME.values();
		var upgrades=Upgrade.getall();
		int nskills=Upgrade.getall(SkillUpgrade.class).size();
		int nupgrades=upgrades.size()-spells.size()-nskills;
		int nspells=spells.size()-countsummon(spells)+1;
		int nkits=Kit.KITS.size();
		print(nupgrades+" upgrades, "+nspells+" spells, "+nskills+" skills, "+nkits
				+" kits");
		printmaps();
		HashSet<Class<? extends Actor>> locationtypes=new HashSet<>();
		int uniquelocations=0;
		for(Actor a:World.getactors()){
			if(!(a instanceof Location)) continue;
			locationtypes.add(a.getClass());
			if(a instanceof UniqueLocation) uniquelocations+=1;
		}
		print(locationtypes.size()-uniquelocations+" world location types, "
				+uniquelocations+" unique locations");
		print(Deck.getsummary());
		print(Quest.printsummary());
		print(WildEvents.instance.printsummary("wilderness events"));
		print(UrbanEvents.instance.printsummary("Urban events"));
		print(Mandate.printsummary());
	}

	void printmaps() throws IOException{
		var total=Arrays.stream(Terrain.ALL)
				.collect(Collectors.summingInt(t->t.getmaps().size()));
		var byterrain=Arrays.stream(Terrain.ALL)
				.sorted((a,b)->a.name.compareTo(b.name))
				.map(t->t.getmaps().size()+" "+t).collect(Collectors.joining(", "));
		print(total+" battle maps ("+byterrain+")");
	}
}
