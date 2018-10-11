package javelin.model.unit.feat.save;

import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.feat.Feat;

/**
 * A feat that enhances one of your saving throws.
 *
 * @author alex
 */
abstract public class SaveFeat extends Feat{
	SaveFeat(String namep){
		super(namep);
	}

	@Override
	public String inform(final Combatant m){
		return "Current "+getname()+": "+getbonus(m.source);
	}

	@Override
	public boolean upgrade(final Combatant m){
		if(m.source.hasfeat(this)) return false;
		super.upgrade(m);
		setbonus(m.source,getbonus(m.source)+2);
		return true;
	}

	/**
	 * @param value Sets the {@link Monster}'s save to its new value.
	 */
	abstract protected void setbonus(final Monster m,int value);

	/**
	 * @return Current {@link Monster}'s save bonus.
	 */
	protected abstract Integer getbonus(final Monster m);

	/**
	 * @return Description of this save (will, reflex or fortitude).
	 */
	abstract protected String getname();
}