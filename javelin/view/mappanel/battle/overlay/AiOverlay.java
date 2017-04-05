package javelin.view.mappanel.battle.overlay;

import java.awt.Graphics;
import java.awt.Image;
import java.util.Collection;

import javelin.controller.Point;
import javelin.controller.action.ai.AiAction;
import javelin.view.mappanel.Overlay;
import javelin.view.mappanel.Tile;

/**
 * Used to simply allow {@link AiAction}s to present some visual feedback. Takes
 * one or more {@link Point}s and draws a target on them.
 * 
 * @author alex
 */
public class AiOverlay extends Overlay {
	public Image image = TargetOverlay.TARGET;

	public AiOverlay(int x, int y) {
		affected.add(new Point(x, y));
	}

	public AiOverlay(Collection<Point> area) {
		affected.addAll(area);
	}

	@Override
	public void overlay(Tile t, Graphics g) {
		if (affected.contains(new Point(t.x, t.y))) {
			draw(t, g, image);
		}
	}
}