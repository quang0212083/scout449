package org.s449.ui;

import java.awt.*;

public class VerticalFlow implements LayoutManager {
	private int hgap;
	private int vgap;
	private boolean forceExpand;

	public VerticalFlow(boolean forceExpand) {
		this(forceExpand, 0, 0);
	}
	public VerticalFlow(boolean forceExpand, int hgap, int vgap) {
		this.forceExpand = forceExpand;
		this.hgap = hgap;
		this.vgap = vgap;
	}
	public void addLayoutComponent(String name, Component comp) {
	}
	public void removeLayoutComponent(Component comp) {
	}
	public Dimension preferredLayoutSize(Container target) {
		synchronized (target.getTreeLock()) {
			Dimension dim = new Dimension(0, 0);
			int nmembers = target.getComponentCount();
			boolean firstVisibleComponent = true;

			for (int i = 0 ; i < nmembers ; i++) {
				Component m = target.getComponent(i);
				if (m.isVisible()) {
					Dimension d = m.getPreferredSize();
					dim.width = Math.max(dim.width, d.width);
					if (firstVisibleComponent)
						firstVisibleComponent = false;
					else
						dim.height += vgap;
					dim.height += d.height;
				}
			}
			Insets insets = target.getInsets();
			dim.width += insets.left + insets.right + hgap*2;
			dim.height += insets.top + insets.bottom + vgap*2;
			return dim;
		}
	}
	public Dimension minimumLayoutSize(Container target) {
		return preferredLayoutSize(target);
	}

	/**
	 * Lays out the container. This method lets each 
	 * <i>visible</i> component take
	 * its preferred size by reshaping the components in the
	 * target container in order to satisfy the alignment of
	 * this <code>VerticalFlow</code> object.
	 *
	 * @param target the specified component being laid out
	 * @see Container
	 * @see       java.awt.Container#doLayout
	 */
	public void layoutContainer(Container target) {
		synchronized (target.getTreeLock()) {
			Insets insets = target.getInsets();
			int maxWidth = target.getWidth() - 2 * hgap - insets.left - insets.right;
			int nmembers = target.getComponentCount();
			int x, y = insets.top + vgap;

			for (int i = 0 ; i < nmembers ; i++) {
				Component m = target.getComponent(i);
				if (m.isVisible()) {
					Dimension d = m.getPreferredSize();
					if (forceExpand) {
						x = 0;
						m.setSize(maxWidth, d.height);
					} else {
						x = (int)((maxWidth - d.width) * m.getAlignmentX());
						m.setSize(Math.min(d.width, maxWidth), d.height);
					}
					m.setLocation(insets.left + hgap + x, y);
					y += d.height + vgap;
				}
			}
		}
	}

	/**
	 * Returns a string representation of this <code>VerticalFlow</code>
	 * object and its values.
	 * @return     a string representation of this layout
	 */
	public String toString() {
		return getClass().getName() + "[hgap=" + hgap + ",vgap=" + vgap + "]";
	}
}