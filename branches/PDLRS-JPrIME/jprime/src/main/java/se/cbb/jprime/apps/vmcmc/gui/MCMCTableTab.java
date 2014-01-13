package se.cbb.jprime.apps.vmcmc.gui;

import java.awt.Color;
import java.util.List;

import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * MCMCTableTab: Tab panel for the table tab. Relies on outside definition of it's buttons
 * array.
 */
public class MCMCTableTab extends MCMCStandardTab {
	/* **************************************************************************** *
	 * 							CLASS VARIABLES										*
	 * **************************************************************************** */
	private static final long serialVersionUID = 1L;

	private DefaultTableModel model;
	private JTable table;
	private JRadioButton buttons[];

	private class MCMCTableModel extends DefaultTableModel{
		private static final long serialVersionUID = 1L;

		public boolean isCellEditable(int rowIndex,int columnIndex) {
			return false;
		}
	}

	/* **************************************************************************** *
	 * 							CLASS CONSTRUCTORS									*
	 * **************************************************************************** */
	public MCMCTableTab() {
		super();

		model = new MCMCTableModel();
		table = new JTable(model);

		JScrollPane scrollpane = new JScrollPane(table);
		scrollpane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		buttons = null;

		table.setCellSelectionEnabled(true);
		table.getTableHeader().setBackground(new Color(0xFFDDDDFF));
		table.getTableHeader().setResizingAllowed(false);
		table.getTableHeader().setReorderingAllowed(false);

		centerPanel.add(scrollpane);
	}

	/* **************************************************************************** *
	 * 							CLASS PUBLIC FUNCTIONS								*
	 * **************************************************************************** */
	public void addColumn(String name, List<?> data) 	{model.addColumn(name, data.toArray());}
	public void setSelectedButton(int id) 				{buttons[id].setSelected(true);}
	public void setButtons(JRadioButton[] buttons) 		{this.buttons = buttons;};
	public JRadioButton[] getButtons() 					{return buttons;}
	
	/* **************************************************************************** *
	 * 							END OF CLASS										*
	 * **************************************************************************** */
}

