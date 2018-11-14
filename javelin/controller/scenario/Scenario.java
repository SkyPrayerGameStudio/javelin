package javelin.controller.scenario;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javelin.Javelin;
import javelin.controller.action.world.Guide;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.fight.Fight;
import javelin.controller.fight.RandomEncounter;
import javelin.controller.fight.minigame.Minigame;
import javelin.controller.generator.WorldGenerator;
import javelin.controller.generator.feature.FeatureGenerator;
import javelin.controller.generator.feature.Frequency;
import javelin.controller.kit.Kit;
import javelin.controller.terrain.Desert;
import javelin.controller.terrain.Terrain;
import javelin.controller.terrain.Water;
import javelin.controller.terrain.hazard.Hazard;
import javelin.controller.upgrade.Upgrade;
import javelin.controller.wish.Win;
import javelin.model.Realm;
import javelin.model.item.Item;
import javelin.model.item.key.TempleKey;
import javelin.model.transport.Transport;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.world.Actor;
import javelin.model.world.Caravan;
import javelin.model.world.Incursion;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.feature.Altar;
import javelin.model.world.location.dungeon.feature.Chest;
import javelin.model.world.location.dungeon.temple.Temple;
import javelin.model.world.location.fortification.Fortification;
import javelin.model.world.location.fortification.RealmAcademy;
import javelin.model.world.location.fortification.Trove;
import javelin.model.world.location.order.Order;
import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Deck;
import javelin.model.world.location.town.labor.Labor;
import javelin.model.world.location.town.labor.productive.Shop;
import javelin.model.world.location.unique.UniqueLocation;
import javelin.old.RPG;
import javelin.view.mappanel.world.WorldTile;
import javelin.view.screen.SquadScreen;

/**
 * Scenario mode is a much faster type of gameplay than the main
 * {@link Campaign} mode. It's supposed to be finished on anywhere from 2 hours
 * of play to an afternoon (but of course it can be saved an resumed too).
 *
 * The world is a lot more static in this mode. Several features and
 * {@link Location}s are disabled - including {@link RandomEncounter}s in ohe
 * overworld map and {@link Hazard}s. The only "moving pieces" in the world map
 * are yourself and {@link Incursion}s.
 *
 * The {@link FeatureGenerator} is disabled after the original world is created,
 * meaning that, wuthout random encounters and other infinite means of gaining
 * experience and loot, you are on a race against time to conquer all hostile
 * {@link Town}s - 1 to 3, with varying degress of power according to the
 * quantity in each game.
 *
 * There is only one enemy {@link Realm} per game and the starting features are
 * roughly made to be 1/3 neutral and 2/3 hostile.
 */
public class Scenario implements Serializable{
	/** {@link World} size. */
	public int size=30;
	/**
	 * Allow access to {@link Minigame}s or not.
	 *
	 * TODO may allow access but should fix them for scenario mode where their
	 * {@link UniqueLocation}s aren't present.
	 */
	public boolean minigames=false;
	/**
	 * Starting {@link Town} population. See {@link #statictowns}.
	 */
	public int startingpopulation=6;
	/**
	 * If <code>true</code>, {@link Town}s will be overidden after {@link World}
	 * generation according to {@link #getscenariochallenge()}.
	 */
	public boolean statictowns=true;
	/**
	 * If not <code>null</code>, this amount will be seeded during {@link World}
	 * generation. It will also be the cap as per {@link Frequency#max}.
	 *
	 * Number of starting dungeons in the {@link World} map. Since
	 * {@link TempleKey}s are important to {@link Win}ning the game this should be
	 * a fair amount, otherwise the player will depend only on {@link Caravan}s if
	 * too many dungeons are destroyed or if unable to find the proper
	 * {@link Chest} inside the dungeons he does find. Not that dungeons also
	 * spawn during the course of a game but since this is highly randomized a
	 * late-game player who ran out of dungeons should not be required to depend
	 * on that alone.
	 *
	 * @see Actor#destroy(Incursion)
	 * @see FeatureGenerator
	 */
	public Integer startingdungeons=null;
	public boolean respawnlocations=false;
	public boolean normalizemap=true;
	/** Wheter {@link TempleKey}s should exist at all. */
	public boolean lockedtemples=false;
	/**
	 * <code>true</code> if first {@link Town} should be located on
	 * {@link Terrain#PLAIN} or {@link Terrain#HILL}.
	 */
	public boolean easystartingtown=false;
	/** Minimum distance between {@link Desert} and {@link Water}. */
	public int desertradius=1;
	/** Number of {@link Town}s in the {@link World}. */
	public int towns=RPG.r(1+1,1+3);
	/**
	 * Will clear locations as indicated by {@link Fortification#clear}.
	 */
	public boolean clearlocations=true;
	/** Wheter a full {@link Deck} should be allowed. */
	public boolean allowlabor=false;
	/**
	 * If <code>false</code>, only allow Actors marked as
	 * {@link Actor#allowedinscenario}.
	 */
	public boolean allowallactors=false;
	/** Ask for {@link Monster} names on {@link SquadScreen}. */
	public boolean asksquadnames=false;
	/** Wheter to cover {@link WorldTile}s. */
	public boolean fogofwar=false;
	public boolean worldencounters=false;
	public boolean worldhazards=false;
	/** File name for the F1 help {@link Guide}. */
	public String helpfile="Scenario";
	/**
	 * TODO highscores should be scenario-agnostic
	 */
	public boolean record=false;
	public boolean dominationwin=true;
	/** Number of {@link Location}s to spawn. See {@link FeatureGenerator}. */
	public int startingfeatures=size*size/7;
	/** {@link Trove}s will only offer gold and experience rewards. */
	public boolean simpletroves=true;
	/**
	 * Make random {@link RealmAcademy} and {@link Shop}s, insted of local
	 * {@link Realm}.
	 */
	public boolean randomrealms=true;
	/**
	 * Affect labor and training speeds and amounts for XP and gold rewards.
	 *
	 * @see Labor#work(float)
	 * @see RewardCalculator
	 * @see Order
	 */
	public int boost=3;
	/**
	 * If <code>true</code> will try to generate all possible {@link Location}s
	 * around the world.
	 */
	public boolean worlddistrict=true;

	/**
	 * If <code>true</code>, hostile {@link Location}s will spawn more monsters
	 * over time.
	 *
	 * @see Location#ishostile()
	 * @see Location#spawn()
	 * @see Incursion
	 */
	public boolean spawn=false;
	public boolean expiredungeons=false;
	public Class<? extends FeatureGenerator> featuregenerator=FeatureGenerator.class;
	/** Multiplied to daily {@link Labor}. */
	public float labormodifier=1;
	public Class<? extends WorldGenerator> worldgenerator=WorldGenerator.class;
	public boolean roads=false;
	public boolean boats=false;
	/**
	 * Adds this many squares to {@link District}s.
	 *
	 * @see District#getradius()
	 */
	public int districtmodifier=0;
	/**
	 * Whether all-flying or all-swimming {@link Squad}s can cross {@link Water}.
	 * Does not impede {@link Transport}s from doing so.
	 *
	 * @see Squad#swim()
	 * @see Monster#swim
	 * @see Monster#fly
	 */
	public boolean crossrivers=true;

	/**
	 * @return Starting encounter level for each hostile town in {@link #SCENARIO}
	 *         mode. 20 for 1 hostile town, 15 for 2 or 10 for 3.
	 */
	public static int getscenariochallenge(){
		return new int[]{20,15,10}[Town.gettowns().size()-2];
	}

	/**
	 * {@link Upgrade} or not the starting squad after it's been selected.
	 *
	 * @see SquadScreen
	 */
	public void upgradesquad(ArrayList<Combatant> squad){
		ArrayList<Combatant> members=new ArrayList<>(squad);
		HashSet<Kit> chosen=new HashSet<>(members.size());
		while(!members.isEmpty()){
			Combatant c=members.get(0);
			Kit kit=null;
			List<Kit> kits=Kit.getpreferred(c.source);
			Collections.shuffle(kits);
			for(Kit k:kits){
				kit=k;
				if(!chosen.contains(kit)) break;
			}
			chosen.add(kit);
			c.source.customName=Character.toUpperCase(kit.name.charAt(0))
					+kit.name.substring(1);
			while(c.source.cr<6)
				c.upgrade(kit.basic);
			members.remove(0);
		}
	}

	/**
	 * @return <code>true</code> if the current selection is enough to start the
	 *         game.
	 * @see SquadScreen
	 */
	public boolean checkfullsquad(ArrayList<Combatant> squad){
		return squad.size()>=4;
	}

	public boolean win(){
		for(Town t:Town.gettowns())
			if(t.ishostile()) return false;
		String win="Congratulations, you have won this scenario!\nThanks for playing!";
		Javelin.show(win);
		return true;
	}

	public List<Location> generatestartinglocations(World seed){
		HashSet<Realm> realms=new HashSet<>(2);
		for(Town t:Town.gettowns())
			realms.add(t.originalrealm);
		ArrayList<Location> shops=new ArrayList<>();
		for(Realm r:Realm.values())
			if(!realms.contains(r)) shops.add(new Shop(r,false));
		return shops;
	}

	public String getsaveprefix(){
		return getClass().getSimpleName().toLowerCase();
	}

	/**
	 * A special {@link Chest} is found in each {@link Dungeon}.
	 *
	 * @return Item inside the Chest.
	 * @see #openaltar(Temple)
	 */
	public Item openspecialchest(Dungeon d){
		throw new UnsupportedOperationException();
	}

	/**
	 * The deepest floor of each {@link Temple} doesn't contain a special
	 * {@link Chest} but an {@link Altar} instead.
	 *
	 * @return Item inside the Altar.
	 * @see #openspecialchest(Dungeon)
	 */
	public Item openaltar(Temple t){
		throw new UnsupportedOperationException();
	}

	/**
	 * Called at the end of each day. Might be called several times in a row - for
	 * example: if a lone {@link Squad} decides to rest for a week, it'll be
	 * called 7 times in a row.
	 *
	 * @param day
	 */
	public void endday(double day){
		// nothing
	}

	/**
	 * Setup or interact with the user before {@link World} generation starts.
	 *
	 * @see WorldGenerator
	 */
	public void setup(){
		Squad.active=new SquadScreen().open();
	}

	/** Called when a Fight starts. */
	public void start(Fight f,List<Combatant> blue,List<Combatant> red){
		// nothing by default
	}

	/** Called when a Fight ends. */
	public void end(Fight f,boolean victory){
		// nothing by default
	}

	/**
	 * Called only once, after {@link World} generation ends.
	 *
	 * @param w
	 *
	 * @see WorldGenerator#finish(Location, World)
	 */
	public void ready(World w){
		//nothing by default
	}

	/** Called at the end of @link World} or {@link Dungeon} actions. */
	public void endturn(){
		// nothing by default
	}
}
