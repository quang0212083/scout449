package org.s449.ui;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

import org.s449.Constants;

public class FrontPanelTabbedPaneUI extends BasicTabbedPaneUI {
	private Font font;
	private Font boldFont;
	private FontMetrics boldFontMetrics;

	protected void installDefaults() {
		super.installDefaults();
		tabAreaInsets = new Insets(5, 8, 2, 8);
		tabInsets = new Insets(1, 4, 1, 4);
		selectedTabPadInsets = new Insets(0, 0, 2, 0);
		contentBorderInsets = new Insets(4, 4, 4, 4);
		tabPane.setBackground(Constants.WHITE);
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
		g2d.fill(new RoundRectangle2D.Float(0, 0, width, height - rh - 4, 10, 10));
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
			int tabIndex, int x, int y, int w, int h, boolean isSelected) { }
	protected void paintFocusIndicator(Graphics g, int tabPlacement,
			Rectangle[] rects, int tabIndex, Rectangle iconRect,
			Rectangle textRect, boolean isSelected) { }
	protected void paintTabBackground(Graphics g, int tabPlacement,
			int tabIndex, int x, int y, int w, int h, boolean isSelected) {
		GeneralPath path = new GeneralPath(GeneralPath.WIND_NON_ZERO);
		path.moveTo(x + 1, y - 1);
		path.lineTo(x + 1, y + h - 4);
		path.quadTo(x + 2, y + h - 1, x + 5, y + h);
		path.lineTo(x + w - 5, y + h);
		path.quadTo(x + w - 2, y + h - 1, x + w - 1, y + h - 4);
		path.lineTo(x + w - 1, y - 1);
		path.lineTo(x + 1, y - 1);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		if (isSelected)
			g2d.setColor(Constants.WHITE);
		else
			g2d.setColor(Constants.LIGHT_GRAY);
		g2d.fill(path);
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
		g2d.fillRect(0, tabPane.getHeight() - rects[selectedIndex].height - 4,
			tabPane.getWidth(), tabPane.getHeight());
		g2d.setComposite(composite);
		super.paintTabArea(g, tabPlacement, selectedIndex);
	}
	protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
		return super.calculateTabWidth(tabPlacement, tabIndex, boldFontMetrics) + 4;
	}
}