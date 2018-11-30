package javelin.controller.event.wild;

import java.util.List;

import javelin.Javelin;
import javelin.controller.db.StateManager;
import javelin.controller.event.EventCard;
import javelin.controller.event.EventDealer;
import javelin.controller.event.wild.negative.ConfusingFairies;
import javelin.controller.event.wild.negative.FindIncursion;
import javelin.controller.event.wild.neutral.FindMercenary;
import javelin.controller.event.wild.neutral.WeatherChange;
import javelin.controller.event.wild.positive.FindCaravan;
import javelin.controller.event.wild.positive.FindRuby;
import javelin.controller.event.wild.positive.RevealLocation;
import javelin.controller.event.wild.positive.RevealRegion;
import javelin.controller.event.wild.positive.WanderingHalflings;
import javelin.controller.event.wild.positive.WanderingPegasusHerd;
import javelin.controller.event.wild.positive.WanderingPriest;
import javelin.model.unit.Squad;
import javelin.model.world.location.PointOfInterest;

/**
 * Manages {@link WildEvent}s.
 *
 * @author alex
 */
public class WildEvents extends EventDealer{
	/** @see StateManager */
	public static EventDealer instance=new WildEvents();
	static final Class<? extends WildEvent> DEBUG=WanderingHalflings.class;

	/** Constructor. */
	public WildEvents(){
		positive.addcontent(List.of(WanderingPriest.class,RevealLocation.class,
				RevealRegion.class,FindRuby.class,WanderingPegasusHerd.class));
		neutral.addcontent(List.of(FindMercenary.class,WeatherChange.class,
				FindCaravan.class,WanderingHalflings.class));
		negative.addcontent(List.of(FindIncursion.class,ConfusingFairies.class));
	}

	@Override
	public EventCard generate(Squad s,int squadel,PointOfInterest l){
		if(Javelin.DEBUG&&WildEvents.DEBUG!=null) try{
			var card=DEBUG.getDeclaredConstructor().newInstance();
			if(!card.validate(s,squadel,l))
				throw new UnsupportedOperationException("Invalid #wildevent card ");
			card.define(s,squadel,l);
			return card;
		}catch(ReflectiveOperationException e){
			throw new RuntimeException(e);
		}
		return super.generate(s,squadel,l);
	}
}
