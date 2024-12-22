package cnuphys.ced.cedview.alert;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.List;

import org.jlab.geom.component.ScintillatorPaddle;
import org.jlab.io.base.DataEvent;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.ced.alldata.DataWarehouse;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.event.AccumulationManager;
import cnuphys.ced.geometry.alert.AlertGeometry;
import cnuphys.ced.geometry.alert.TOFLayer;

public class AlertTOFHitDrawer {

	// data warehouse
	private DataWarehouse _dataWarehouse = DataWarehouse.getInstance();

	// the alert view
	private AlertXYView _view;

	/**
	 * Create a TOF hit drawer for the alert view.
	 *
	 * @param view the alert view.
	 */
	public AlertTOFHitDrawer(AlertXYView view) {
		_view = view;
	}

	/**
	 * Draw the hits
	 * 
	 * @param g
	 * @param container
	 */
	public void drawHits(Graphics g, IContainer container) {

		if (ClasIoEventManager.getInstance().isAccumulating()) {
			return;
		}

		DataEvent dataEvent = ClasIoEventManager.getInstance().getCurrentEvent();
		if (dataEvent == null) {
			return;
		}

		drawTOFHits(g, container, dataEvent);

	}

	// draw the TOF hits
	private void drawTOFHits(Graphics g, IContainer container, DataEvent dataEvent) {
		if (dataEvent.hasBank("ATOF::adc") && _view.showADCHits()) {

			short component[] = _dataWarehouse.getShort("ATOF::adc", "component");
			if (component != null) {
				int count = component.length;
				if (count > 0) {
					byte sector[] = _dataWarehouse.getByte("ATOF::adc", "sector");
					byte compLayer[] = _dataWarehouse.getByte("ATOF::adc", "layer");
					byte order[] = _dataWarehouse.getByte("ATOF::adc", "order");

					AlertTOFGeometryNumbering adcGeom = new AlertTOFGeometryNumbering();

					for (int i = 0; i < count; i++) {
						adcGeom.fromDataNumbering(sector[i], compLayer[i], component[i], order[i]);
						TOFLayer tofl = AlertGeometry.getTOFLayer(adcGeom.sector, adcGeom.superlayer, adcGeom.layer);
						if (tofl == null) {
							System.err.println("TOF layer not found for sector " + adcGeom.sector + ", superlayer "
									+ adcGeom.superlayer + ", layer " + adcGeom.layer);
							continue;
						}

						// mod 4, there are 4 paddles per layer with ids 0..59
						ScintillatorPaddle paddle = tofl.getPaddle(adcGeom.component % 4);
						tofl.drawPaddle(g, container, paddle, Color.red, Color.black);
					}
				}
			}
		}

	} // drawTOFHits
	
	public void drawAccumulatedHits(Graphics g, IContainer container) {
		drawAccumulatedHitsSL(g, container, 0);
		drawAccumulatedHitsSL(g, container, 1);

	}

	
	/**
	 * Draw the accumulated hits
	 * @param g
	 * @param container
	 */
	private void drawAccumulatedHitsSL(Graphics g, IContainer container, int superlayer) {
		
		int maxHit;
		int counts[][][];
		
		if (superlayer == 0) {
			maxHit = AccumulationManager.getInstance().getMaxAlertTOFSL0Count();
			counts = AccumulationManager.getInstance().getAccumulatedAlertTOFSL0Data();
		} else {
			maxHit = AccumulationManager.getInstance().getMaxAlertTOFSL1Count();
			counts = AccumulationManager.getInstance().getAccumulatedAlertTOFSL1Data();
		}
		

		if (counts == null) {
			return;
		}


		for (int sector = 0; sector < counts.length; sector++) {
			for (int layer = 0; layer < counts[sector].length; layer++) {
				TOFLayer tofl = AlertGeometry.getTOFLayer(sector, superlayer, layer);
				if (tofl == null) {
					continue;
				}

				for (int paddle = 0; paddle < counts[sector][layer].length; paddle++) {
					double count = counts[sector][layer][paddle];
					double fract = (maxHit == 0) ? 0 : (count / maxHit);
					Color color = AccumulationManager.getInstance().getColor(_view.getColorScaleModel(), fract);
					ScintillatorPaddle scintPaddle = tofl.getPaddle(paddle);
					tofl.drawPaddle(g, container, scintPaddle, color, Color.black);
				}
			}
		}
	}

	/**
	 * Get feedback strings for a hit
	 * 
	 * @param container       the container
	 * @param pp              the pixel point
	 * @param wp              the world point
	 * @param tofl            the TOF layer
	 * @param feedbackStrings the list of feedback strings to add to
	 */
	public void getHitFeedbackStrings(IContainer container, Point pp, Point2D.Double wp, TOFLayer tofl,
			List<String> feedbackStrings) {

		if (ClasIoEventManager.getInstance().isAccumulating() || _view.isAccumulatedMode()) {
			return;
		}

		DataEvent dataEvent = ClasIoEventManager.getInstance().getCurrentEvent();
		if (dataEvent == null) {
			return;
		}

		if (!dataEvent.hasBank("ATOF::adc")) {
			return;
		}

		short component[] = _dataWarehouse.getShort("ATOF::adc", "component");
		if (component != null) {
			int count = component.length;
			if (count > 0) {
				byte sector[] = _dataWarehouse.getByte("ATOF::adc", "sector");
				byte compLayer[] = _dataWarehouse.getByte("ATOF::adc", "layer");
				byte order[] = _dataWarehouse.getByte("ATOF::adc", "order");

				AlertTOFGeometryNumbering adcGeom = new AlertTOFGeometryNumbering();

				for (int i = 0; i < count; i++) {
					adcGeom.fromDataNumbering(sector[i], compLayer[i], component[i], order[i]);
					if (adcGeom.match(tofl)) {

						// mod 4, there are 4 paddles per layer with ids 0..59
						ScintillatorPaddle paddle = tofl.getPaddle(adcGeom.component % 4);

						boolean contains = tofl.paddleContains(paddle, pp);
						if (contains) {
							String bankName = "ATOF::adc";
							AlertFeedbackSupport.handleInt(bankName, "ADC", i, "$orange$", feedbackStrings);
							AlertFeedbackSupport.handleByte(bankName, "order", i, "$orange$", feedbackStrings);
							AlertFeedbackSupport.handleShort(bankName, "ped", i, "$orange$", feedbackStrings);
							AlertFeedbackSupport.handleFloat(bankName, "time", i, "$orange$", feedbackStrings);
							return;
						}

					}

				}
			}
		}

	}

}