package com.example.photoeditor;

import static com.example.photoeditor.editoractivity.EditorSaveConstants.RESTORE_SAVED_BITMAP;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.photoeditor.editoractivity.BrightnessActivity;
import com.example.photoeditor.editoractivity.CropActivity;
import com.example.photoeditor.editoractivity.FilterActivity;
import com.example.photoeditor.editoractivity.RotateActivity;
import com.example.photoeditor.graphics.ImageProcessor;
import com.example.photoeditor.utils.BitmapScalingUtil;
import com.example.photoeditor.utils.ImageScannerAdapter;
import com.example.photoeditor.utils.SaveToStorageUtil;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

public class EditorActivity extends Activity implements OnClickListener {

	private static final int EDITOR_FUNCTION = 1;

	private ImageView imageView;

	// Top bar buttons
	private ImageButton brightnessButton;
	private ImageButton cropButton;
	private ImageButton rotateButton;
	private ImageButton filtersButton;

	// Bottom bar buttons
	private ImageButton backButton;
	private ImageButton shareButton;
	private ImageButton saveButton;

	private String savedImagePath;
	private AdView mAdView;// google adview
	protected InterstitialAd interstitial;
	protected boolean AdLoaded = false;
	private int adsCounter = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.editor);
		initComponents();
		initImageView();
		String adsID = getResources().getString(R.string.admob_banner);
		if (adsID != null && !adsID.isEmpty()) {
			loadAds();
		}
	}

	private void initComponents() {
		brightnessButton = (ImageButton) findViewById(R.id.brightness_button);
		brightnessButton.setOnClickListener(this);
		cropButton = (ImageButton) findViewById(R.id.crop_button);
		cropButton.setOnClickListener(this);
		rotateButton = (ImageButton) findViewById(R.id.rotate_button);
		rotateButton.setOnClickListener(this);
		filtersButton = (ImageButton) findViewById(R.id.filters_button);
		filtersButton.setOnClickListener(this);
		backButton = (ImageButton) findViewById(R.id.back_button);
		backButton.setOnClickListener(this);
		shareButton = (ImageButton) findViewById(R.id.share_button);
		shareButton.setOnClickListener(this);
		saveButton = (ImageButton) findViewById(R.id.save_button);
		saveButton.setOnClickListener(this);
		mAdView = (AdView) findViewById(R.id.adView);
	}

	private void loadAds() {

		AdRequest adRequest = new AdRequest.Builder().build();
		mAdView.loadAd(adRequest);
		interstitial = new InterstitialAd(this);
		AdRequest adRequest2 = new AdRequest.Builder().build(); //
		interstitial.setAdUnitId(getResources().getString(R.string.admob_interstitial));
		interstitial.setAdListener(new AdListener() {

			@Override
			public void onAdLoaded() {
				AdLoaded = true;
			}

			@Override
			public void onAdClosed() {
				super.onAdClosed();
			}

			@Override
			public void onAdFailedToLoad(int errorCode) {
				AdLoaded = false;
			}
		});

		interstitial.loadAd(adRequest2);

	}

	public void displayInterstitial() {
		if (adsCounter == 3) {
			if (interstitial.isLoaded()) {

				interstitial.show();
			}
			adsCounter = 0;
		} else {
			adsCounter += 1;
		}
	}

	@SuppressWarnings("deprecation")
	private void initImageView() {
		String imageUri = getIntent().getStringExtra(getString(R.string.image_uri_flag));
		Log.i("Photo Editor", "Image URI = " + imageUri);
		imageView = (ImageView) findViewById(R.id.image_view);

		final Object data = getLastNonConfigurationInstance();
		if (data == null) {
			openBitmap(imageUri);
		} else {
			restoreBitmap();
		}
	}

	private void restoreBitmap() {
		Log.i("Photo Editor", "Restore bitmap");
		Bitmap b = ImageProcessor.getInstance().getBitmap();
		if (b != null) {
			imageView.setImageBitmap(b);
		}
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		Bundle saveObject = new Bundle();
		saveObject.putInt("Bitmap", RESTORE_SAVED_BITMAP);
		return saveObject;
	}

	private void openBitmap(String imageUri) {
		Log.i("Photo Editor", "Open Bitmap");
		Bitmap b;
		try {
			b = BitmapScalingUtil.bitmapFromUri(this, Uri.parse(imageUri));
			if (b != null) {
				Log.i("Photo Editor", "Opened Bitmap Size: " + b.getWidth() + " " + b.getHeight());
			}
			ImageProcessor.getInstance().setBitmap(b);
			imageView.setImageBitmap(b);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.brightness_button:
			brightnessButtonClicked();
			break;
		case R.id.crop_button:
			cropButtonClicked();
			break;
		case R.id.rotate_button:
			rotateButtonClicked();
			break;
		case R.id.filters_button:
			filtersButtonClicked();
			break;
		case R.id.back_button:
			backButtonClicked();
			break;
		case R.id.share_button:
			sharedButtonClicked();
			break;
		case R.id.save_button:
			saveButtonClicked();
			break;
		default:
			break;
		}
	}

	private void backButtonClicked() {
		finish();
	}

	private boolean imageIsAlreadySaved() {
		return savedImagePath != null && !savedImagePath.equals("");
	}

	private void sharedButtonClicked() {
		if (!imageIsAlreadySaved() || ImageProcessor.getInstance().isModified()) {
			saveImage();
		}
		Intent share = new Intent(Intent.ACTION_SEND);
		share.setType("image/*");
		share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(savedImagePath)));
		startActivity(Intent.createChooser(share, "Share via"));
	}

	private void saveButtonClicked() {
		saveImage();
		Toast.makeText(this, R.string.photo_saved_info, Toast.LENGTH_LONG).show();
	}

	private void saveImage() {
		savedImagePath = SaveToStorageUtil.save(ImageProcessor.getInstance().getBitmap(), this);
		ImageScannerAdapter adapter = new ImageScannerAdapter(this);
		adapter.scanImage(savedImagePath);
		ImageProcessor.getInstance().resetModificationFlag();
	}

	private void cropButtonClicked() {
		runEditorActivity(CropActivity.class);
	}

	private void brightnessButtonClicked() {
		runEditorActivity(BrightnessActivity.class);
	}

	private void filtersButtonClicked() {
		runEditorActivity(FilterActivity.class);
	}

	private void rotateButtonClicked() {
		runEditorActivity(RotateActivity.class);
	}

	private void runEditorActivity(Class<?> activityClass) {
		String adsID = getResources().getString(R.string.admob_banner);
		if (adsID != null && !adsID.isEmpty()) {
			displayInterstitial();
		}
		Intent i = new Intent(this, activityClass);
		startActivityForResult(i, EDITOR_FUNCTION);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (requestCode) {
		case EDITOR_FUNCTION:
			if (resultCode == RESULT_OK) {
				imageView.setImageBitmap(ImageProcessor.getInstance().getBitmap());
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void onPause() {
		if (mAdView != null) {
			mAdView.pause();
		}
		super.onPause();
	}

	/** Called when returning to the activity */
	@Override
	public void onResume() {
		super.onResume();
		AdLoaded = false;
		if (mAdView != null) {
			mAdView.resume();
		}
	}

	/** Called before the activity is destroyed */
	@Override
	public void onDestroy() {
		if (mAdView != null) {
			mAdView.destroy();
		}
		super.onDestroy();
	}

}
