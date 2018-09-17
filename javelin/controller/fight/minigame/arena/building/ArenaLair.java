package javelin.controller.fight.minigame.arena.building;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.exception.GaveUp;
import javelin.controller.fight.Fight;
import javelin.controller.fight.minigame.arena.ArenaFight;
import javelin.controller.generator.encounter.EncounterGenerator;
import javelin.controller.terrain.Terrain;
import javelin.model.unit.Combatant;
import javelin.model.world.location.unique.MercenariesGuild;
import javelin.old.RPG;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.Option;
import javelin.view.screen.town.PurchaseScreen;

public class ArenaLair extends ArenaBuilding{
	public static final int OPTIONS=9;

	class HireOption extends Option{
		ArrayList<Combatant> group;

		public HireOption(ArrayList<Combatant> group){
			super(Combatant.group(group),calculateprice(group));
			this.group=group;
		}
	}

	class ArenaLairScreen extends PurchaseScreen{
		ArrayList<Combatant> hired=null;

		public ArenaLairScreen(){
			super("Which group of allies do you wish to hire?",null);
			stayopen=false;
		}

		@Override
		protected int getgold(){
			return ArenaFight.get().gold;
		}

		@Override
		protected void spend(Option o){
			ArenaFight.get().gold-=o.price;
			hired=((HireOption)o).group;
			for(Combatant c:hired)
				c.setmercenary(true);
			hires.remove(hired);
		}

		@Override
		public void onexit(){
			super.onexit();
			if(hired!=null){
				Javelin.app.switchScreen(BattleScreen.active);
				Point here=ArenaFight.displace(getlocation());
				ArenaFight.get().enter(hired,Fight.state.blueTeam,here);
			}
		}

		@Override
		public List<Option> getoptions(){
			ArrayList<Option> options=new ArrayList<>(hires.size());
			for(ArrayList<Combatant> group:hires)
				options.add(new HireOption(group));
			return options;
		}
	}

	ArrayList<ArrayList<Combatant>> hires=new ArrayList<>(OPTIONS);

	public ArenaLair(){
		super("Lair","locationmercenariesguild",
				"Click this lair to recruit allies into the arena!");
		stock();
	}

	void stock(){
		int levelmin=level*5+1;
		int levelmax=levelmin+4;
		while(hires.size()<OPTIONS)
			try{
				hires.add(EncounterGenerator.generate(RPG.r(levelmin,levelmax),
						Arrays.asList(Terrain.ALL)));
			}catch(GaveUp e){
				continue;
			}
	}

	@Override
	protected boolean click(Combatant current){
		new ArenaLairScreen().show();
		return true;
	}

	static int calculateprice(ArrayList<Combatant> group){
		int fee=0;
		for(Combatant c:group)
			fee+=MercenariesGuild.getfee(c);
		return fee*10;
	}

	@Override
	public String getactiondescription(Combatant current){
		String cheapest="";
		if(!hires.isEmpty()){
			int price=hires.stream().map(hire->calculateprice(hire))
					.min((a,b)->Integer.compare(a,b)).get();
			cheapest=" The minimum hire goes for $"+price+".";
		}
		return super.getactiondescription(current)+getgoldinfo()+cheapest;
	}

	public static String getgoldinfo(){
		return "\n\nYour gladiators currently have $"
				+Javelin.format(ArenaFight.get().gold)+".";
	}

	@Override
	protected void upgradebuilding(){
		hires.clear();
		stock();
	}
}
