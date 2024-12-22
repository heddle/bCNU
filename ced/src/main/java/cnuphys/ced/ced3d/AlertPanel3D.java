package cnuphys.ced.ced3d;


import java.awt.Color;
import java.awt.Font;

import cnuphys.ced.ced3d.view.CedView3D;
import cnuphys.ced.geometry.BSTGeometry;
import cnuphys.ced.geometry.alert.AlertGeometry;
import cnuphys.ced.geometry.alert.DCLayer;
import cnuphys.lund.X11Colors;
import item3D.Axes3D;


public class AlertPanel3D extends CedPanel3D {

	
	// dimension of this panel are in mm
	private final float xymax = 150f;
	private final float zmax = 200f;
	private final float zmin = -20f;
	
	// labels for the check box
	private static final String _cbaLabels[] = { SHOW_VOLUMES, SHOW_TRUTH, SHOW_RECON_CROSSES, SHOW_TB_TRACK,
			SHOW_HB_TRACK, SHOW_COSMIC, SHOW_DC, SHOW_TOF,
			DC_SUPLAY1_LAY1, DC_SUPLAY2_LAY1, DC_SUPLAY2_LAY2, DC_SUPLAY3_LAY1, DC_SUPLAY3_LAY2, DC_SUPLAY4_LAY1,
			DC_SUPLAY4_LAY2, DC_SUPLAY5_LAY1, 
			TOF_SUPLAY1_LAY1, TOF_SUPLAY2_LAY1, TOF_SUPLAY2_LAY2, TOF_SUPLAY2_LAY3, TOF_SUPLAY2_LAY4, TOF_SUPLAY2_LAY5,
			TOF_SUPLAY2_LAY6, TOF_SUPLAY2_LAY7, TOF_SUPLAY2_LAY8, TOF_SUPLAY2_LAY9, TOF_SUPLAY2_LAY10,};
	

	/**
	 *
	 * @param view
	 * @param angleX
	 * @param angleY
	 * @param angleZ
	 * @param xDist
	 * @param yDist
	 * @param zDist
	 */
	public AlertPanel3D(CedView3D view, float angleX, float angleY, float angleZ, float xDist, float yDist,
			float zDist) {
		super(view, angleX, angleY, angleZ, xDist, yDist, zDist, _cbaLabels);
	}
	
	@Override
	public void createInitialItems() {
		// coordinate axes
		Axes3D axes = new Axes3D(this, -xymax, xymax, -xymax, xymax, zmin, zmax, null, Color.darkGray, 1f, 6, 6, 6,
				Color.black, X11Colors.getX11Color("Dark Green"), new Font("SansSerif", Font.PLAIN, 12), 0);
		addItem(axes);

		// trajectory drawer
		TrajectoryDrawer3D trajDrawer = new TrajectoryDrawer3D(this);
		addItem(trajDrawer);
		
		for (int sectorId = 0; sectorId < 1; sectorId++) {
			for (int superlayerId = 0; superlayerId < 5; superlayerId++) {
				for (int layerId = 0; layerId < 2; layerId++) {
					DCLayer dcLayer = AlertGeometry.getDCLayer(sectorId, superlayerId, layerId);
					if ((dcLayer != null) && (dcLayer.numWires > 0)) {
						AlertDCLayer3D dc = new AlertDCLayer3D(this, sectorId, superlayerId, layerId);
						addItem(dc);
					}
				}
			}
		}

		
		// tof paddles superlayer 0
		for (int sectorId = 0; sectorId < 15; sectorId++) {
			for (int superlayerId = 0; superlayerId < 1; superlayerId++) {
				for (int layerId = 0; layerId < 1; layerId++) {
					for (int paddleId = 3; paddleId >= 0; paddleId--) {
						AlertPaddle3D tof = new AlertPaddle3D(this, sectorId, superlayerId, layerId, paddleId);
						addItem(tof);
					}
				}
			}
		}
		
		// tof paddles superlayer 1-10
		for (int sectorId = 0; sectorId < 15; sectorId++) {
			for (int superlayerId = 1; superlayerId < 2; superlayerId++) {
				for (int layerId = 9; layerId >= 0; layerId--) {
					for (int paddleId = 3; paddleId >= 0; paddleId--) {
						AlertPaddle3D tof = new AlertPaddle3D(this, sectorId, superlayerId, layerId, paddleId);
						addItem(tof);
					}
				}
			}
		}
		

	}

	/**
	 * This gets the z step used by the mouse and key adapters, to see how fast we
	 * move in or in in response to mouse wheel or up/down arrows. It should be
	 * overridden to give something sensible. like the scale/100;
	 *
	 * @return the z step (changes to zDist) for moving in and out
	 */
	@Override
	public float getZStep() {
		return (zmax - zmin) / 50f;
	}
}