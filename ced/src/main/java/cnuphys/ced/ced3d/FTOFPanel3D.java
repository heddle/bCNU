package cnuphys.ced.ced3d;

import com.jogamp.opengl.GLAutoDrawable;

import cnuphys.ced.event.data.FTOF;
import cnuphys.ced.event.data.TdcAdcTOFHit;
import cnuphys.ced.event.data.lists.TdcAdcTOFHitList;
import cnuphys.ced.geometry.ftof.FTOFGeometry;

public class FTOFPanel3D extends DetectorItem3D {

	// individual paddles
	private FTOFPaddle3D _paddles[];

	// one based sector [1..6]
	private final int _sector;

	// "superlayer" [PANEL_1A, PANEL_1B, PANEL_2] (0, 1, 2)
	private final int _panelId;

	/**
	 * An FTOF Panel 3D item
	 *
	 * @param panel3d the owner graphical panel
	 * @param sector  the sector 1..6
	 * @param panelId the super layer [PANEL_1A, PANEL_1B, PANEL_2] (0, 1, 2)
	 */
	public FTOFPanel3D(PlainPanel3D panel3D, int sector, int panelId) {
		super(panel3D);
		_sector = sector;
		_panelId = panelId;

		_paddles = new FTOFPaddle3D[FTOFGeometry.numPaddles[panelId]];
		for (int paddleId = 1; paddleId <= _paddles.length; paddleId++) {
			_paddles[paddleId - 1] = new FTOFPaddle3D(sector, panelId, paddleId);
		}
	}

	/**
	 * Get the number of paddles
	 *
	 * @return the number of paddles
	 */
	public int getPaddleCount() {
		return _paddles.length;
	}

	/**
	 * Get the paddle
	 *
	 * @param paddleId the 1-based index
	 * @return the paddle
	 */
	public FTOFPaddle3D getPaddle(int paddleId) {
		return _paddles[paddleId - 1];
	}

	@Override
	public void drawShape(GLAutoDrawable drawable) {
	}

	@Override
	public void drawData(GLAutoDrawable drawable) {

		// draw tdc adc hits
		TdcAdcTOFHitList hits = FTOF.getInstance().getTdcAdcHits();
		if (!hits.isEmpty()) {
			byte layer = (byte) (_panelId + 1);
			for (TdcAdcTOFHit hit : hits) {
				if ((hit.sector == _sector) && (hit.layer == layer)) {
					getPaddle(hit.component).drawPaddle(drawable, hits.adcColor(hit));
				}
			}
		}


	}

	/**
	 * Get the sector [1..6]
	 *
	 * @return the sector 1..6
	 */
	public int getSector() {
		return _sector;
	}

	/**
	 * Get the superlayer [PANEL_1A, PANEL_1B, PANEL_2] (0, 1, 2)
	 *
	 * @return the superlayer [PANEL_1A, PANEL_1B, PANEL_2] (0, 1, 2)
	 */
	public int getSuperLayer() {
		return _panelId;
	}

	// show FTOFs?
	@Override
	protected boolean show() {
		return _cedPanel3D.showFTOF();
	}

}
