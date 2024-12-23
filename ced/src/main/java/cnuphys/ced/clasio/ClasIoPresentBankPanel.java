package cnuphys.ced.clasio;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.jlab.io.base.DataEvent;

import cnuphys.bCNU.component.ActionLabel;
import cnuphys.bCNU.view.BaseView;
import cnuphys.bCNU.view.VirtualView;
import cnuphys.ced.alldata.DataWarehouse;
import cnuphys.ced.cedview.CedView;
import cnuphys.ced.cedwindow.CedDataView;
import cnuphys.ced.cedwindow.CedDataWindow;
import cnuphys.ced.clasio.ClasIoEventManager.EventSourceType;
import cnuphys.ced.clasio.table.NodeTable;
import cnuphys.ced.frame.Ced;

/**
 * Panel that shows which banks are present in an event
 *
 * @author heddle
 *
 */
@SuppressWarnings("serial")
public class ClasIoPresentBankPanel extends JPanel {

	// try to set a reasonable height
	private int preferredHeight;

	// the event manager
	private static ClasIoEventManager _eventManager = ClasIoEventManager.getInstance();

	//data warehouse
	private static DataWarehouse _dataWarehouse = DataWarehouse.getInstance();

	// hash table
	private Hashtable<String, ActionLabel> _alabels = new Hashtable<>(193);

	// the node table
	private NodeTable _nodeTable;

	// all the panels
	private static List<ClasIoPresentBankPanel> _pbPanels = new ArrayList<>();

	// the single event listaner
	private static IClasIoEventListener _eventListener;

	// if a cedview owns this
	private CedView _view;

	// scroll pane
	private JScrollPane _scrollPane;

	/**
	 * This panel holds all the known banks in a grid of buttons. Banks present will
	 * be clickable, and will cause the table to scroll to that name * @param
	 * nodeTable
	 *
	 * @param view      the view owner
	 * @param nodeTable the table
	 * @param numRows   the number of rows for banks
	 */
	private ClasIoPresentBankPanel(BaseView view, NodeTable nodeTable, int numRows) {

		_view = (view instanceof CedView) ? (CedView) view : null;

		_nodeTable = nodeTable;
		setLayout(new GridLayout(numRows, 0, 2, 0));
		setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 2));

		FontMetrics gm = getFontMetrics(ActionLabel.enabledFontLarge);
		preferredHeight = numRows * (gm.getHeight() + 2);

	}

	/**
	 * Create a present bank panel
	 *
	 * @param view      the view owner
	 * @param nodeTable the table
	 * @param numRows   the number of rows for banks
	 * @return the present bank panel
	 */
	public static ClasIoPresentBankPanel createPresentBankPanel(BaseView view, NodeTable nodeTable, int numRows) {

		if (_eventListener == null) {
			createEventListener();
		}

		ClasIoPresentBankPanel pbPanel = new ClasIoPresentBankPanel(view, nodeTable, numRows);
		_pbPanels.add(pbPanel);

		return pbPanel;

	}

	// create the event listener
	private static void createEventListener() {
		_eventListener = new IClasIoEventListener() {
			@Override
			public void newClasIoEvent(DataEvent event) {
				if (!_eventManager.isAccumulating()) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							for (ClasIoPresentBankPanel pbPanel : _pbPanels) {
								pbPanel.replaceBankLabels(event);
								pbPanel.update();
							}
						}
					});
				}
			}

			@Override
			// TODO Auto-generated method stub
			public void openedNewEventFile(String path) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						for (ClasIoPresentBankPanel pbPanel : _pbPanels) {
							pbPanel.replaceBankLabels(null);
						}
					}
				});
			}

			@Override
			public void changedEventSource(EventSourceType source) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						for (ClasIoPresentBankPanel pbPanel : _pbPanels) {
							pbPanel.replaceBankLabels(null);
						}
					}
				});
			}
		};
		ClasIoEventManager.getInstance().addClasIoEventListener(_eventListener, 1);
	}


//	@Override
//	public Dimension getPreferredSize() {
//		Dimension d = super.getPreferredSize();
//		d.width = 300;
//		d.height = preferredHeight;
//		return d;
//
//	}

	// replace all the bank action labels as result of new event
	private void replaceBankLabels(DataEvent event) {

		removeAll();
		repaint();

		if (event != null) {
			String[] allBanks = event.getBankList();
			Arrays.sort(allBanks);
			if (allBanks != null) {
				for (String s : allBanks) {
					if (match(s)) {
						makeLabel(s);
					}
				}
			}
		}
		repaint();
	}

	// must match
	private boolean match(String s) {

		if (_view == null) {
			return true;
		}

		String[] matchList = _view.getBanksMatches();

		if (matchList == null) { // accept all
			return true;
		} else {
			for (String ms : matchList) {
				if (s.contains(ms)) {
					return true;
				}
			}
		}

		return false;
	}

	// update as the result of a new event arriving
	private void update() {
		String[] allBanks = DataWarehouse.getInstance().getKnownBanks();

		if (allBanks == null) {
			return;
		}

		for (String s : allBanks) {

			ActionLabel alabel = _alabels.get(s);

			if (alabel != null) {

				boolean inCurrent = _dataWarehouse.isBankInCurrentEvent(s);
				alabel.setEnabled(inCurrent);
			}
		}
	}

	// convenience method to make a button
	private ActionLabel makeLabel(final String label) {
		final ActionLabel alabel = new ActionLabel(label, false);
		alabel.setOpaque(true);

		MouseListener ml = new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (_dataWarehouse.isBankInCurrentEvent(label)) {
					int clickCount = e.getClickCount();

					if ((_nodeTable != null) && (clickCount == 1)) {
						_nodeTable.makeNameVisible(label);

					} else if (clickCount == 2) {

						if (Ced.getCed().isFloatingBankDisplay()) {
							// open a new window
							CedDataWindow dw = CedDataWindow.getBankWindow(label);
							dw.update();
							dw.setVisible(true);
						return;
						}

						//open a view
						CedDataView cdv = CedDataView.getBankView(label);
						cdv.update();
						cdv.setVisible(true);
						VirtualView.getInstance().moveToCurrentColumn(cdv);

					}
				}
			}

			@Override
			public void mousePressed(MouseEvent e) {
			}

			@Override
			public void mouseReleased(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				if (_dataWarehouse.isBankInCurrentEvent(label)) {
					alabel.setBackground(Color.yellow);
				}

			}

			@Override
			public void mouseExited(MouseEvent e) {
				alabel.setBackground(null);
			}

		};

		alabel.addMouseListener(ml);

		// alabel.addActionListener(this);
		_alabels.put(label, alabel);
		add(alabel);
		return alabel;
	}

	/**
	 * Get the table's scroll pane
	 *
	 * @return te table's scroll pane
	 */
	public JScrollPane getScrollPane() {
		if (_scrollPane == null) {
			_scrollPane = new JScrollPane(this);
		}
		return _scrollPane;
	}

}
