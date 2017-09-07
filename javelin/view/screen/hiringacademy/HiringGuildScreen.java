package javelin.view.screen.hiringacademy;

import javelin.model.unit.Squad;
import javelin.model.unit.attack.Combatant;
import javelin.model.world.location.fortification.Guild;
import javelin.model.world.location.unique.MercenariesGuild;
import javelin.view.screen.town.PurchaseScreen;

public class HiringGuildScreen extends RecruitingGuildScreen {

	public HiringGuildScreen(Guild academy) {
		super(academy);
	}

	@Override
	Hire createoption(Combatant c) {
		Hire h = super.createoption(c);
		h.price = MercenariesGuild.getfee(c);
		h.name = "Hire: " + c.toString().toLowerCase() + " ($"
				+ PurchaseScreen.formatcost(h.price) + "/day)";
		return h;
	}

	@Override
	boolean canafford(Hire h) {
		return Squad.active.gold >= h.price;
	}

	@Override
	void spend(Hire h) {
		Squad.active.gold -= h.price;
		h.c.setmercenary(true);
	}

	@Override
	String printresourcesinfo() {
		return "You currently have $"
				+ PurchaseScreen.formatcost(Squad.active.gold);
	}
}
