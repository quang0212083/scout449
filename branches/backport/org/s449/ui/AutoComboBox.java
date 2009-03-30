package org.s449.ui;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxEditor;

public class AutoComboBox extends JComboBox {
	private static final long serialVersionUID = 0L;

	private AutoTextFieldEditor autoTextFieldEditor;
	private boolean isFired;

	public AutoComboBox(java.util.List list) {
		isFired = false;
		autoTextFieldEditor = new AutoTextFieldEditor(list);
		setEditable(true);
		setModel(new DefaultComboBoxModel(list.toArray()) {
			private static final long serialVersionUID = 0L;

			protected void fireContentsChanged(Object obj, int i, int j) {
				if (!isFired)
					super.fireContentsChanged(obj, i, j);
			}
		});
		setEditor(autoTextFieldEditor);
	}
	public boolean isCaseSensitive() {
		return autoTextFieldEditor.getAutoTextFieldEditor().isCaseSensitive();
	}
	public void setCaseSensitive(boolean flag) {
		autoTextFieldEditor.getAutoTextFieldEditor().setCaseSensitive(flag);
	}
	public boolean isStrict() {
		return autoTextFieldEditor.getAutoTextFieldEditor().isStrict();
	}
	public void setStrict(boolean flag) {
		autoTextFieldEditor.getAutoTextFieldEditor().setStrict(flag);
	}
	public java.util.List getDataList() {
		return autoTextFieldEditor.getAutoTextFieldEditor().getDataList();
	}
	public void setDataList(java.util.List list) {
		autoTextFieldEditor.getAutoTextFieldEditor().setDataList(list);
		setModel(new DefaultComboBoxModel(list.toArray()));
	}
	public void setSelectedValue(Object obj) {
		if (isFired)
			return;
		else {
			isFired = true;
			setSelectedItem(obj);
			fireItemStateChanged(new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED,
				selectedItemReminder, 1));
			isFired = false;
			return;
		}
	}
	protected void fireActionEvent() {
		if (!isFired)
			super.fireActionEvent();
	}

	private class AutoTextFieldEditor extends BasicComboBoxEditor {
		private static final long serialVersionUID = 0L;
		private AutoTextField getAutoTextFieldEditor() {
			return (AutoTextField) editor;
		}
		protected AutoTextFieldEditor(java.util.List list) {
			editor = new AutoTextField(list, AutoComboBox.this);
		}
	}
}