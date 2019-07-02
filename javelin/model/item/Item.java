package javelin.model.item;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import javelin.Javelin;
import javelin.controller.action.UseItem;
import javelin.controller.action.world.UseItems;
import javelin.controller.comparator.ItemsByPrice;
import javelin.controller.exception.battle.StartBattle;
import javelin.model.item.artifact.Artifact;
import javelin.model.item.artifact.CasterRing;
import javelin.model.item.consumable.Eidolon;
import javelin.model.item.consumable.Potion;
import javelin.model.item.consumable.Scroll;
import javelin.model.item.precious.ArtPiece;
import javelin.model.item.precious.Gem;
import javelin.model.item.wand.Rod;
import javelin.model.item.wand.Wand;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.model.world.location.Academy;
import javelin.model.world.location.dungeon.feature.Chest;
import javelin.model.world.location.order.Order;
import javelin.model.world.location.order.TrainingOrder;
import javelin.model.world.location.town.Town;
import javelin.old.RPG;
import javelin.view.screen.WorldScreen;

/**
 * Represents an item carried by a {@link Combatant}. Most often items are
 * consumable. Currently only human players use items.
 *
 * When crafting new items, it takes a day per $1000 for the process to
 * complete. Exceptions are {@link Potion}s, which always take 1 day.
 *
 * @author alex
 */
public abstract class Item implements Serializable,Cloneable{
	/**
	 * All available item types from cheapest to most expensive.
	 */
	public static final ItemSelection ITEMS=new ItemSelection();
	/** Map of items by price in gold coins ($). */
	public static final TreeMap<Integer,ItemSelection> BYPRICE=new TreeMap<>();
	/** Map of items by price {@link Tier} */
	public static final HashMap<Tier,ItemSelection> BYTIER=new HashMap<>();
	/** @see Artifact */
	public static final ItemSelection ARTIFACT=new ItemSelection();
	/** Price of the cheapest {@link Artifact} after loot registration. */
	public static Integer cheapestartifact=null;

	static{
		for(Tier t:Tier.TIERS)
			BYTIER.put(t,new ItemSelection());
	}

	/** Name to show the player. */
	public String name;
	/** Cost in gold pieces. */
	public int price;
	/**
	 * <code>true</code> if can be used during battle . <code>true</code> by
	 * default (default: true).
	 */
	public boolean usedinbattle=true;
	/**
	 * <code>true</code> if can be used while in the world map (default: true).
	 */
	public boolean usedoutofbattle=true;
	/** <code>true</code> if should be expended after use (default: true). */
	public boolean consumable=true;
	/** How many action points to spend during {@link UseItem}. */
	public float apcost=.5f;

	/** Whether to {@link #waste(float, ArrayList)} this item or not. */
	public boolean waste=true;
	/**
	 * Usually only {@link Scroll}s and {@link Potion}s provoke attacks of
	 * opportunity.
	 */
	public boolean provokesaoo=true;
	/** Whether to select a {@link Combatant} to use this on. */
	public boolean targeted=true;

	/**
	 * A value between 0 and 1 signifying how much this item should be (re)sold
	 * for. A value of zero means the item can't be sold.
	 *
	 * Default is 50% original {@link #price}.
	 */
	public double sellvalue=.5f;
	/** If not <code>null</code> will be used for {@link #describefailure()}. */
	volatile protected String failure=null;

	/**
	 * @param upgradeset One the static constants in this class, like
	 *          {@link #MAGIC}.
	 */
	public Item(final String name,final int price,boolean register){
		this.name=name;
		this.price=Javelin.round(price);
		if(register) register();
	}

	/** Register this item as a generation/purhcase option. */
	protected void register(){
		if(ITEMS.add(this)) BYTIER.get(Tier.get(getlevel())).add(this);
	}

	/**
	 * @return A clone of this item (base implementation), with randomized
	 *         parameters, meant for generating a new item to be found in a
	 *         {@link Chest}, for example. Examples being: a used {@link Wand}
	 *         with a certain amount of charges or a {@link Gem} with a random
	 *         sell value.
	 */
	public Item randomize(){
		return clone();
	}

	public int getlevel(){
		final double level=Math.pow(price/7.5,1.0/3.0);
		return Math.max(1,Math.round(Math.round(level)));
	}

	@Override
	public String toString(){
		return name;
	}

	/**
	 * @return <code>true</code> if item was spent.
	 */
	public boolean use(Combatant user){
		throw new RuntimeException("Not used in combat: "+this);
	}

	/**
	 * Uses an item while on the {@link WorldScreen}.
	 *
	 * @param m Unit using the item.
	 * @return <code>true</code> if item is to be expended.
	 */
	public boolean usepeacefully(Combatant user){
		throw new RuntimeException("Not used peacefully: "+this);
	}

	@Override
	public boolean equals(final Object obj){
		return obj instanceof Item&&name.equals(((Item)obj).name);
	}

	@Override
	public int hashCode(){
		return name.hashCode();
	}

	@Override
	public Item clone(){
		try{
			return (Item)super.clone();
		}catch(CloneNotSupportedException e){
			throw new RuntimeException(e);
		}
	}

	/**
	 * Use this to remove a particular instance from the active
	 * {@link Squad#equipment} (not an equivalent item, ie
	 * {@link #equals(Object)}).
	 */
	public void expend(){
		for(var c:Squad.active.members){
			var bag=Squad.active.equipment.get(c);
			for(var i:new ArrayList<>(bag))
				if(i==this){
					bag.remove(i);
					return;
				}
		}
	}

	/**
	 * Use this to customize the error message if the item is not expended.
	 */
	public String describefailure(){
		return "Can only be used in battle.";
	}

	/**
	 * @return <code>null</code> if can use this, or an error message otherwise.
	 */
	public String canuse(Combatant c){
		return null;
	}

	/**
	 * Prompts user to select one of the active {@link Squad} members to keep this
	 * item and updates {@link Squad#equipment}.
	 */
	public void grab(Squad s){
		String list=UseItems.listitems(null,false)+"\n";
		list+="Who will take the "+toString().toLowerCase()+"?";
		s.equipment.get(UseItems.selectmember(s.members,this,list)).add(this);
	}

	/**
	 * Same as {@link #grab(Squad)} but uses {@link Squad#active}.
	 */
	public void grab(){
		grab(Squad.active);
	}

	/**
	 * Used as strategic resource damage.
	 *
	 * @param c
	 *
	 * @return Lowercase description of used resources or <code>null</code> if
	 *         wasn't wasted.
	 * @see StartBattle
	 */
	public String waste(float resourcesused,Combatant c,ArrayList<Item> bag){
		if(RPG.random()>=resourcesused||canuse(c)!=null) return null;
		bag.remove(this);
		return name;
	}

	/**
	 * Creates {@link Item}s from {@link Spell}s. Base constructor will add to
	 * {@link #ITEMS}.
	 */
	@SuppressWarnings("unused")
	public static void setup(){
		for(Spell s:Spell.BYNAME.values()){
			if(s.isscroll) new Scroll(s);
			if(s.iswand){
				if(s.level<=Wand.MAXLEVEL) new Wand(s);
				for(var charges=1;charges<=Rod.VARIATIONS;charges++)
					new Rod(s,charges);
			}
			if(s.ispotion) new Potion(s);
			if(s.isring) for(int uses:CasterRing.POWERLEVELS)
				new CasterRing(s,uses);
		}
		Gem.generate();
		ArtPiece.generate();
		Eidolon.generate();
		cheapestartifact=ITEMS.stream().filter(i->i instanceof Artifact)
				.map(i->i.price).min(Integer::compare).get();
	}

	static void addall(ItemSelection fire2,HashMap<String,ItemSelection> all,
			String string){
		all.put(string,fire2);
	}

	/**
	 * @return A list of all {@link Item}s in any {@link Squad}, {@link Town}
	 *         trainees and {@link Academy} trainees (including subclasses).
	 */
	public static List<Item> getplayeritems(){
		ArrayList<Item> items=new ArrayList<>();
		for(Actor a:World.getactors()){
			Academy academy=a instanceof Academy?(Academy)a:null;
			if(academy!=null){
				for(Order o:academy.training.queue){
					TrainingOrder training=(TrainingOrder)o;
					items.addAll(training.equipment);
				}
				continue;
			}
			Squad squad=a instanceof Squad?(Squad)a:null;
			if(squad!=null){
				squad.equipment.clean();
				for(List<Item> bag:squad.equipment.values())
					items.addAll(bag);
				continue;
			}
		}
		return items;
	}

	/**
	 * @param from A sample of items (like {@link #ITEMS} or from
	 *          {@link #BYTIER}).
	 * @return The same items but with randomized parameters, from cheapest to
	 *         most expensive (previously shuffled to introduce order randomness
	 *         for items with exact same price).
	 * @see Item#randomize()
	 */
	public static List<Item> randomize(Collection<Item> from){
		ArrayList<Item> randomized=new ArrayList<>(from.size());
		for(Item i:from)
			randomized.add(i.randomize());
		Collections.shuffle(randomized);
		randomized.sort(ItemsByPrice.SINGLETON);
		return randomized;
	}

	/**
	 * @return <code>true</code> if this item can be currently sold.
	 */
	public boolean sell(){
		return sellvalue>0;
	}

	/** @return A human-readable description of this item. */
	public String describe(Combatant c){
		String description=toString();
		String prohibited=canuse(c);
		if(prohibited!=null)
			description+=" ("+prohibited+")";
		else if(c.equipped.contains(this)) description+=" (equipped)";
		return description;
	}

	/**
	 * @return <code>true</code> if any of the {@link Combatant}s can use this.
	 * @see #canuse(Combatant)
	 */
	public boolean canuse(List<Combatant> members){
		for(var member:members)
			if(canuse(member)==null) return true;
		return false;
	}

	/**
	 * Items with daily uses may refresh charges.
	 *
	 * @param hours Hours ellapsed.
	 */
	public void refresh(int hours){
		//most items do not refresh
	}
}
