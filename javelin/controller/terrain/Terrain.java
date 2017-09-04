package javelin.controller.terrain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javelin.Javelin;
import javelin.JavelinApp;
import javelin.controller.Point;
import javelin.controller.Weather;
import javelin.controller.WorldGenerator;
import javelin.controller.action.world.WorldMove;
import javelin.controller.map.Map;
import javelin.controller.map.Maps;
import javelin.controller.terrain.hazard.Hazard;
import javelin.model.unit.Squad;
import javelin.model.world.Actor;
import javelin.model.world.Season;
import javelin.model.world.World;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.productive.Mine;
import tyrant.mikera.engine.RPG;

/**
 * Represent different types of {@link World} terrain.
 * 
 * One of the responsibilities of subclasses is to generate {@link World} areas.
 * The world generation method is pretty arbitrary, though it's based on
 * real-life geological considerations (like which area is usually close or
 * distant to each area) but not too worried with realist. Read the
 * documentation for methods starting with generate- for more information.
 * 
 * Each terrain also offer hazards and events, which occur about 1% of the step,
 * but are only really triggered if conditions are right.
 * 
 * @author alex
 */
public abstract class Terrain implements Serializable {
	/**
	 * Description return by {@link #getweather()} in case of
	 * {@link Season#WINTER} snow.
	 */
	public static final String SNOWING = "snowing";

	/**
	 * 1 in chance in X of a special {@link #gethazards(int, boolean)} ocurring.
	 */
	public static final int HAZARDCHANCE = 100;

	/** 2/16 Easy (el-5 to el-8) - plains */
	public static final Terrain PLAIN = new Plains();
	/** Similar to plains. */
	public static final Terrain HILL = new Hill();
	/** * 10/16 Moderate (el-4) - forest */
	public static final Terrain FOREST = new Forest();
	/** Similar to {@link #FOREST}. */
	public static final Terrain WATER = new Water();

	/** 3/16 Difficult (el-3 to el) - mountains */
	public static final Terrain MOUNTAINS = new Mountains();
	/** Similar to mountain. Doubles as tundra in the winter. */
	public static final Terrain DESERT = new Desert();
	/** 1/16 Very difficult (el+1) - swamp */
	public static final Terrain MARSH = new Marsh();
	/** Represent {@link Dungeon}s and {@link Mine}s. */
	public static final Terrain UNDERGROUND = new Underground();
	/** All terrain types except {@link #UNDERGROUND}. */
	public static final Terrain[] ALL = new Terrain[] { PLAIN, HILL, FOREST,
			WATER, MOUNTAINS, DESERT, MARSH, WATER };

	/**
	 * Encounter level adjustment.
	 * 
	 * @deprecated This is being ignored for positive values. Currently the
	 *             terrains are already more difficult due to travel speed
	 *             variations. Besides that, {@link #difficultycap} should be
	 *             enough to also make certain terrains lesser fatal - and is a
	 *             rarer ocurrance instead of a fixed adjustment, which can be
	 *             veryunforgiving and hence no fun.
	 */
	@Deprecated
	public Integer difficulty = null;

	/** No road. */
	public Float speedtrackless = null;
	/** Minor road. */
	public Float speedroad = null;
	/** Major road. */
	public Float speedhighway = null;
	/** Used to determine tile. */
	public String name = null;

	/**
	 * Maximum encounter level delta allowed, in order to make some terrains
	 * more noob friendly.
	 */
	public Integer difficultycap = null;
	/** Used to see distant {@link World} terrain. */
	public Integer visionbonus = null;

	private ArrayList<Actor> towns;

	/** ASCII representation of terrain type for debugging purposes. */
	public Character representation = null;

	/**
	 * Uses current terrain as base.
	 * 
	 * @param mph
	 *            Applies terrain penalty to base Squad speed.
	 * @param x
	 *            {@link World} coordinate.
	 * @param y
	 *            {@link World} coordinate.
	 * @return Speed in miles per hour to traverse this terrain.
	 * 
	 * @see Squad#move()
	 * @see Terrain#current()
	 */
	public int speed(int mph, int x, int y) {
		return Math.round(mph * getspeed(x, y));
	}

	/**
	 * @param x
	 *            {@link World} coordinate.
	 * @param y
	 *            {@link World} coordinate.
	 * @return A percentage value determining how fast it is to walk here, based
	 *         on road status.
	 * @see World#roads
	 * @see World#highways
	 */
	public float getspeed(int x, int y) {
		if (!World.seed.roads[x][y]) {
			return speedtrackless;
		}
		return World.seed.highways[x][y] ? speedhighway : speedroad;
	}

	/**
	 * TODO this probably should return {@link Underground} as well.
	 * 
	 * @return Current terrain difficulty. For example: {@link PLAIN}.
	 */
	static public Terrain current() {
		if (JavelinApp.context == null) {
			return null;
		}
		Point h = JavelinApp.context.getherolocation();
		return h == null ? null : Terrain.get(h.x, h.y);
	}

	/**
	 * @param x
	 *            {@link World} coordinate.
	 * @param y
	 *            {@link World} coordinate.
	 * @return Terrain difficulty. For example: {@link PLAIN}.
	 */
	public static Terrain get(int x, int y) {
		return World.getseed().map[x][y];
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return obj != null && name.equals(((Terrain) obj).name);
	}

	/**
	 * @return A battle map instance to be generated.
	 * @see Map#generate()
	 */
	abstract public Maps getmaps();

	HashSet<Point> generatearea(World world) {
		Point source = generatesource(world);
		Point current = source;
		HashSet<Point> area = generatestartingarea(world);
		while (area.size() < generateareasize()) {
			area.add(current);
			current = expand(area, world, generatereference(source, current));
		}
		return area;
	}

	/**
	 * @return Number of tiles the generated area for this terrain should have.
	 */
	protected int generateareasize() {
		return World.scenario.size * World.scenario.size
				/ WorldGenerator.NREGIONS;
	}

	/**
	 * Usually returns an empty set.
	 * 
	 * @return a set of points which will be considered as already included in
	 *         the generated area, before starting the
	 *         {@link #generatearea(World)} process proper.
	 */
	protected HashSet<Point> generatestartingarea(World world) {
		return new HashSet<Point>();
	}

	/**
	 * Decides where to start the {@link #expand(HashSet, World, Point)} process
	 * from.
	 * 
	 * @param source
	 *            The very starting point for this area.
	 * @param current
	 *            The last expanded point for this area.
	 * @return the source, by default. Subclasses may change this behavior.
	 */
	protected Point generatereference(Point source, Point current) {
		return source;
	}

	/**
	 * @param area
	 *            Given the current generated area...
	 * @param p
	 *            and a point of reference...
	 * @return A new point to be added to the area.
	 */
	protected Point expand(HashSet<Point> area, World world, Point p) {
		int x = p.x;
		int y = p.y;
		Integer lastx;
		Integer lasty;
		while (area.contains(new Point(x, y))) {
			lastx = x;
			lasty = y;
			x += randomstep();
			y += randomstep();
			if (checkinvalid(world, x, y)) {
				x = lastx;
				y = lasty;
				WorldGenerator.retry();
				continue;
			}
		}
		return new Point(x, y);
	}

	/**
	 * @param x
	 *            Coordinate.
	 * @param y
	 *            Coordinate.
	 * @return <code>false</code> if for any reason the given coordinate
	 *         shouldn't be added to this area.
	 */
	protected boolean checkinvalid(World world, int x, int y) {
		return !World.validatecoordinate(x, y)
				|| !generatetile(world.map[x][y], world) || checktown(x, y);
	}

	boolean checktown(int x, int y) {
		for (Actor town : Town.gettowns()) {
			if (town.x == x && town.y == y) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return The starting point for this area.
	 */
	protected Point generatesource(World world) {
		return new Point(randomaxispoint(), randomaxispoint());
	}

	/**
	 * @param p
	 *            Given a point...
	 * @param neighbor
	 *            will check if there is such a terrain tile...
	 * @param radius
	 *            in the given radius around it.
	 * @param World
	 *            World instance.
	 * @return Number of terrain tiles from the given type found in radius.
	 */
	public static int search(Point p, Terrain neighbor, int radius, World w) {
		int found = 0;
		for (int x = p.x - radius; x <= p.x + radius; x++) {
			for (int y = p.y - radius; y <= p.y + radius; y++) {
				if (x == p.x && y == p.y) {
					continue;
				}
				if (!World.validatecoordinate(x, y)) {
					continue;
				}
				if (w.map[x][y].equals(neighbor)) {
					found += 1;
				}
			}
		}
		return found;
	}

	/**
	 * Called at the end of the {@link #generatearea(World)} process.
	 * 
	 * @param area
	 *            The generated area.
	 */
	public void generatesurroundings(List<Point> area, World w) {
		// nothing by default
	}

	/**
	 * @param terrain
	 *            Given the terrain this area is going to expand to...
	 * @return <code>true</code> if OK to overried previous terrain and expand
	 *         the current area into it.
	 */
	protected boolean generatetile(Terrain terrain, World world) {
		return Terrain.FOREST.equals(terrain);
	}

	/**
	 * @return All points of this {@link World} where this terrain exists.
	 */
	protected HashSet<Point> gettiles(World world) {
		HashSet<Point> area = new HashSet<Point>();
		for (int x = 0; x < World.scenario.size; x++) {
			for (int y = 0; y < World.scenario.size; y++) {
				if (world.map[x][y] == this) {
					area.add(new Point(x, y));
				}
			}
		}
		return area;
	}

	static ArrayList<Point> adjacent = new ArrayList<Point>(4);
	static {
		adjacent.add(new Point(-1, 0));
		adjacent.add(new Point(0, -1));
		adjacent.add(new Point(+1, 0));
		adjacent.add(new Point(0, +1));
	}

	/**
	 * Generate terrain area and...
	 * 
	 * @param r
	 *            a {@link Town} of this realm, if not <code>null</code>.
	 * @return
	 */
	public List<Point> generate(World w) {
		List<Point> area = new ArrayList<Point>(generatearea(w));
		if (flooded()) {
			isolated: for (Point p : new ArrayList<Point>(area)) {
				for (Point a : adjacent) {
					if (area.contains(new Point(p.x + a.x, p.y + a.y))) {
						continue isolated;
					}
				}
				area.remove(p);
			}
		}
		for (Point p : area) {
			w.map[p.x][p.y] = this;
		}
		generatesurroundings(area, w);
		return area;
	}

	static int randomstep() {
		return RPG.pick(new int[] { -1, 0, +1 });
	}

	static int randomaxispoint() {
		return RPG.r(1, World.scenario.size - 2);
	}

	/**
	 * @return <code>true</code> if active {@link Squad} can enter this
	 *         location.
	 */
	public boolean enter(int x, int y) {
		return true;
	}

	/**
	 * Hazards are things like a sandstorm in a {@link Desert} or a storm in
	 * {@link Water}, which can have dire implication for a {@link Squad}
	 * travelling in that terrain, Somewhat of a misnomer, a hazard can also be
	 * a more peaceful event like meeting a special character or such.
	 * 
	 * Almost all types of hazards are dependent upon {@link Season},
	 * {@link Weather} and day period conditions. No one suffers a heatstroke on
	 * the desert during the night, for example.
	 * 
	 * Usually, even if the conditions for multiple types of hazards are met in
	 * a certain time period, only one of them should actually trigger.
	 * 
	 * @param special
	 *            <code>true</code> if may allow a special event to happen.
	 *            Special events are rare occurances like a spontaneous
	 *            avalanche.
	 * @return
	 * @see #HAZARDCHANCE
	 * @see Javelin#getDayPeriod()
	 * @see WorldMove
	 */
	public Set<Hazard> gethazards(boolean special) {
		return new HashSet<Hazard>();
	}

	/**
	 * @return a string representation of the {@link Weather}.
	 * @see Weather#current
	 */
	public String getweather() {
		if (Weather.current == Weather.RAIN) {
			return "raining";
		}
		if (Weather.current == Weather.STORM) {
			return Season.current == Season.WINTER ? SNOWING : "storm";
		}
		return "";
	}

	/**
	 * @param teame
	 *            Added to the encounter level delta.l
	 * @return Encounter level for a fight taking place in this type of terrain.
	 */
	public Integer getel(int teamel) {
		final int delta = Javelin.randomdifficulty() + Math.min(0, difficulty);
		return teamel + Math.min(delta, difficultycap);
	}

	public boolean flooded() {
		return equals(MARSH) || equals(WATER) || equals(DESERT);
	}
}
