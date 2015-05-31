package org.boofcv.android;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.VerticalSeekBar;

import java.util.List;
import java.util.Random;

import boofcv.alg.feature.detect.edge.CannyEdge;
import boofcv.alg.feature.detect.edge.EdgeContour;
import boofcv.android.VisualizeImageData;
import boofcv.android.gui.VideoImageProcessing;
import boofcv.factory.feature.detect.edge.FactoryEdgeDetectors;
import boofcv.struct.image.ImageSInt16;
import boofcv.struct.image.ImageType;
import boofcv.struct.image.ImageUInt8;

/**
 * Displays results from the canny edge detector
 *
 * @author Peter Abeles
 */
public class CannyEdgeActivity extends DemoVideoDisplayActivity
	implements VerticalSeekBar.OnSeekBarChangeListener, CompoundButton.OnCheckedChangeListener
{
	private static final String TAG = "CannyEdgeActivity";

	Random rand = new Random(234);
	int []rgb = new int[0];

	float threshold;
	boolean colorize;

	public CannyEdgeActivity() {
		super(true);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		LayoutInflater inflater = getLayoutInflater();
		LinearLayout controls = (LinearLayout)inflater.inflate(R.layout.canny_controls,null);

		LinearLayout parent = getViewContent();
		parent.addView(controls);

		VerticalSeekBar seek = (VerticalSeekBar)controls.findViewById(R.id.slider_threshold);
		seek.setOnSeekBarChangeListener((SeekBar.OnSeekBarChangeListener) this);
		threshold = seek.getProgress()/100f;

		CheckBox toggle = (CheckBox)controls.findViewById(R.id.check_colorize);
		toggle.setOnCheckedChangeListener(this);
		colorize = toggle.isChecked();

//		Button testButton = (Button) findViewById(R.id.test_button);
//
//		testButton.setOnClickListener(new View.OnClickListener() {
//			public void onClick(View v) {
//
//				if (mCamera != null) {
//
//					Log.i(TAG, "Releasing cam");
//
//					stopAndRelease();
//
//				} else {
//
//					Log.i(TAG, "Starting cam again...");
//
//					onResume();
//				}
//
//			}
//		});

	}

	@Override
	protected void onResume() {
		super.onResume();
		startCanny();
	}

	private void startCanny() {
		setProcessing(new CannyProcessing());
	}

	@Override
	public void onCheckedChanged(CompoundButton compoundButton, boolean b) { colorize = b; }

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		threshold = progress/100.0f;
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {

	}

	protected class CannyProcessing extends VideoImageProcessing<ImageUInt8> {
		CannyEdge<ImageUInt8,ImageSInt16> canny;

		public CannyProcessing() {
			super(ImageType.single(ImageUInt8.class));
			this.canny = FactoryEdgeDetectors.canny(2, true, true, ImageUInt8.class, ImageSInt16.class);
		}

		@Override
		protected void process(ImageUInt8 input, Bitmap output, byte[] storage) {

			// make sure it doesn't get too low
			if( threshold <= 0.03f )
				threshold = 0.03f;

			canny.process(input,threshold/3.0f,threshold,null);
			List<EdgeContour> contours = canny.getContours();

			// random colors for each line
			if( colorize ) {
				if( rgb.length < contours.size() ) {
					rgb = new int[ contours.size() ];
					for( int i = 0; i < contours.size(); i++ ) {
						rgb[i] = rand.nextInt();
					}
				}

				VisualizeImageData.drawEdgeContours(contours,rgb,output,storage);
			} else {
				VisualizeImageData.drawEdgeContours(contours,0xFF0000,output,storage);
			}
		}
	}
}