package javelin.model.world.location.haunt;

import java.util.List;
import java.util.stream.Collectors;

import javelin.controller.map.location.haunt.ShatteredTempleMap;
import javelin.model.unit.Monster;
import javelin.model.unit.Monster.MonsterType;

/**
 * TODO
 *
 * @author alex
 */
public class ShatteredTemple extends Haunt{
	static final List<MonsterType> TYPES=List.of(MonsterType.UNDEAD,
			MonsterType.FEY);
	static final List<Monster> POOL=Monster.MONSTERS.stream()
			.filter(m->TYPES.contains(m.type)).collect(Collectors.toList());

	/** Constructor. */
	public ShatteredTemple(){
		super("Shaterred temple",ShatteredTempleMap.class,POOL);
	}
}
