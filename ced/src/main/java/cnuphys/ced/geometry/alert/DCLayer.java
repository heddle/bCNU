package cnuphys.ced.geometry.alert;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.jlab.geom.detector.alert.AHDC.AlertDCLayer;
import org.jlab.geom.detector.alert.AHDC.AlertDCWire;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.world.WorldGraphicsUtilities;
import cnuphys.ced.cedview.alert.E_DCProjection;
import cnuphys.lund.X11Colors;

public class DCLayer {
	//LENGTH units are mm

	//layer shell
	private static Color[] shellColors = {X11Colors.getX11Color("Cornsilk"), X11Colors.getX11Color("Blanched Almond")};

	//wire fill
	private static Color wireFill = Color.white;

	/** assumed half radial width of gap */
	public static final double LAYERDR = 1.6; // mm;

	/** assumed half radial width of gap */
	public static final double WIRERAD = 0.85*LAYERDR; // mm;

	/** the 0-based sector of this layer */
	public final int sector;

	/** the 0-based superlayer of this layer */
	public final int superlayer;

	/** the 0-based layer ID of this layer */
	public final int layer;

	/** the number of wires */
	public final int numWires;

	/** the array of wires */
	public final Line3D wires[];

	/** the midpoints of the wires */
	public final Point2D.Double midpoints[];

	//for world xy shell
	private Point2D.Double _shell[];

	//for wire outline
	private Rectangle2D.Double _wrect[];

	//workspace

/**
 * Create a DC layer from an alert DC layer
 * @param geoAlertDCLayer the geoDatabase object
 */
	public DCLayer(AlertDCLayer geoAlertDCLayer) {
		sector = geoAlertDCLayer.getSectorId();
		superlayer = geoAlertDCLayer.getSuperlayerId();
		layer = geoAlertDCLayer.getLayerId();
		numWires = geoAlertDCLayer.getNumComponents();


		List<AlertDCWire> wireList = geoAlertDCLayer.getAllComponents();

		wires = new Line3D[numWires];
		midpoints = new Point2D.Double[numWires];

		int index = 0;

		
		for (AlertDCWire aw : wireList) {
			wires[index] = aw.getLine();
			Point3D midpoint = aw.getMidpoint();
			
			midpoints[index] = new Point2D.Double(midpoint.x(), midpoint.y());
			index++;
		}
		
		_wrect = new Rectangle2D.Double[numWires];
 		for (int wire = 0; wire < numWires; wire++) {
			_wrect[wire] = new Rectangle2D.Double();
		}
	}
	
	
	
	/**
	 * Get the 3D coords of the wire used in 3D drawing
	 * @param wire the 0-based wire id
	 * @param coords the 3D coords
	 */
	public void getWireCoords(int wire, float coords[]) {
		
		Point3D p0 = wires[wire].origin();
		Point3D p1 = wires[wire].end();
		coords[0] = (float) p0.x();
		coords[1] = (float) p0.y();
		coords[2] = (float) p0.z();
		coords[3] = (float) p1.x();
		coords[4] = (float) p1.y();
		coords[5] = (float) p1.z();
	}

	/**
	 * Get a wire as a 3D line
	 * @param wire the 0-based wire id
	 * @return the 3D line for the wire, or null
	 */
	public Line3D getLine(int wire) {
		if ((wire < 0) || (wire >= numWires)) {
			return null;
		}
		return wires[wire];
	}




	/**
	 * Get midpoint of wire as a 2D point
	 * @param wire the 0-based wire id
	 * @return midpoint of wire, or null
	 */
	public Point2D.Double getMidpoint(int wire) {
		if ((wire < 0) || (wire >= numWires)) {
			return null;
		}
		return midpoints[wire];
	}

	/**
	 * Contained in the XY view?
	 * @param wp the world point
	 * @return true if contained in this layer
	 */
	public boolean containsXY(Point2D.Double wp) {
		if (numWires == 0) {
			return false;
		}
		
		return WorldGraphicsUtilities.contains(_shell, wp);
	}
	
	/**
	 * Point contained by the wire oval?
	 * @param wire the 0-based wire id
	 * @param wp the world point
	 * @return true if contained
	 */
	public boolean wireContainsXY(int wire, Point2D.Double wp) {
		if ((wire < 0) || (wire >= numWires)) {
			return false;
		}
		return _wrect[wire].contains(wp);
	}

	/**
	 * Basic fb string for XY view
	 * @param feedbackStrings list to add to
	 */
	public void feedbackXYString(Point pp, Point2D.Double wp, List<String> feedbackStrings) {
		feedbackStrings.add(String.format("AlertDC GeoDB sector: %d", sector + 1));
		feedbackStrings.add(String.format("AlertDC GeoDB superlayer: %d", superlayer + 1));
		feedbackStrings.add(String.format("AlertDC GeoDB layer: %d", layer + 1));

		if (numWires > 0) {
			for (int wire = 0; wire < numWires; wire++) {
				if (_wrect[wire].contains(wp)) {
					feedbackStrings.add(String.format("AlertDC wire: %d", wire + 1));
					break;
				}
			}
		}
	}

	/**
	 * Draw the layer shell as a donut
	 * @param g the graphics object
	 * @param container the drawing container
	 */
	private void drawShell(Graphics g, IContainer container) {

		if (numWires < 1) {
			return;
		}

		Point pp = new Point();

		//to erase connecting line
		Point pp0 = new Point();
		Point pp1 = new Point();

		Polygon poly = new Polygon();

		for (int i = 0; i < _shell.length; i++) {
			container.worldToLocal(pp, _shell[i]);
			poly.addPoint(pp.x, pp.y);

			if (i == 0) {
				pp0.setLocation(pp);
			}
			else if (i == _shell.length/2) {
				pp1.setLocation(pp);
			}
		}


		Color fc = shellColors[layer];
		g.setColor(fc);
		g.fillPolygon(poly);
		g.setColor(Color.gray);
		g.drawPolygon(poly);

		g.setColor(fc);
		g.drawLine(pp0.x-1, pp0.y, pp1.x+1, pp1.y);

	}
	
	public double getWireXYatZ(int wire, double z, Point2D.Double xy) {
		Line3D line = wires[wire];
		Point3D p0 = line.origin();
		Point3D p1 = line.end();
		double t = (z - p0.z()) / (p1.z() - p0.z());
		xy.x = p0.x() + t * (p1.x() - p0.x());
		xy.y = p0.y() + t * (p1.y() - p0.y());
		return t;
	}
	
	public double getWireXYatTheta(int wire, double theta, Point2D.Double xy) {
		theta = Math.toRadians(theta);
		Line3D line = wires[wire];
		Point3D p0 = line.origin();
		Point3D p1 = line.end();
		double theta0 = getTheta(p0);
		double theta1 = getTheta(p1);
		
		if (theta < Math.min(theta0, theta1) || theta > Math.max(theta0, theta1)) {
			return -1;
		}
		
		if (theta0 > theta1) {
			Point3D temp = p0;
			p0 = p1;
			p1 = temp;
		}
		
		Point3D p3d = findPointWithTheta(p0, p1, theta);
		if (p3d == null) {
            return -1;
		}
		
		xy.x = p3d.x();
		xy.y = p3d.y();
		
//		double testTheta = Math.toDegrees(getTheta(p3d));
//		System.out.println("theta: " + Math.toDegrees(theta) + "testTheta: " + testTheta);
//		
//
		
		return 0.5; //dummy,anything 0-1 will do
	}

	private double getTheta(Point3D p3d) {
		double z = p3d.z();
		
		if (z < 1.0e-10) {
			return Math.PI/2;
		}
		
		double x = p3d.x();
		double y = p3d.y();
		double r = Math.sqrt(x * x + y * y + z * z);

	    return Math.acos(z/r);
	}
	
	//get the shell poly
	private void shellWorldPoly( E_DCProjection projection, double z, double theta) {
		
		ArrayList<Point2D.Double> points = new ArrayList<Point2D.Double>();
		for (int wire = 0; wire < numWires; wire++) {
			if (projection == E_DCProjection.MIDPOINT) {
				points.add(midpoints[wire]);
			} else if (projection == E_DCProjection.FIXED_Z) {
				Point2D.Double zp = new Point2D.Double();
				double t = getWireXYatZ(wire, z, zp);
				if ((t >= 0) && (t <= 1)) {
					points.add(zp);
				}
			}
			else { //fixed theta
				Point2D.Double tp = new Point2D.Double();
                double t = getWireXYatTheta(wire, theta, tp);
                if ((t >= 0) && (t <= 1)) {
                    points.add(tp);
                }
				
			}
		}
		
		if (points.size() < 2) {
			return;
		}
		
		int N2 = 2 * points.size();
		_shell = new Point2D.Double[N2];
		double x, y;
		
		int i = 0;
		for (Point2D.Double p : points) {
			int j = i + points.size();
			double radius = Math.hypot(p.x, p.y);
			double thet = Math.atan2(p.y, p.x);
			double cos = Math.cos(thet);
			double sin = Math.sin(thet);
			
			double innerMidPointRadius = radius - LAYERDR;
			double outerMidPointRadius = radius + LAYERDR;

			x = innerMidPointRadius * cos;
			y = innerMidPointRadius * sin;
			_shell[i] = new Point2D.Double(x, y);
			
			x = outerMidPointRadius * cos;
			y = outerMidPointRadius * sin;
			_shell[j] = new Point2D.Double(x, y);

			i++;
		}
	}

	/**
	 * Draw the wires
	 * @param g the graphics object
	 * @param container the drawing container
	 */
	public void drawXYWires(Graphics g, IContainer container, E_DCProjection projection, double z, double theta) {

		shellWorldPoly(projection, z, theta);
		//draw the poly
		drawShell(g, container);
		for (int wire = 0; wire < numWires; wire++) {
			drawXYWire(g, container, wire, wireFill, Color.darkGray, projection, z, theta);
		}
	}

	/**
	 * Draw a wire
	 * 
	 * @param g         the graphics object
	 * @param container the drawing container
	 * @param wire      the 0-based wire id (data is 1-based!)
	 * @param fc        the fill color
	 * @param lc        the line color
	 */
	public void drawXYWire(Graphics g, IContainer container, int wire, Color fc, Color lc,  E_DCProjection projection, double z, double theta) {
		if (numWires < 1) {
			return;
		}
		
		
		if (projection == E_DCProjection.MIDPOINT) {
			Point2D.Double mp = midpoints[wire];
			_wrect[wire].setFrame(mp.x-WIRERAD, mp.y-WIRERAD, 2*WIRERAD, 2*WIRERAD);
			WorldGraphicsUtilities.drawWorldOval(g, container, _wrect[wire], fc, lc);
		} else if (projection == E_DCProjection.FIXED_Z) {
			Point2D.Double zp = new Point2D.Double();
			double t = getWireXYatZ(wire, z, zp);
			
			if ((t < 0) || (t > 1)) {
				return;
			}
			_wrect[wire].setFrame(zp.x-WIRERAD, zp.y-WIRERAD, 2*WIRERAD, 2*WIRERAD);
			WorldGraphicsUtilities.drawWorldOval(g, container, _wrect[wire], fc, lc);
		}
		else {  //fixed theta
			Point2D.Double tp = new Point2D.Double();
			double t = getWireXYatTheta(wire, theta, tp);

			if ((t < 0) || (t > 1)) {
				return;
			}
			_wrect[wire].setFrame(tp.x - WIRERAD, tp.y - WIRERAD, 2 * WIRERAD, 2 * WIRERAD);
			WorldGraphicsUtilities.drawWorldOval(g, container, _wrect[wire], fc, lc);
			
		}
	}
	
	
	  private static Point3D findPointWithTheta(Point3D p0, Point3D p1, double targetTheta) {
	        double thetaEps = 1e-4; // Tolerance for theta
	        double epsilon = 1e-10; // Tolerance for the binary search termination

	        // Parametrize the line: p(t) = (1 - t) * p0 + t * p1
	        double dx = p1.x() - p0.x();
	        double dy = p1.y() - p0.y();
	        double dz = p1.z() - p0.z();

	        // Use a binary search to find t such that the interpolated point has theta == targetTheta
	        double tLow = 0.0;
	        double tHigh = 1.0;

	        while (true) {
	            double tMid = (tLow + tHigh) / 2.0;

	            // Interpolated point at tMid
	            double x = (1 - tMid) * p0.x() + tMid * p1.x();
	            double y = (1 - tMid) * p0.y() + tMid * p1.y();
	            double z = (1 - tMid) * p0.z() + tMid * p1.z();

	            // Compute theta for the interpolated point
	            double r = Math.sqrt(x * x + y * y + z * z);
	            if (r == 0) {
	            	return null;
	       //         throw new IllegalArgumentException("Segment passes through the origin; theta is undefined at some point.");
	            }
	            double theta = Math.acos(z / r);

	            if (Math.abs(theta - targetTheta) < thetaEps) {
	                // Found the desired point
	                return new Point3D(x, y, z);
	            }

	            if (theta < targetTheta) {
	                tLow = tMid; // Move to the upper half
	            } else {
	                tHigh = tMid; // Move to the lower half
	            }
	            
				if (Math.abs(tHigh - tLow) < epsilon) {
	                return new Point3D(x, y, z);
				}
	        }
	    }

}
