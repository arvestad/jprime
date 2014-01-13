package se.cbb.jprime.apps.vmcmc.gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ColorUIResource;

import se.cbb.jprime.apps.vmcmc.libs.MCMCDataContainer;
import se.cbb.jprime.apps.vmcmc.libs.MCMCInterface;

/**
 * MCMCGraphToolPanel: Panel contains graph and tools dependent on the graph.
 */
public class MCMCGraphToolPanel extends JPanel implements MCMCInterface {
	/* **************************************************************************** *
	 * 							CLASS VARIABLES										*
	 * **************************************************************************** */
	private static final long serialVersionUID = 1L;
	private JSlider slider;
	MCMCGraphPanel graphpanel;
	MCMCGraphRuler rulerpanel;
	MCMCDataContainer datacontainer;
	int seriesID;
	double burnin;
	JScrollPane scrollpane;

	private class GraphListener implements MouseListener, MouseMotionListener, KeyListener {
		@SuppressWarnings("unused")
		double x, y; 	//Current position of the mouse cursor
		double leftMarker, rightMarker;		//Selection markers

		Cursor cursor;

		GraphListener() {
			cursor = new Cursor(Cursor.CROSSHAIR_CURSOR);	//Use different cursor for graph
		}

		//Remove selection upon click
		public void mouseClicked(MouseEvent e) {
			graphpanel.select(0, 0);
			graphpanel.repaint();
			graphpanel.requestFocus();
		}
		public void mouseEntered(MouseEvent e) {}
		public void mouseExited(MouseEvent e) {}

		//Left button pressed means beginning of selection
		public void mousePressed(MouseEvent e) {
			leftMarker = e.getX();
			graphpanel.requestFocus();
		}
		public void mouseReleased(MouseEvent e) {}

		//Dragging specifies and updates the width of the selection
		public void mouseDragged(MouseEvent e) {
			rightMarker = e.getX();
			
			if(rightMarker < 0)
				rightMarker = 0;
			else if(rightMarker > graphpanel.getWidth())
				rightMarker = graphpanel.getWidth();
			
			graphpanel.select((int) leftMarker, (int) rightMarker);
			graphpanel.repaint();
		}

		//Cursor and cursor position should be updated when mouse is moved over the graph.
		public void mouseMoved(MouseEvent arg0) {
			setCursor(cursor);

			x = arg0.getX();
			y = arg0.getY();
		}

		public void keyPressed(KeyEvent arg0) {
			KeyEvent keyEvent = arg0;

			switch(keyEvent.getKeyCode()) {
				//Return graph to normal view when user press space and graph has focus.
				case KeyEvent.VK_SPACE:
					graphpanel.setSize(0, 0);
					break;
				//Zoom graph when user press return and graph has focus
				case KeyEvent.VK_ENTER:
					int selectionWidth = graphpanel.getSelection().getWidth();
					int graphWidth = scrollpane.getSize().width;
					final double zoomfactor = (double) graphWidth/selectionWidth;
					
					scrollpane.revalidate();	//Update scrollpane when graph is resized
					
					graphpanel.setWidth((int) ((double) zoomfactor*graphWidth));
					graphpanel.updatePlotPoints();
					
					graphpanel.repaint();
					
					//Scrollbar must be updated later when graph has updated properly
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							JScrollBar scrollbar = scrollpane.getHorizontalScrollBar();
							int leftPos = graphpanel.getSelection().getLeftPos();
							scrollbar.setValue((int) ((double) leftPos/scrollpane.getWidth()*scrollbar.getMaximum()));
							
							graphpanel.select(0, 0);
						}
					});
					
					break;
			}
		}
		public void keyReleased(KeyEvent arg0) {}
		public void keyTyped(KeyEvent arg0) {}
	}

	/* **************************************************************************** *
	 * 							CLASS CONSTRUCTORS									*
	 * **************************************************************************** */
	public MCMCGraphToolPanel() {
		slider = createSlider();
		
		graphpanel = createGraphPanel();
		rulerpanel = createRuler();		//Right side ruler. Displays 9 marker values

		scrollpane = new JScrollPane(graphpanel);
		scrollpane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		scrollpane.getHorizontalScrollBar().addAdjustmentListener(new AdjustmentListener() {
			public void adjustmentValueChanged(AdjustmentEvent arg0) {
				/* Possibility of adding functionality to update burn in marker when graph
				 * is scrolled.
				 */
				
				graphpanel.repaint();
			}
		});

		JPanel centerpanel = new JPanel();		//Centerpanel stores scrollpane

		centerpanel.setLayout(new BoxLayout(centerpanel, BoxLayout.X_AXIS));
		centerpanel.setBackground(new Color(0xFFEEEEFF));

		//Compress scrollpane to conform to slider size.
		centerpanel.add(Box.createRigidArea(new Dimension(7, 0)));
		centerpanel.add(scrollpane);
		centerpanel.add(Box.createRigidArea(new Dimension(7, 0)));

		JPanel top = new JPanel();
		top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
		top.setBackground(new Color(0xFFEEEEFF));
		
		top.add(slider);
		top.add(Box.createRigidArea(new Dimension(50, 0)));
		
		JPanel bottom = new JPanel();
		bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));

		bottom.add(centerpanel);
		bottom.add(rulerpanel);
		
		this.add(top);
		this.add(bottom);
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setBackground(new Color(0xFFEEEEFF));
	}
	
	/* **************************************************************************** *
	 * 							CLASS PUBLIC FUNCTIONS								*
	 * **************************************************************************** */
	public JSlider createSlider() {
		Object[] gradient = {new Float(0.3f), new Float(0.3f), new ColorUIResource(0xFFEEEEEE),	new Color(0xFFFFFFFF), new ColorUIResource(0xFFEEEEEE)};
		Object[] focusGradient = {new Float(0.3f), new Float(0.3f),	new ColorUIResource(0xFFDDDDEE), new Color(0xFFEEEEFF),	new ColorUIResource(0xFFDDDDEE)};
		
		//Specify look of slider
		UIManager.put("Slider.gradient", Arrays.asList(gradient));
		UIManager.put("Slider.focusGradient", Arrays.asList(focusGradient));
		UIManager.put("Slider.altTrackColor", new Color(0xFFFFFFFF));
		UIManager.put("Slider.background", new Color(0xFFFF0000));

		JSlider slider = new JSlider();

		slider.setMajorTickSpacing(1000);	//Allow for a slider resolution of 1000
		slider.setPaintTicks(true);

		slider.setBackground(new Color(0xFFEEEEFF));
		slider.setMaximum(10000);
		slider.setValue(0);

		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				JSlider source = (JSlider) arg0.getSource();
				burnin = (double) source.getValue()/10000;
				graphpanel.setBurnInMarkerPos((int) (burnin*scrollpane.getWidth() + scrollpane.getHorizontalScrollBar().getValue()));
			}
		});

		//Slider mouse listener used to deactivate graph repainting thread when not in use
		slider.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent arg0) {}
			public void mouseEntered(MouseEvent arg0) {}
			public void mouseExited(MouseEvent arg0) {}
			public void mousePressed(MouseEvent arg0) {
				graphpanel.startThread();
			}
			public void mouseReleased(MouseEvent arg0) {
				graphpanel.stopThread();
			}
		});

		return slider;
	}

	public MCMCGraphPanel createGraphPanel() {
		final MCMCGraphPanel graphpanel = new MCMCGraphPanel();

		graphpanel.addComponentListener(new ComponentListener() {
			public void componentHidden(ComponentEvent arg0) {}
			public void componentMoved(ComponentEvent arg0) {}
			public void componentShown(ComponentEvent arg0) {}

			public void componentResized(ComponentEvent arg0) {
				int width = arg0.getComponent().getSize().width;
				int height = arg0.getComponent().getSize().height;
				
				graphpanel.setPanelSize(width, height);
				graphpanel.updatePlotPoints();
				updateGraph();

				graphpanel.repaint();
			}
		});

		GraphListener listener = new GraphListener();

		graphpanel.addMouseListener(listener);
		graphpanel.addMouseMotionListener(listener);
		graphpanel.addKeyListener(listener);

		return graphpanel;
	}
	
	public MCMCGraphRuler createRuler() {
		MCMCGraphRuler rulerpanel = new MCMCGraphRuler();
		
		return rulerpanel;
	}
	
	/** updateGraph: Set new data for graph.*/
	public void updateGraph() {
		if(datacontainer != null) {
			Object[] data = datacontainer.getValueSerie(seriesID).toArray();
			int numPoints = data.length;

			Double[] y = new Double[numPoints];

			for(int i=0; i<numPoints; i++) {
				y[i]= (Double) data[i];
			}

			slider.setValue((int) (burnin*10000));
			graphpanel.setBurnInMarkerPos((int) (burnin*scrollpane.getWidth() + scrollpane.getHorizontalScrollBar().getValue()));
			
			graphpanel.setData(y);
			graphpanel.updatePlotPoints();
			graphpanel.repaint();
		}
	}
	
	/** updateRuler: Set new data for ruler.*/
	public void updateRuler() {
		Object[] serie = datacontainer.getValueSerie(seriesID).toArray();
		
		Double[] data = new Double[serie.length];
		System.arraycopy(serie, 0, data, 0, serie.length);
		
		double maxY = data[0];
		for(int i=0; i<data.length; i++) {
			double value = data[i];

			if(value > maxY)
				maxY = value;
		}

		double minY = data[0];
		for(int i=0; i<data.length; i++) {
			double value = data[i];

			if(value < minY)
				minY = value;
		}

		rulerpanel.setMax(maxY);
		rulerpanel.setMin(minY);
		rulerpanel.repaint();
	}
	
	/** addMarksToGraph: Marks intervals in graph. */
	public void addMarksToGraph(ArrayList<Integer> list) {
		ArrayList<Double> templist = new ArrayList<Double>();

		for(int i=0; i<list.size(); i++) 
			templist.add((double) list.get(i)/datacontainer.getNumValues(seriesID));

		graphpanel.getMarksList().add(templist);
	}
	
	/** clearGraphMarks: Removes marks made in graph.*/
	public void clearGraphMarks() {
		graphpanel.getMarksList().clear();
		graphpanel.repaint();
	}

	public void setDataContainer(MCMCDataContainer datacontainer) 	{this.datacontainer = datacontainer;}
	public void setSeriesID(int id) 								{this.seriesID 		= id;}
	public void setBurnIn(double burnin) 							{this.burnin 		= burnin;}
	
	public JSlider getSlider() 										{return slider;}
	public MCMCGraphPanel getGraph() 								{return graphpanel;}
	public JScrollPane getScrollPane() 								{return scrollpane;}
	public int getSeriesID() 										{return seriesID;}
	public MCMCDataContainer getDataContainer() 					{return datacontainer;}
	/* **************************************************************************** *
	 * 							END OF CLASS										*
	 * **************************************************************************** */
}
