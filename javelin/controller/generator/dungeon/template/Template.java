package javelin.controller.generator.dungeon.template;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javelin.controller.Point;
import javelin.controller.generator.dungeon.template.Iterator.TemplateTile;
import tyrant.mikera.engine.RPG;

/**
 * TODO most templates should be read from file, not generated
 *
 * @author alex
 */
public abstract class Template implements Cloneable {
	public static final char FLOOR = '.';
	public static final char WALL = '█';
	public static final char DECORATION = '!';
	public static final char DOOR = '□';

	public char[][] tiles = null;
	public int width = 0;
	public int height = 0;
	// public boolean hasdecoration;

	void init(int width, int height) {
		this.width = width;
		this.height = height;
		tiles = new char[width][height];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				tiles[x][y] = FLOOR;
			}
		}
	}

	public abstract void generate();

	public void modify() {
		if (RPG.chancein(2)) {
			rotate();
		}
		if (RPG.chancein(2)) {
			mirrorhorizontally();
		}
		if (RPG.chancein(2)) {
			mirrorvertically();
		}
	}

	private void mirrorvertically() {
		for (int x = 0; x < width; x++) {
			char[] original = Arrays.copyOf(tiles[x], height);
			for (int y = 0; y < height; y++) {
				tiles[x][height - 1 - y] = original[y];
			}
		}
	}

	void mirrorhorizontally() {
		char[][] original = Arrays.copyOf(tiles, width);
		for (int x = 0; x < width; x++) {
			tiles[width - x - 1] = original[x];
		}
	}

	void rotate() {
		char[][] rotated = new char[height][width];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				rotated[y][x] = tiles[x][y];
			}
		}
		tiles = rotated;
		Point dimensions = new Point(width, height);
		width = dimensions.y;
		height = dimensions.x;
	}

	@Override
	public String toString() {
		String s = "";
		for (int y = height - 1; y >= 0; y--) {
			for (int x = 0; x < width; x++) {
				s += tiles[x][y];
			}
			s += "\n";
		}
		return s;
	}

	public void iterate(Iterator i) {
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				i.iterate(new TemplateTile(x, y, tiles[x][y]));
			}
		}
	}

	protected double getarea() {
		return width * height;
	}

	public ArrayList<Point> find(char tile) {
		ArrayList<Point> found = new ArrayList<Point>();
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (tiles[x][y] == tile) {
					found.add(new Point(x, y));
				}
			}
		}
		return found;
	}

	public Template create() {
		if (width == 0 || !validate()) {
			generate();
		}
		modify();
		close();
		makedoors();
		return clone();
	}

	static HashSet<Point> walkcache = new HashSet<Point>();

	boolean validate() {
		List<Point> doors = getdoors();
		for (int a = 0; a < doors.size(); a++) {
			Point doora = doors.get(a);
			for (int b = a + 1; b < doors.size(); b++) {
				Point doorb = doors.get(b);
				walkcache.clear();
				if (!walk(new Point(doora.x, doora.y),
						new Point(doorb.x, doorb.y))) {
					return false;
				}
			}
		}
		return false;
	}

	boolean walk(Point a, Point b) {
		if (a.equals(b)) {
			return true;
		}
		if (!walkcache.add(b)) {
			return false;
		}
		for (int x = a.x - 1; x <= a.x + 1; x++) {
			for (int y = a.y - 1; y <= a.y + 1; y++) {
				Point step = new Point(x, y);
				if (step.equals(a) || !step.validate(0, 0, width, height)
						|| tiles[x][y] != FLOOR) {
					continue;
				}
				if (walk(step, b)) {
					return true;
				}
			}
		}
		return false;
	}

	void makedoors() {
		int doors = RPG.r(1, 4);
		for (int i = 0; i < doors; i++) {
			Direction direction = Direction.getrandom();
			Point door = findentry(direction);
			if (door != null) {
				tiles[door.x][door.y] = DOOR;
				continue;
			}
			if (count(DOOR) != 0) {
				return;
			}
			i -= 1;
		}
	}

	public int count(char c) {
		return find(c).size();
	}

	Point findentry(Direction d) {
		ArrayList<Point> doors = d.getborder(this);
		Collections.shuffle(doors);
		for (Point door : doors) {
			Point p = new Point(door.x + d.reverse.x, door.y + d.reverse.y);
			if (tiles[p.x][p.y] == FLOOR && !neardoor(p)) {
				return door;
			}
		}
		return null;
	}

	boolean neardoor(Point p) {
		for (int x = p.x - 1; x <= p.x + 1; x++) {
			for (int y = p.y - 1; y <= p.y + 1; y++) {
				Point neighbor = new Point(x, y);
				if (p.validate(0, 0, width, height)
						&& tiles[neighbor.x][neighbor.y] == DOOR) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	protected Template clone() {
		try {
			return (Template) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	public void close() {
		width += 2;
		height += 2;
		char[][] closed = new char[width + 2][height + 2];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (isborder(x, y)) {
					closed[x][y] = WALL;
				} else {
					closed[x][y] = tiles[x - 1][y - 1];
				}
			}
		}
		tiles = closed;
	}

	protected boolean isborder(int x, int y) {
		return x == 0 || y == 0 || x == width - 1 || y == height - 1;
	}

	public List<Point> getdoors() {
		return find(Template.DOOR);
	}

	public Point getdoor(Direction d) {
		for (Point door : getdoors()) {
			if (inborder(door.x, door.y) == d) {
				return door;
			}
		}
		return null;
	}

	public Direction inborder(int x, int y) {
		if (x == 0) {
			return Direction.WEST;
		}
		if (x == width - 1) {
			return Direction.EAST;
		}
		if (y == 0) {
			return Direction.SOUTH;
		}
		if (y == height - 1) {
			return Direction.NORTH;
		}
		return null;
	}

	public Point rotate(Direction to) {
		Point todoor = null;
		while (todoor == null) {
			todoor = getdoor(to);
			if (todoor == null) {
				modify();
			}
		}
		return todoor;
	}
}