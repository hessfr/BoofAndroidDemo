package org.boofcv.android;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;

import java.util.List;

import boofcv.alg.feature.shapes.ShapeFittingOps;
import boofcv.alg.filter.binary.BinaryImageOps;
import boofcv.alg.filter.binary.Contour;
import boofcv.alg.filter.binary.LinearContourLabelChang2004;
import boofcv.alg.filter.binary.ThresholdImageOps;
import boofcv.alg.filter.derivative.GImageDerivativeOps;
import boofcv.alg.misc.GPixelMath;
import boofcv.alg.misc.ImageStatistics;
import boofcv.android.VisualizeImageData;
import boofcv.android.gui.VideoImageProcessing;
import boofcv.struct.ConnectRule;
import boofcv.struct.PointIndex_I32;
import boofcv.struct.image.ImageSInt16;
import boofcv.struct.image.ImageSInt32;
import boofcv.struct.image.ImageType;
import boofcv.struct.image.ImageUInt8;
import georegression.struct.point.Point2D_I32;

/**
 * Fits different shapes to binary images
 *
 * @author Peter Abeles
 */
public class ShapeFittingActivity extends DemoVideoDisplayActivity
//		implements AdapterView.OnItemSelectedListener
{

//	Spinner spinnerView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		startShapeFitting();
	}

	@Override
	protected void onResume() {
		super.onResume();
		startShapeFitting();
	}

	private void startShapeFitting() {
		setProcessing(new PolygonProcessing() );
	}

	protected abstract class BaseProcessing extends VideoImageProcessing<ImageUInt8> {
		ImageSInt16 edge;
		ImageUInt8 binary;
		ImageUInt8 filtered1;
		ImageSInt32 contourOutput;
		Paint paint = new Paint();
		RectF r = new RectF();
		LinearContourLabelChang2004 findContours = new LinearContourLabelChang2004(ConnectRule.EIGHT);

		protected BaseProcessing() {
			super(ImageType.single(ImageUInt8.class));
		}

		@Override
		protected void declareImages( int width , int height ) {
			super.declareImages(width, height);

			edge = new ImageSInt16(width,height);
			binary = new ImageUInt8(width,height);
			filtered1 = new ImageUInt8(width,height);
			contourOutput = new ImageSInt32(width,height);

			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth(3f);
			paint.setColor(Color.RED);
		}

		@Override
		protected void process(ImageUInt8 input, Bitmap output, byte[] storage) {

			GImageDerivativeOps.laplace(input,edge);
			GPixelMath.abs(edge,edge);

			// use the mean value to threshold the image
			int mean = (int)ImageStatistics.mean(edge)*2;

			// create a binary image by thresholding
			ThresholdImageOps.threshold(edge, binary, mean, false);

			// reduce noise with some filtering
			BinaryImageOps.removePointNoise(binary, filtered1);

			// draw binary image for output
			VisualizeImageData.binaryToBitmap(filtered1, output, storage);

			// draw the ellipses
			findContours.process(filtered1,contourOutput);
			List<Contour> contours = findContours.getContours().toList();

			Canvas canvas = new Canvas(output);

			for( Contour contour : contours ) {
				List<Point2D_I32> points = contour.external;
				if( points.size() < 20 )
					continue;

				fitShape(points,canvas);
			}
		}

		protected abstract void fitShape( List<Point2D_I32> contour , Canvas canvas );
	}

	/*
	protected class EllipseProcessing extends BaseProcessing {

		FitData<EllipseRotated_F64> ellipse = new FitData<EllipseRotated_F64>(new EllipseRotated_F64());

		@Override
		protected void fitShape(List<Point2D_I32> contour, Canvas canvas) {
			ShapeFittingOps.fitEllipse_I32(contour, 0, false, ellipse);

			float phi = (float)UtilAngle.radianToDegree(ellipse.shape.phi);
			float cx =  (float)ellipse.shape.center.x;
			float cy =  (float)ellipse.shape.center.y;
			float w = (float)ellipse.shape.a;
			float h = (float)ellipse.shape.b;

			//  really skinny ones are probably just a line and not what the user wants
			if( w <= 2 || h <= 2 )
				return;

			canvas.rotate(phi, cx, cy);
			r.set(cx-w,cy-h,cx+w+1,cy+h+1);
			canvas.drawOval(r,paint);
			canvas.rotate(-phi, cx, cy);
		}
	}
	*/

	protected class PolygonProcessing extends BaseProcessing {

		@Override
		protected void fitShape(List<Point2D_I32> contour, Canvas canvas) {
			List<PointIndex_I32> poly = ShapeFittingOps.fitPolygon(contour, true, 4, 0.3f, 0);

			for( int i = 1; i < poly.size(); i++ ) {
				PointIndex_I32 a = poly.get(i-1);
				PointIndex_I32 b = poly.get(i);

				canvas.drawLine(a.x,a.y,b.x,b.y,paint);
			}

			PointIndex_I32 a = poly.get(poly.size()-1);
			PointIndex_I32 b = poly.get(0);

			canvas.drawLine(a.x,a.y,b.x,b.y,paint);
		}
	}
}