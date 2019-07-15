package javelin.controller.action.ai.attack;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.Javelin.Delay;
import javelin.controller.action.Action;
import javelin.controller.action.ActionCost;
import javelin.controller.ai.ChanceNode;
import javelin.controller.audio.Audio;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.abilities.discipline.Strike;
import javelin.model.unit.attack.Attack;
import javelin.model.unit.attack.AttackSequence;
import javelin.view.mappanel.battle.overlay.AiOverlay;
import javelin.view.screen.StatisticsScreen;

/**
 * Despite not being very large, {@link AbstractAttack} is extremely complex.
 * This new utility class is being created in the hopes of simplifying it after
 * a number of incarnations and revisions.
 *
 * @author alex
 */
public class AttackResolver{
	class DamageNode extends ChanceNode{
		DamageNode(Combatant attacker,Combatant target,BattleState state,
				float chance,String action,Delay delay,String audio){
			super(state,chance,action,delay);
			overlay=new AiOverlay(target.location[0],target.location[1]);
			this.audio=target.hp<=0?new Audio("die",target):new Audio(audio,attacker);
		}
	}

	enum Outcome{
		MISS,
		/**
		 * Inspired by (but deals minimum damage instead of half)
		 * https://dnd-wiki.org/wiki/Graze_Damage_(3.5e_Variant_Rule)#dynamic_user_navbox
		 *
		 * Found the link when looking for a less miss-prone variant combat rules.
		 */
		GRAZE,HIT,CRITICAL_UNCONFIRMED,CRITICAL
	}

	class SequenceResult{
		List<Outcome> outcomes;
		List<String> chances;

		public SequenceResult(){
			var size=sequence.size();
			outcomes=new ArrayList<>(size);
			chances=new ArrayList<>(size);
		}

		@Override
		public boolean equals(Object o){
			var r=o instanceof SequenceResult?(SequenceResult)o:null;
			return r!=null&&outcomes.equals(r.outcomes);
		}

		@Override
		public int hashCode(){
			var hash=0;
			for(int i=0;i<outcomes.size();i++)
				hash+=(outcomes.get(i).ordinal()+1)*Math.pow(Outcome.values().length,i);
			return Math.round(Math.round(hash));
		}

		@Override
		public String toString(){
			return outcomes.toString();
		}
	}

	/**
	 * Bonus to applied to all {@link Attack}s. Not a preview, previews are
	 * calculated by also adding the first {@link Attack#bonus} of the
	 * {@link #sequence}.
	 */
	public int attackbonus=0;
	/** @see Attack#damage */
	public int damagebonus=0;
	/** @see #preview(Combatant, AttackSequence) */
	public Float misschance=null;
	/** @see #preview(Combatant, AttackSequence) */
	public Float hitchance=null;
	/** Human-text preview, see {@link #attackbonus}. */
	public String chance=null;
	/** Can be overriden to force a particular {@link ActionCost}. */
	public Float ap=null;

	AttackSequence sequence;
	AbstractAttack action;
	Strike maneuver;

	/** {@link AttackSequence} Constructor. */
	public AttackResolver(AbstractAttack action,Combatant attacker,
			Combatant target,AttackSequence sequence,BattleState state){
		this.action=action;
		this.sequence=sequence;
		sequence.sort();
		maneuver=action.maneuver;
		attackbonus-=action.getpenalty(attacker,target,state);
		//		attackbonus-=20*target.source.misschance; //TODO is this naive?
		damagebonus+=action.getdamagebonus(attacker,target);
	}

	/** Calculates some fields to expose attack information and statistics. */
	public void preview(Combatant target){
		var preview=sequence.get(0);
		misschance=(target.getac()-attackbonus-preview.getbonus(target))/20f;
		misschance=Action.bind(Action.or(misschance,target.source.misschance));
		hitchance=1-misschance;
		chance=Javelin.getchance(Math.round(20*misschance))+" to hit";
	}

	/** Single-{@link Attack} constructor. */
	public AttackResolver(AbstractAttack action,Combatant attacker,
			Combatant target,Attack attack,BattleState state){
		this(action,attacker,target,new AttackSequence(List.of(attack)),state);
	}

	static void validate(final Collection<Float> chances){
		var sum=chances.stream().collect(Collectors.summingDouble(c->c));
		if(!(0.999<sum&&sum<=1.001))
			throw new RuntimeException("Attack sum not whole: "+sum);
	}

	/*DamageNode miss(Combatant c,Combatant target,BattleState s,DamageChance dc){
		if(action.feign&&target.source.dexterity>=12) Bluff.feign(c,target);
		String name;
		Delay wait;
		if(maneuver==null){
			name=target.toString();
			wait=Delay.WAIT;
		}else{
			name=maneuver.name.toLowerCase();
			wait=Delay.BLOCK;
		}
		var output=c+" misses "+name+" ("+chance+")...";
		return new DamageNode(c,target,s,dc,output,wait,action.soundmiss);
	}

	List<DamageChance> hit(Attack a,float hitchance,int multiplier,Boolean savep){
		if(hitchance==0) return List.of();
		if(FLATDAMAGE) return List.of(
				new DamageChance(hitchance,a.getaveragedamage(),multiplier!=1,savep));
		var effetc=a.geteffect();
		var damagerolls=Action.distributeroll(a.damage[0],a.damage[1]).entrySet();
		return damagerolls.stream().map(roll->{
			var damage=Math.max(1,(roll.getKey()+a.damage[2])*multiplier);
			var chance=hitchance*roll.getValue();
			var save=effetc==null?null:savep;
			return new DamageChance(chance,damage,multiplier!=1,save);
		}).collect(Collectors.toList());
	}

	String posthit(Attack a,Combatant c,Combatant target,BattleState s,
			DamageChance dc){
		if(target.hp>0){
			if(dc.save!=null) return a.geteffect().cast(c,target,dc.save,s,null);
		}else if(action.cleave) c.cleave(ap);
		return null;
	}

	DamageNode createnode(Combatant c,Combatant target,BattleState s,
			DamageChance dc){
		s=s.clone();
		c=s.clone(c).clonesource();
		target=s.clone(target).clonesource();
		if(dc.damage>0) dc.damage+=damagebonus;
		if(dc.damage<0) dc.damage=0;
		if(dc.damage==0) return miss(c,target,s,dc);
		if(maneuver!=null) maneuver.hit(c,target,attack,dc,s);
		var name=maneuver==null?attack.name:maneuver.name.toLowerCase();
		var lines=new ArrayList<String>(5);
		var tohit=" ("+chance+")...";
		lines.add(c+" "+dc.message+" "+target+" with "+name+tohit);
		if(dc.critical) lines.add("Critical hit!");
		if(dc.damage==0)
			lines.add("Damage absorbed!");
		else{
			var resistance=attack.energy?target.source.energyresistance
					:target.source.dr;
			target.damage(dc.damage,s,resistance);
			lines.add(target+" is "+target.getstatus()+".");
			var posthit=posthit(c,target,s,dc);
			if(posthit!=null) lines.add(posthit);
		}
		var wait=target.source.passive
				&&target.getnumericstatus()>Combatant.STATUSUNCONSCIOUS;
		var delay=wait?Delay.WAIT:Delay.BLOCK;
		var output=String.join("\n",lines);
		return new DamageNode(c,target,s,dc,output,delay,action.soundhit);
	}

	List<DamageChance> dealattack(Combatant c,Combatant target){
		var chances=new ArrayList<DamageChance>();
		chances.add(new DamageChance(misschance,0,false,null));
		var graze=(target.getac()-target.gettouchac())/20f;
		if(graze>0){
			var dc=new DamageChance(graze,attack.getminimumdamage(),false,null);
			dc.message="grazes";
			chances.add(dc);
		}
		var effect=target.source.passive?null:attack.geteffect();
		var save=effect==null?1:effect.getsavechance(c,target);
		var nosave=1-save;
		var hit=1-misschance-graze;
		var threat=(21-attack.threat)/20f;
		var confirm=target.source.immunitytocritical?0:threat*hit;
		chances.addAll(hit((hit-confirm)*save,1,true));
		chances.addAll(hit((hit-confirm)*nosave,1,false));
		chances.addAll(hit(confirm*save,attack.multiplier,true));
		chances.addAll(hit(confirm*nosave,attack.multiplier,false));
		if(Javelin.DEBUG) AttackResolver.validate(chances);
		return chances;
	}*/

	/** @return Attack roll penalty equivalent to {@link Monster#misschance}. */
	int getmisspenalty(int bonus,Combatant target){
		var misschance=Action.bind((target.getac()-bonus)/20f);
		var totalmisschance=Action.or(misschance,target.source.misschance);
		return Math.round(Action.bind(totalmisschance-misschance)*20);
	}

	SequenceResult dealattacks(int roll,Combatant target){
		var r=new SequenceResult();
		for(var a:sequence){
			var bonus=a.getbonus(target)+attackbonus;
			if(target.source.misschance>0) bonus-=getmisspenalty(bonus,target);
			var ac=target.getac();
			r.chances.add(Javelin.getchance(ac-bonus));
			final Outcome o;
			if(roll==1)
				o=Outcome.MISS;
			else if(roll>=a.threat)
				o=Outcome.CRITICAL_UNCONFIRMED;
			else if(roll+bonus>=ac)
				o=Outcome.HIT;
			else if(roll+bonus>=ac-target.source.armor)
				o=Outcome.GRAZE;
			else
				o=Outcome.MISS;
			r.outcomes.add(o);
			if(o==Outcome.MISS) break;
		}
		return r;
	}

	void confirm(HashMap<SequenceResult,Float> results){
		//TODO just confirming all for now
		for(var r:results.keySet())
			for(int i=0;i<r.outcomes.size();i++)
				if(r.outcomes.get(i)==Outcome.CRITICAL_UNCONFIRMED)
					r.outcomes.set(i,Outcome.CRITICAL);
	}

	String damage(Combatant target,Outcome o,Attack a,BattleState s){
		var damage=a.getaveragedamage()+damagebonus;
		final String description;
		if(o==Outcome.GRAZE){
			description="graze";
			damage=a.getminimumdamage()+damagebonus;
		}else if(o==Outcome.HIT)
			description="hit";
		else if(o==Outcome.CRITICAL){
			//TODO critical sound would be nice
			description="CRITICAL";
			damage*=a.multiplier;
		}else
			throw new InvalidParameterException(o.toString());
		if(damage<1) damage=1;
		target.damage(damage,s,0);//TODO reduction
		return description;
	}

	ChanceNode apply(SequenceResult result,Float chance,BattleState s,Combatant c,
			Combatant target){
		s=s.clone();
		c=s.clone(c).clonesource();
		target=s.clone(target).clonesource();
		var descriptions=new ArrayList<String>(sequence.size());
		var hit=false;
		var ap=0f;
		for(int i=0;i<sequence.size();i++){
			var o=result.outcomes.get(i);
			var a=sequence.get(i);
			ap+=sequence.indexOf(a)==0?ActionCost.STANDARD
					:ActionCost.SWIFT/(sequence.size()-1);
			var chancetohit=" ("+result.chances.get(i)+" to hit)";
			var name=i==0?StatisticsScreen.capitalize(a.name):a.name;
			if(o==Outcome.MISS){
				descriptions.add(name+": miss"+chancetohit);
				break;
			}
			hit=true;
			var apply=maneuver!=null&&o!=Outcome.GRAZE;
			if(apply) maneuver.prehit(c,target,a,s);
			descriptions.add(name+": "+damage(target,o,a,s)+chancetohit);
			if(apply) maneuver.posthit(c,target,a,s);
			if(target.hp<=0) break;
		}
		c.ap+=this.ap==null?ap:this.ap;
		if(maneuver!=null) maneuver.postattacks(c,target,sequence,s);
		var delay=hit?Delay.BLOCK:Delay.WAIT;
		var sound=hit?action.soundhit:action.soundmiss;
		var message=c+" attacks "+target+"! "+String.join(", ",descriptions)+"...";
		if(hit) message+="\n"+target+" is "+target.getstatus()+".";
		return new DamageNode(c,target,s,chance,message,delay,sound);
	}

	List<ChanceNode> merge(List<ChanceNode> nodes){
		// TODO merge same damage (for example, if killed target in first attack)
		//TODO can probably do by message?
		if(Javelin.DEBUG)
			validate(nodes.stream().map(n->n.chance).collect(Collectors.toList()));
		return nodes;
	}

	public List<ChanceNode> attack(Combatant attackerp,Combatant targetp,
			BattleState statep){
		var s=statep.clone();
		var c=s.clone(attackerp).clonesource();
		var target=s.clone(targetp).clonesource();
		if(maneuver!=null) maneuver.preattacks(c,target,sequence,s);
		var results=new HashMap<SequenceResult,Float>(20);
		for(var roll=1;roll<=20;roll++){
			var result=dealattacks(roll,target);
			var previous=results.get(result);
			var chance=1/20f;
			if(previous!=null) chance+=previous;
			results.put(result,chance);
		}
		confirm(results);
		if(Javelin.DEBUG) validate(results.values());
		var nodes=results.entrySet().stream()
				.map(entry->apply(entry.getKey(),entry.getValue(),s,c,target))
				.collect(Collectors.toList());
		if(Javelin.DEBUG)
			validate(nodes.stream().map(n->n.chance).collect(Collectors.toList()));
		return merge(nodes);
	}
}
