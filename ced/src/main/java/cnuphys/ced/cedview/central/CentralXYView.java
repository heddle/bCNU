package cnuphys.ced.cedview.central;

import java.awt.BorderLayout;

/**
 * Note this view started out as just the XY view for the BST. But it has evolved into the xy view for
 * all central detectors.
 */

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Vector;

import org.jlab.io.base.DataEvent;

import cnuphys.bCNU.drawable.DrawableAdapter;
import cnuphys.bCNU.drawable.IDrawable;
import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.world.WorldGraphicsUtilities;
import cnuphys.bCNU.layer.LogicalLayer;
import cnuphys.bCNU.util.Environment;
import cnuphys.bCNU.util.Fonts;
import cnuphys.bCNU.util.PropertySupport;
import cnuphys.bCNU.view.BaseView;
import cnuphys.ced.alldata.ColumnData;
import cnuphys.ced.cedview.CedView;
import cnuphys.ced.cedview.CedXYView;
import cnuphys.ced.cedview.ILabCoordinates;
import cnuphys.ced.cedview.urwell.HighlightData;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.component.ControlPanel;
import cnuphys.ced.component.DisplayBits;
import cnuphys.ced.event.data.AdcHit;
import cnuphys.ced.event.data.AdcList;
import cnuphys.ced.event.data.BMT;
import cnuphys.ced.event.data.BST;
import cnuphys.ced.event.data.BaseHit2;
import cnuphys.ced.event.data.CTOF;
import cnuphys.ced.event.data.Cosmic;
import cnuphys.ced.event.data.Cosmics;
import cnuphys.ced.event.data.DataDrawSupport;
import cnuphys.ced.event.data.TdcAdcTOFHit;
import cnuphys.ced.event.data.lists.BaseHit2List;
import cnuphys.ced.event.data.lists.CosmicList;
import cnuphys.ced.event.data.lists.TdcAdcTOFHitList;
import cnuphys.ced.geometry.BSTxyPanel;
import cnuphys.ced.geometry.CNDGeometry;
import cnuphys.ced.geometry.CTOFGeometry;
import cnuphys.ced.geometry.GeometryManager;
import cnuphys.ced.geometry.bmt.BMTSectorItem;
import cnuphys.lund.X11Colors;
import cnuphys.swim.SwimTrajectory2D;

@SuppressWarnings("serial")
public class CentralXYView extends CedXYView implements ILabCoordinates {

	// for naming clones
	private static int CLONE_COUNT = 0;

	// base title
	private static final String _baseTitle = "Central XY";

	private BSTxyPanel _closestPanel;

	private static Color _panelColors[] = { X11Colors.getX11Color("sky blue"), X11Colors.getX11Color("light blue") };

	private static Color _ctofColors[] = { new Color(240, 240, 240), new Color(224, 224, 224) };

	// the CND xy polygons
	private CNDXYPolygon _cndPoly[][] = new CNDXYPolygon[3][48];

	// the CTOF polygons
	private CTOFXYPolygon _ctofPoly[] = new CTOFXYPolygon[48];

	// BMT [sector][layer]
	private BMTSectorItem _bmtItems[][];
	
	//for highlighting
	private HighlightData _bstHighlightData = new HighlightData();
	private HighlightData _bmtHighlightData = new HighlightData();


	// units are mm
	// private static Rectangle2D.Double _defaultWorldRectangle = new
	// Rectangle2D.Double(
	// 200., -200., -400., 400.);

	private static Rectangle2D.Double _defaultWorldRectangle = new Rectangle2D.Double(400, -400, -800, 800);

	// used to draw swum trajectories (if any) in the after drawer
	private SwimTrajectoryDrawer _swimTrajectoryDrawer;

	// draws reconstructed crosses
	private CrossDrawerXY _crossDrawer;
	
	// draws reconstructed clusters
	private ClusterDrawerXY _clusterDrawer;


	// draws hits
	private CentralXYHitDrawer _hitDrawer;

	//bank matches
	private static String _defMatches[] = {"BMT", "BST", "CVT", "CTOF"};


	/**
	 * Create a Central detector XY View
	 *
	 * @param keyVals
	 */
	private CentralXYView(Object... keyVals) {
		super(keyVals);

		_crossDrawer = new CrossDrawerXY(this);
		_clusterDrawer = new ClusterDrawerXY(this);
		_hitDrawer = new CentralXYHitDrawer(this);

		// draws any swum trajectories (in the after draw)
		_swimTrajectoryDrawer = new SwimTrajectoryDrawer(this);

		// add the CND polys
		for (int layer = 1; layer <= 3; layer++) {
			for (int paddleId = 1; paddleId <= 48; paddleId++) {
				_cndPoly[layer - 1][paddleId - 1] = new CNDXYPolygon(layer, paddleId);
			}
		}

		// ad the ctof polygons
		for (int paddleId = 1; paddleId <= 48; paddleId++) {
			_ctofPoly[paddleId - 1] = new CTOFXYPolygon(paddleId);
		}

	}

	/**
	 * Create a Central detector XY view
	 *
	 * @return a Central detector XY View
	 */
	public static CentralXYView createCentralXYView() {

		// set to a fraction of screen
		Dimension d = GraphicsUtilities.screenFraction(0.35);

		// make it square
		int width = d.width;
		int height = width;

		String title = _baseTitle + ((CLONE_COUNT == 0) ? "" : ("_(" + CLONE_COUNT + ")"));

		// create the view
		final CentralXYView view = new CentralXYView(PropertySupport.WORLDSYSTEM, _defaultWorldRectangle,
				PropertySupport.WIDTH, width, PropertySupport.HEIGHT, height, PropertySupport.LEFTMARGIN, LMARGIN,
				PropertySupport.TOPMARGIN, TMARGIN, PropertySupport.RIGHTMARGIN, RMARGIN, PropertySupport.BOTTOMMARGIN,
				BMARGIN, PropertySupport.TOOLBAR, true, PropertySupport.TOOLBARBITS, CedView.TOOLBARBITS,
				PropertySupport.VISIBLE, true, PropertySupport.TITLE, title, PropertySupport.PROPNAME, "CentralXY",
				PropertySupport.STANDARDVIEWDECORATIONS,
				true);

		view._controlPanel = new ControlPanel(view,
				ControlPanel.DISPLAYARRAY + ControlPanel.FEEDBACK + ControlPanel.ACCUMULATIONLEGEND
						+ ControlPanel.MATCHINGBANKSPANEL,
				DisplayBits.ACCUMULATION + DisplayBits.CROSSES + DisplayBits.CLUSTERS + DisplayBits.MCTRUTH + DisplayBits.RECONHITS
						+ DisplayBits.ADCDATA + DisplayBits.CVTRECTRACKS + DisplayBits.CVTP1TRACKS
						+ DisplayBits.CVTRECTRAJ + DisplayBits.CVTP1TRAJ + DisplayBits.COSMICS + DisplayBits.GLOBAL_HB
						+ DisplayBits.GLOBAL_TB,
				3, 5);

		view.add(view._controlPanel, BorderLayout.EAST);
		view.pack();

		// add quick zooms
		view.addQuickZoom("BST & BMT", -190, -190, 190, 190);

		//i.e. if none were in the properties
		if (view.hasNoBankMatches()) {
			view.setBankMatches(_defMatches);
		}

		view._controlPanel.getMatchedBankPanel().update();
		return view;
	}

	/**
	 * Create the view's before drawer.
	 */
	@Override
	protected void setBeforeDraw() {
		// use a before-drawer to sector dividers and labels
		IDrawable beforeDraw = new DrawableAdapter() {

			@Override
			public void draw(Graphics g, IContainer container) {
				Component component = container.getComponent();
				Rectangle b = component.getBounds();

				// ignore b.x and b.y as usual

				b.x = 0;
				b.y = 0;

				Rectangle screenRect = container.getInsetRectangle();
				g.setColor(Color.white);
				g.fillRect(screenRect.x, screenRect.y, screenRect.width, screenRect.height);

				drawBSTPanels(g, container);
			}

		};

		getContainer().setBeforeDraw(beforeDraw);
	}

	/**
	 * Set the view's after draw
	 */
	@Override
	protected void setAfterDraw() {

		IDrawable afterDraw = new DrawableAdapter() {

			@Override
			public void draw(Graphics g, IContainer container) {

				if (!_eventManager.isAccumulating()) {

					_hitDrawer.draw(g, container);

					_swimTrajectoryDrawer.draw(g, container);
					if (showCosmics()) {
						drawCosmicTracks(g, container);
					}

					if (showCrosses()) {
						_crossDrawer.draw(g, container);
					}

					if (showClusters()) {
						_clusterDrawer.draw(g, container);
					}
					
					//data selected highlight?
					drawDataSelectedHighlight(g, container);



					Rectangle screenRect = getActiveScreenRectangle(container);
					drawAxes(g, container, screenRect, true);

				}

			}

		};
		getContainer().setAfterDraw(afterDraw);
	}
	
	//draw data selected hightlight data
	private void drawDataSelectedHighlight(Graphics g, IContainer container) {

		DataEvent dataEvent = ClasIoEventManager.getInstance().getCurrentEvent();
		if (dataEvent == null) {
			return;
		}

		if (showClusters()) {
			drawHighlightClusters(g, container, dataEvent);
		}

	}
	
	//draw highlighted clusters
	private void drawHighlightClusters(Graphics g, IContainer container, DataEvent dataEvent) {
		// indices are zero based
		if ((_bstHighlightData.cluster >= 0) && dataEvent.hasBank("BSTRec::Clusters")) {
			
			if (ColumnData.getFloatArray("BSTRec::Clusters.x1") == null) {
				return;
			}
			int idx = _bstHighlightData.cluster; // 0 based

			float x1 = ColumnData.getFloatArray("BSTRec::Clusters.x1")[idx];
			float y1 = ColumnData.getFloatArray("BSTRec::Clusters.y1")[idx];
			float x2 = ColumnData.getFloatArray("BSTRec::Clusters.x2")[idx];
			float y2 = ColumnData.getFloatArray("BSTRec::Clusters.y2")[idx];
			Point p1 = new Point();
			Point p2 = new Point();

			container.worldToLocal(p1, 10 * x1, 10 * y1);
			container.worldToLocal(p2, 10 * x2, 10 * y2);
			g.setColor(Color.black);
			g.drawLine(p1.x, p1.y, p2.x, p2.y);
			DataDrawSupport.drawReconClusterHighlight(g, p1);
			DataDrawSupport.drawReconClusterHighlight(g, p2);
		}
		
		if ((_bmtHighlightData.cluster >= 0) && dataEvent.hasBank("BMTRec::Clusters")) {
			int idx = _bmtHighlightData.cluster; // 0 based

			float x1 = ColumnData.getFloatArray("BMTRec::Clusters.x1")[idx];
			float y1 = ColumnData.getFloatArray("BMTRec::Clusters.y1")[idx];
			float x2 = ColumnData.getFloatArray("BMTRec::Clusters.x2")[idx];
			float y2 = ColumnData.getFloatArray("BMTRec::Clusters.y2")[idx];
			Point p1 = new Point();
			Point p2 = new Point();

			container.worldToLocal(p1, 10 * x1, 10 * y1);
			container.worldToLocal(p2, 10 * x2, 10 * y2);
			g.setColor(Color.black);
			g.drawLine(p1.x, p1.y, p2.x, p2.y);
			DataDrawSupport.drawReconClusterHighlight(g, p1);
			DataDrawSupport.drawReconClusterHighlight(g, p2);
		}

	}


	// draw cosmic ray tracks
	private void drawCosmicTracks(Graphics g, IContainer container) {

		CosmicList cosmics;
		cosmics = Cosmics.getInstance().getCosmics();

		if ((cosmics == null) || cosmics.isEmpty()) {
			return;
		}

		Shape oldClip = clipView(g);

		Point p1 = new Point();
		Point p2 = new Point();
		for (Cosmic cosmic : cosmics) {
			double y1 = 100;
			double y2 = -100;
			double x1 = cosmic.trkline_yx_slope * y1 + cosmic.trkline_yx_interc;
			double x2 = cosmic.trkline_yx_slope * y2 + cosmic.trkline_yx_interc;
			// convert to mm
			x1 *= 10;
			x2 *= 10;
			y1 *= 10;
			y2 *= 10;
			container.worldToLocal(p1, x1, y1);
			container.worldToLocal(p2, x2, y2);

			g.setColor(Color.red);
			g.drawLine(p1.x, p1.y, p2.x, p2.y);

		}

		g.setClip(oldClip);
	}

	/**
	 * Get the panel based on the layer and sector
	 *
	 * @param layer  1-based layer 1..6
	 * @param sector 1-based sector (max is layer dependent)
	 * @return the panel
	 */
	public static BSTxyPanel getPanel(int layer, int sector) {
		List<BSTxyPanel> panels = GeometryManager.getBSTxyPanels();
		if (panels == null) {
			return null;
		}

		synchronized (panels) {
			for (BSTxyPanel panel : panels) {
				if ((panel.getLayer() == layer) && (panel.getSector() == sector)) {
					return panel;
				}
			}
		}

		System.err.println("Null BST xy panel");

		return null;
	}

	// draw the panels
	private void drawBSTPanels(Graphics g, IContainer container) {

		Shape oldClip = g.getClip();

		List<BSTxyPanel> panels = GeometryManager.getBSTxyPanels();
		if (panels == null) {
			return;
		}

		Graphics2D g2 = (Graphics2D) g;

		Rectangle sr = container.getInsetRectangle();
		g2.clipRect(sr.x, sr.y, sr.width, sr.height);

		// BST panels
		for (BSTxyPanel panel : panels) {
			drawBSTPanel(g2, container, panel, _panelColors[(panel.getSector()) % 2]);
		}

		// CND Polys
		for (int layer = 1; layer <= 3; layer++) {
			for (int paddleId = 1; paddleId <= 48; paddleId++) {
				if (_cndPoly[layer - 1][paddleId - 1] != null) {
					_cndPoly[layer - 1][paddleId - 1].draw(g2, container);
				}
			}

		}

		// CTOF Polys
		for (int paddleId = 1; paddleId <= 48; paddleId++) {
			if (_ctofPoly[paddleId - 1] != null) {
				_ctofPoly[paddleId - 1].draw(g2, container, paddleId, _ctofColors[paddleId % 2]);
			}
		}

		g.setClip(oldClip);
	}

	// draw one BST panel
	public void drawBSTPanel(Graphics2D g2, IContainer container, BSTxyPanel panel, Color color) {

		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		Stroke oldStroke = g2.getStroke();
		g2.setColor(color);
		Point p1 = new Point();
		Point p2 = new Point();
		Point2D.Double wp1 = new Point2D.Double();
		Point2D.Double wp2 = new Point2D.Double();
		g2.setStroke((panel == _closestPanel) ? stroke2 : stroke);
		// Just draw a line from (x1,y1) to (x2,y2)

		wp1.setLocation(panel.getX1(), panel.getY1());
		wp2.setLocation(panel.getX2(), panel.getY2());
		container.worldToLocal(p1, wp1);
		container.worldToLocal(p2, wp2);
		g2.drawLine(p1.x, p1.y, p2.x, p2.y);
		g2.setStroke(oldStroke);

		// draw sector number

		Point porig = new Point();
		Point pmid = new Point();
		Point2D.Double wporig = new Point2D.Double();
		container.worldToLocal(porig, wporig);
		g2.setFont(Fonts.smallFont);
		FontMetrics fm = getFontMetrics(g2.getFont());

		if ((panel.getLayer() % 2) == 0) {
			g2.setColor(TEXT);
			pmid.x = (p1.x + p2.x) / 2;
			pmid.y = (p1.y + p2.y) / 2;
			String s = "" + panel.getSector();
			extendLine(porig, pmid, 4 + panel.getLayer() / 2, fm.stringWidth(s), fm.getHeight());
			g2.drawString(s, pmid.x, pmid.y);
		}
	}

	private static void extendLine(Point p0, Point p1, int del, int sw, int fh) {
		double dx = p1.x - p0.x;
		double dy = p1.y - p0.y;
		double theta = Math.atan2(dy, dx);

		int quad = ((int) ((180 + Math.toDegrees(theta)) / 90)) % 4;

		int delx = (int) (del * Math.cos(theta));
		int dely = (int) (del * Math.sin(theta));

		if (quad == 0) {
			delx = delx - sw;
			// dely = dely + fh/2;
		} else if (quad == 2) {
			delx = delx + 2;
			dely = dely + fh / 2;
		} else if (quad == 3) {
			delx = delx - sw;
			dely = dely + fh / 2;
		}

		p1.x += delx;
		p1.y += dely;
	}

	/**
	 * This adds the detector items.
	 */
	@Override
	protected void addItems() {
		// BMT sectors for now only layers 5 & 6
		LogicalLayer detectorLayer = getContainer().getLogicalLayer(_detectorLayerName);

		_bmtItems = new BMTSectorItem[3][6];
		for (int sect = 1; sect <= 3; sect++) {
			for (int lay = 1; lay <= 6; lay++) {
				_bmtItems[sect - 1][lay - 1] = new BMTSectorItem(detectorLayer, sect, lay);
			}
		}
	}

	/**
	 * Get the BMT Sector item
	 *
	 * @param sector the geo sector 1..3
	 * @param layer  the layer 1..6
	 * @return the BMS Sector Item
	 */
	public BMTSectorItem getBMTSectorItem(int sector, int layer) {
		if ((sector < 1) || (sector > 3) || (layer < 1) || (layer > 6)) {
			return null;
		}
		return _bmtItems[sector - 1][layer - 1];
	}

	/**
	 * Get the panel closest to the mouse
	 *
	 * @return the panel closest to the mouse
	 */
	protected BSTxyPanel closestPanel() {
		return _closestPanel;
	}

	/**
	 * Some view specific feedback. Should always call super.getFeedbackStrings
	 * first.
	 *
	 * @param container   the base container for the view.
	 * @param screenPoint the pixel point
	 * @param worldPoint  the corresponding world location.
	 */
	@Override
	public void getFeedbackStrings(IContainer container, Point screenPoint, Point2D.Double worldPoint,
			List<String> feedbackStrings) {

		basicFeedback(container, screenPoint, worldPoint, "mm", feedbackStrings);

		if (!Environment.getInstance().isDragging()) {
			BSTxyPanel newClosest = getClosest(worldPoint);
			if (newClosest != _closestPanel) {
				_closestPanel = newClosest;
				container.refresh();
			}
		}

		if (_closestPanel != null) {

			int region = (_closestPanel.getLayer() + 1) / 2;
			fbString("red", "BST layer " + _closestPanel.getLayer(), feedbackStrings);
			fbString("red", "BST region " + region, feedbackStrings);
			fbString("red", "BST sector " + _closestPanel.getSector(), feedbackStrings);
		} else {
			double rad = Math.hypot(worldPoint.x, worldPoint.y);
			boolean found = false;

			// cnd ?
			if ((rad > 288) && (rad < 382)) {

				for (int layer = 1; layer <= 3; layer++) {
					for (int paddleId = 1; paddleId <= 48; paddleId++) {

						found = _cndPoly[layer - 1][paddleId - 1].getFeedbackStrings(container, screenPoint, worldPoint,
								feedbackStrings);

						if (found) {
							break;
						}
					}

					if (found) {
						break;
					}

				}
			}

			// ctof
			else if ((rad > CTOFGeometry.RINNER) && (rad < CTOFGeometry.ROUTER)) {

				for (int index = 0; index < 48; index++) {
					if (_ctofPoly[index].contains(screenPoint)) {
						int paddle = index + 1;
						TdcAdcTOFHit hit = null;
						TdcAdcTOFHitList hits = CTOF.getInstance().getHits();
						if ((hits != null) && !hits.isEmpty()) {
							hit = hits.get(0, 0, paddle);
						}

						if (hit == null) {
							feedbackStrings.add("$dodger blue$" + "CTOF paddle " + paddle);
						} else {
							hit.tdcAdcFeedback("CTOF paddle", feedbackStrings);
						}

						break;
					}

				}
			}

		}

		// hits data

		if (_closestPanel != null) {
			AdcList hits = BST.getInstance().getADCHits();
			if ((hits != null) && !hits.isEmpty()) {
				Vector<int[]> stripADCData = BST.getInstance().allStripsForSectorAndLayer(_closestPanel.getSector(),
						_closestPanel.getLayer());
				for (int sdtdat[] : stripADCData) {
					fbString("orange", "strip  " + sdtdat[0] + " adc " + +sdtdat[1], feedbackStrings);
				}
			}
		}

		// BMT?

		if (showADCHits()) {
			AdcList adcHits = BMT.getInstance().getADCHits();
			if ((adcHits != null) && !adcHits.isEmpty()) {
				for (AdcHit adcHit : adcHits) {
					if (adcHit.contains(screenPoint)) {
						adcHit.adcFeedback("layer", "strip", feedbackStrings);
						break;
					}
				}
			}
		}

		boolean foundHit = false;
		if (showReconHits()) {
			BaseHit2List reconHits = BMT.getInstance().getRecHits();
			if ((reconHits != null) && !reconHits.isEmpty()) {
				for (BaseHit2 bhit2 : reconHits) {
					if (bhit2.contains(screenPoint)) {
						fbString("orange", "BMT recon hit sector  " + bhit2.sector + " layer " + bhit2.layer + " strip "
								+ bhit2.component, feedbackStrings);
						foundHit = true;
						break;
					}
				}
			}
			if (!foundHit) {
				reconHits = BST.getInstance().getRecHits();
				if ((reconHits != null) && !reconHits.isEmpty()) {
					for (BaseHit2 bhit2 : reconHits) {
						if (bhit2.contains(screenPoint)) {
							fbString("orange", "BST recon hit sector  " + bhit2.sector + " layer " + bhit2.layer + " strip "
									+ bhit2.component, feedbackStrings);

							foundHit = true;
							break;
						}
					}
				}

			}

		}

		// near a swum trajectory?
		double mindist = _swimTrajectoryDrawer.closestApproach(worldPoint);
		double pixlen = WorldGraphicsUtilities.getMeanPixelDensity(container) * mindist;

		_lastTrajStr = null; // for hovering response
		if (pixlen < 25.0) {
			SwimTrajectory2D traj2D = _swimTrajectoryDrawer.getClosestTrajectory();
			if (traj2D != null) {
				traj2D.addToFeedback(feedbackStrings);
				_lastTrajStr = traj2D.summaryString();
			}
		}

		// see if any feedback from simulated data
		getGemcFeedback(container, screenPoint, worldPoint, feedbackStrings);

		// reconstructed feedback?
		_crossDrawer.feedback(container, screenPoint, worldPoint, feedbackStrings);

		// hit feedback
		_hitDrawer.feedback(container, screenPoint, worldPoint, feedbackStrings);

	}

	// get the panel closest to a given point
	private BSTxyPanel getClosest(Point2D.Double wp) {
		List<BSTxyPanel> panels = GeometryManager.getBSTxyPanels();
		if (panels == null) {
			return null;
		}

		BSTxyPanel closest = null;
		double minDistance = Double.MAX_VALUE;

		for (BSTxyPanel panel : panels) {
			double dist = panel.pointToLineDistance(wp);
			if (dist < minDistance) {
				closest = panel;
				minDistance = dist;
			}
		}

		if (minDistance > 6.) {
			closest = null;
		}

		return closest;
	}

	// feedback from simulated data
	private void getGemcFeedback(IContainer container, Point screenPoint, Point2D.Double worldPoint,
			List<String> feedbackStrings) {

	}

	/**
	 * Get a CTOF scintillator polygon
	 *
	 * @param index1 the 1=based index [1..48]
	 * @return the most recently drawn polygon
	 */
	public CTOFXYPolygon getCTOFPolygon(int index1) {
		int index0 = index1 - 1;
		if ((index0 < 0) || (index0 > 47)) {
			return null;
		}
		return _ctofPoly[index0];
	}

	/**
	 * Get the CND polygon from Gagik's geometry layer and paddle
	 *
	 * @param layer    1..3
	 * @param paddleId 1..48
	 * @return the CND polygon
	 */
	public CNDXYPolygon getCNDPolygon(int layer, int paddleId) {
		if ((layer < 1) || (layer > 3) || (paddleId < 1) || (paddleId > 48)) {
			return null;
		}

		return _cndPoly[layer - 1][paddleId - 1];
	}

	/**
	 * Get the CND polygon from "real" numbering
	 *
	 * @param sector    1..24
	 * @param layer     1..3
	 * @param component 1..2
	 * @return the CND polygon
	 */
	public CNDXYPolygon getCNDPolygon(int sector, int layer, int component) {
		if ((sector < 1) || (sector > 24) || (layer < 1) || (layer > 3)) {
			return null;
		}
		if ((component < 1) || (component > 2)) {
			return null;
		}

		int real[] = { sector, layer, component };
		int geo[] = new int[3];

		CNDGeometry.realTripletToGeoTriplet(geo, real);

		return getCNDPolygon(geo[1], geo[2]);
	}

	/**
	 * Clone the view.
	 *
	 * @return the cloned view
	 */
	@Override
	public BaseView cloneView() {
		super.cloneView();
		CLONE_COUNT++;

		// limit
		if (CLONE_COUNT > 2) {
			return null;
		}

		Rectangle vr = getBounds();
		vr.x += 40;
		vr.y += 40;

		CentralXYView view = createCentralXYView();
		view.setBounds(vr);
		return view;

	}

	/**
	 * Convert lab coordinates (CLAS x,y,z) to world coordinates (2D world system of the view)
	 * @param x the CLAS12 x coordinate
	 * @param y the CLAS12 y coordinate
	 * @param z the CLAS12 z coordinate
	 * @param wp holds the world point
	 */
	@Override
	public void labToWorld(double x, double y, double z, Point2D.Double wp) {
		wp.x = x;
		wp.y = y;
	}
	
	/**
	 * In the BankDataTable a row was selected.
	 * @param bankName the name of the bank
	 * @param index the 0-based index into the bank
	 */
	@Override
	public void dataSelected(String bankName, int index) {
		System.out.println("CentralXY selected [" + bankName + "]  index: " + index);
		
		if ("BMT::adc".equals(bankName)) {
		}
		else if ("BMTRec::Clusters".equals(bankName)) {
		}
		else if ("BMTRec::Crosses".equals(bankName)) {
		}
		else if ("BMTRec::Hits".equals(bankName)) {
			_bstHighlightData.hit = index;
		}
		else if ("BST::adc".equals(bankName)) {
		}
		else if ("BSTRec::Clusters".equals(bankName)) {
			_bstHighlightData.cluster = index;
		}
		else if ("BSTRec::Crosses".equals(bankName)) {
			_bstHighlightData.cross = index;
		}
		else if ("BSTRec::Hits".equals(bankName)) {
		}
		else if ("CTOF::adc".equals(bankName)) {
		}
		else if ("CTOF::hits".equals(bankName)) {
		}
		else if ("CTOF::tdc".equals(bankName)) {
		}
		else if ("CVTRec::Tracks".equals(bankName)) {
		}
		else if ("CVTRec::Trajectory".equals(bankName)) {
		}
		else if ("CVTRec::uTracks".equals(bankName)) {
		}

		refresh();

	}
}
