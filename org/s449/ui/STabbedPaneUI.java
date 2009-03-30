package org.s449.ui;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

import org.s449.Constants;

public class STabbedPaneUI extends BasicTabbedPaneUI {
	private Font font;
	private Font boldFont;
	private FontMetrics boldFontMetrics;
	private GradientPaint paint;

	protected void installDefaults() {
		super.installDefaults();
		tabAreaInsets = new Insets(5, 8, 2, 8);
		tabInsets = new Insets(1, 4, 1, 4);
		selectedTabPadInsets = new Insets(2, 2, 2, 1);
		contentBorderInsets = new Insets(4, 4, 4, 4);
		tabPane.setBackground(Constants.WHITE);
		paint = null;
		try {
			font = new Font("Verdana", Font.PLAIN, 11);
		} catch (Exception e) {
			font = new Font("SansSerif", Font.PLAIN, 10);
		}
		tabPane.setFont(font);
		boldFont = font.deriveFont(Font.BOLD);
		boldFontMetrics = tabPane.getFontMetrics(boldFont);
	}
	protected void paintContentBorder(Graphics g, int tabPlacement,
			int selectedIndex) {
		int width = tabPane.getWidth();
		int height = tabPane.getHeight();
		int rh = rects[selectedIndex].height;
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setColor(Constants.WHITE);
		Area a = new Area(new RoundRectangle2D.Float(0, rh + 4, width, height - rh - 4, 10, 10));
		a.subtract(new Area(new Rectangle2D.Float(4, rh + 8, width - 8, height - 12 - rh)));
		g2d.fill(a);
	}
	protected void paintText(Graphics g, int tabPlacement, Font font,
			FontMetrics metrics, int tabIndex, String title, Rectangle textRect,
			boolean isSelected) {
		if (isSelected) {
			int vDifference = (int)(boldFontMetrics.getStringBounds(title,g).
				getWidth()) - textRect.width;
			textRect.x -= (vDifference / 2);
			super.paintText(g, tabPlacement, boldFont, boldFontMetrics, tabIndex,
				title, textRect, isSelected);
		} else
			super.paintText(g, tabPlacement, font, metrics, tabIndex, title,
				textRect, isSelected);
	}
	protected void paintTabBorder(Graphics g, int tabPlacement,
			int tabIndex, int x, int y, int w, int h, boolean isSelected) {
		int b = y + h;
		if (!isSelected) b++;
		GeneralPath path = new GeneralPath(GeneralPath.WIND_NON_ZERO);
		path.moveTo(x - 3, b);
		path.lineTo(x + 1, y + 2);
		path.moveTo(x + w - 2, y + 2);
		path.lineTo(x + w + 2, b);
		path.moveTo(x - 2, b);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setColor(Constants.PURPLE);
		g2d.draw(path);
		if (!isSelected)
			g2d.drawLine(x - 2, y + h + 2, x + w + 2, y + h + 2);
	}
	protected void paintFocusIndicator(Graphics g, int tabPlacement,
			Rectangle[] rects, int tabIndex, Rectangle iconRect,
			Rectangle textRect, boolean isSelected) { }
	protected void paintTabBackground(Graphics g, int tabPlacement,
			int tabIndex, int x, int y, int w, int h, boolean isSelected) {
		if (paint == null)
			paint = new GradientPaint(0, y + h + 2, Constants.WHITE, 0, y,
				new Color(192, 192, 192));
		GeneralPath path = new GeneralPath(GeneralPath.WIND_NON_ZERO);
		path.moveTo(x - 2, y + h + 2);
		path.lineTo(x + 2, y + 2);
		path.quadTo(x + 3, y + 1, x + 4, y);
		path.lineTo(x + w - 4, y);
		path.quadTo(x + w - 3, y + 1, x + w - 2, y + 2);
		path.lineTo(x + w + 2, y + h + 2);
		path.lineTo(x - 2, y + h + 2);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setPaint(paint);
		g2d.fill(path);
		paintTabArea(g, tabPlacement, 0);
	}
	protected int getTabLabelShiftY(int tabPlacement, int tabIndex,
			boolean isSelected) {
		return 0;
	}
	protected void paintContentBorderTopEdge(Graphics g, int tabPlacement,
			int selectedIndex, int x, int y, int w, int h) { }
	protected void paintContentBorderRightEdge(Graphics g, int tabPlacement,
			int selectedIndex, int x, int y, int w, int h) { }
	protected void paintContentBorderLeftEdge(Graphics g, int tabPlacement,
			int selectedIndex, int x, int y, int w, int h) { }
	protected void paintContentBorderBottomEdge(Graphics g, int tabPlacement,
			int selectedIndex, int x, int y, int w, int h) { }
	protected void paintTabArea(Graphics g, int tabPlacement,
			int selectedIndex) {
		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setColor(Constants.ALPHA_ONLY);
		Composite composite = g2d.getComposite();
		g2d.setPaintMode();
		g2d.fillRect(0, 0, tabPane.getWidth(), rects[selectedIndex].height + 4);
		g2d.setComposite(composite);
	}
	protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
		return super.calculateTabWidth(tabPlacement, tabIndex, boldFontMetrics) + 4;
	}
}