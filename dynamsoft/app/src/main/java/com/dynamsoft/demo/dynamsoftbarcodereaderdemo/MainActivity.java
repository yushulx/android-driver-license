package com.dynamsoft.demo.dynamsoftbarcodereaderdemo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import com.dynamsoft.barcode.BarcodeReader;
import com.dynamsoft.barcode.EnumBarcodeFormat;
import com.dynamsoft.barcode.EnumBarcodeFormat_2;
import com.dynamsoft.barcode.EnumConflictMode;
import com.dynamsoft.barcode.EnumImagePixelFormat;
import com.dynamsoft.barcode.EnumIntermediateResultType;
import com.dynamsoft.barcode.PublicRuntimeSettings;
import com.dynamsoft.barcode.TextResult;
import com.pierfrancescosoffritti.slidingdrawer.SlidingDrawer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.fotoapparat.Fotoapparat;
import io.fotoapparat.configuration.UpdateConfiguration;
import io.fotoapparat.parameter.Resolution;
import io.fotoapparat.parameter.ScaleType;
import io.fotoapparat.parameter.camera.CameraParameters;
import io.fotoapparat.preview.Frame;
import io.fotoapparat.preview.FrameProcessor;
import io.fotoapparat.view.CameraView;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import pub.devrel.easypermissions.EasyPermissions;

import static io.fotoapparat.selector.FlashSelectorsKt.off;
import static io.fotoapparat.selector.FlashSelectorsKt.torch;
import static io.fotoapparat.selector.LensPositionSelectorsKt.back;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
	CameraView cameraView;
	@BindView(R.id.tv_flash)
	TextView mFlash;

	private TextView tvContinue;
	private RelativeLayout viewResult;
	private SlidingDrawer slidingDrawer;
	private ListView listView;
	private RelativeLayout dragView;
	private TextView tvResultCount;
	private TextView tvDrag;
	private ImageView ivPull;
	private ImageView ivPhoto;
	private RelativeLayout viewPhoto;
	private HUDCanvasView canvasView;
	private int numberOfCodes;
	private BarcodeReader reader;
	private TextResult[] result;
	private volatile boolean detectStart = false;
	private int barcodeType = 0;
	private DBRCache mCache;
	private boolean isFlashOn = false;
	private boolean isCameraOpen = false;
	private TextView m_tFlasht;
	private TextView tvBarcodeNumber;
	private Fotoapparat fotoapparat;
	private boolean hasCameraPermission;
	private List<Map<String, String>> recentCodeList = new ArrayList<>();
	private HandlerThread decodeThread;
	private Bitmap bitmap;
	private Handler decodeHandler;
	private static final int REQUEST_EXTERNAL_STORAGE = 1;
	private static String[] PERMISSIONS_STORAGE = {
			"android.permission.READ_EXTERNAL_STORAGE",
			"android.permission.WRITE_EXTERNAL_STORAGE",
			"android.permission.CAMERA"};
	private boolean isFinished = false;
	private static final int MSG_PAINT = 1;

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
				case 0:
					obtainPreviewScale();
					break;
				case MSG_PAINT:
					PointResult pointResult = (PointResult) msg.obj;
					if (pointResult.textResults != null && !isFinished) {
						for (TextResult result : pointResult.textResults) {
							if (DBRDriverLicenseUtil.ifDriverLicense(result.barcodeText)) {
								isFinished = true;
								handler.removeMessages(0x01);

								HashMap<String, String> resultMaps = DBRDriverLicenseUtil.readUSDriverLicense(result.barcodeText);
								Intent intent = new Intent(MainActivity.this, ResultActivity.class);
								DriverLicense driverLicense = new DriverLicense();
								driverLicense.documentType = "DL";
								driverLicense.firstName = resultMaps.get(DBRDriverLicenseUtil.FIRST_NAME);
								driverLicense.middleName = resultMaps.get(DBRDriverLicenseUtil.MIDDLE_NAME);
								driverLicense.lastName = resultMaps.get(DBRDriverLicenseUtil.LAST_NAME);
								driverLicense.gender = resultMaps.get(DBRDriverLicenseUtil.GENDER);
								driverLicense.addressStreet = resultMaps.get(DBRDriverLicenseUtil.STREET);
								driverLicense.addressCity = resultMaps.get(DBRDriverLicenseUtil.CITY);
								driverLicense.addressState = resultMaps.get(DBRDriverLicenseUtil.STATE);
								driverLicense.addressZip = resultMaps.get(DBRDriverLicenseUtil.ZIP);
								driverLicense.licenseNumber = resultMaps.get(DBRDriverLicenseUtil.LICENSE_NUMBER);
								driverLicense.issueDate = resultMaps.get(DBRDriverLicenseUtil.ISSUE_DATE);
								driverLicense.expiryDate = resultMaps.get(DBRDriverLicenseUtil.EXPIRY_DATE);
								driverLicense.birthDate = resultMaps.get(DBRDriverLicenseUtil.BIRTH_DATE);
								driverLicense.issuingCountry = resultMaps.get(DBRDriverLicenseUtil.ISSUING_COUNTRY);

								intent.putExtra("DriverLicense", driverLicense);
								startActivity(intent);
							}
						}

						drawDocumentBox(frameUtil.handlePoints(pointResult.textResults, previewScale, hgt, wid), frameUtil.handlePoints(pointResult.localizationResults, previewScale, hgt, wid), pointResult.textResults);
					} else {
						canvasView.clear();
					}
					if (numberOfCodes == 0) {
						if (pointResult.textResults != null) {
							tvBarcodeNumber.setText(pointResult.textResults.length + " barcode(s) scanned");
						} else {
							tvBarcodeNumber.setText("0 barcode(s) scanned");
						}
					} else {
						if (pointResult.textResults != null) {
							tvBarcodeNumber.setText(pointResult.textResults.length + "/" + numberOfCodes + " barcode(s) scanned");
						} else {
							tvBarcodeNumber.setText("0" + "/" + numberOfCodes + " barcode(s) scanned");
						}
					}
					break;
				case 0x02:
					bitmap = msg.getData().getParcelable("bitmap");
					ivPhoto.setImageBitmap(bitmap);
					TextResult[] textResults = (TextResult[]) msg.obj;
					recentCodeList.clear();
					if (textResults != null && textResults.length > 0) {
						for (int i = 0; i < textResults.length; i++) {
							Map<String, String> item = new HashMap<>();
							item.put("Index", i + 1 + "");
							item.put("Format", textResults[i].barcodeFormatString);
							item.put("Text", textResults[i].barcodeText);
							recentCodeList.add(item);
						}
					}
					SimpleAdapter simpleAdapter = new SimpleAdapter(MainActivity.this, recentCodeList, R.layout.item_listview_detail_code_list, new String[]{"Index", "Format", "Text"}, new int[]{R.id.tv_index, R.id.tv_code_format_content, R.id.tv_code_text_content});
					tvResultCount.setText("Total: " + recentCodeList.size());
					canvasView.clear();
					listView.setAdapter(simpleAdapter);
					viewResult.setVisibility(View.VISIBLE);
					break;
				default:
					break;
			}
		}
	};
	private void drawDocumentBox(ArrayList<Point[]> resultPoint, ArrayList<Point[]> localPoint, TextResult[] results) {
		canvasView.clear();
		canvasView.setBoundaryPoints(resultPoint, localPoint, results);
		canvasView.invalidate();
	}

	private float previewScale;
	private Resolution fotPreviewSize = null;
	private FrameUtil frameUtil;
	private void obtainPreviewScale() {
		if (canvasView.getWidth() == 0 || canvasView.getHeight() == 0) {
			return;
		}
		fotoapparat.getCurrentParameters().whenAvailable(new Function1<CameraParameters, Unit>() {
			@Override
			public Unit invoke(CameraParameters cameraParameters) {
				fotPreviewSize = cameraParameters.getPreviewResolution();
				previewScale = frameUtil.calculatePreviewScale(fotPreviewSize, canvasView.getWidth(), canvasView.getHeight());
				return Unit.INSTANCE;
			}
		});
	}

	void initBarcodeReader() throws Exception {
		PublicRuntimeSettings runtimeSettings = reader.getRuntimeSettings();
		runtimeSettings.barcodeFormatIds = EnumBarcodeFormat.BF_ALL;
		runtimeSettings.barcodeFormatIds_2 = EnumBarcodeFormat_2.BF2_NULL;
		runtimeSettings.timeout = 3000;
		//runtimeSettings.intermediateResultSavingMode = EnumIntermediateResultSavingMode.IRSM_FILESYSTEM;
		runtimeSettings.intermediateResultTypes = EnumIntermediateResultType.IRT_TYPED_BARCODE_ZONE;
		if(reader != null) {
			//Best Coverage settings
		    //reader.initRuntimeSettingsWithString("{\"ImageParameter\":{\"Name\":\"BestCoverage\",\"DeblurLevel\":9,\"ExpectedBarcodesCount\":512,\"ScaleDownThreshold\":100000,\"LocalizationModes\":[{\"Mode\":\"LM_CONNECTED_BLOCKS\"},{\"Mode\":\"LM_SCAN_DIRECTLY\"},{\"Mode\":\"LM_STATISTICS\"},{\"Mode\":\"LM_LINES\"},{\"Mode\":\"LM_STATISTICS_MARKS\"}],\"GrayscaleTransformationModes\":[{\"Mode\":\"GTM_ORIGINAL\"},{\"Mode\":\"GTM_INVERTED\"}]}}", EnumConflictMode.CM_OVERWRITE);
		    //Best Speed settings
		    //reader.initRuntimeSettingsWithString("{\"ImageParameter\":{\"Name\":\"BestSpeed\",\"DeblurLevel\":3,\"ExpectedBarcodesCount\":512,\"LocalizationModes\":[{\"Mode\":\"LM_SCAN_DIRECTLY\"}],\"TextFilterModes\":[{\"MinImageDimension\":262144,\"Mode\":\"TFM_GENERAL_CONTOUR\"}]}}", EnumConflictMode.CM_OVERWRITE);
		    //Balance settings
		    reader.initRuntimeSettingsWithString("{\"ImageParameter\":{\"Name\":\"Balance\",\"DeblurLevel\":5,\"ExpectedBarcodesCount\":512,\"LocalizationModes\":[{\"Mode\":\"LM_CONNECTED_BLOCKS\"},{\"Mode\":\"LM_SCAN_DIRECTLY\"}]}}", EnumConflictMode.CM_OVERWRITE);
            reader.updateRuntimeSettings(runtimeSettings);
        }
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ButterKnife.bind(this);
		frameUtil = new FrameUtil();
		try {
			reader = new BarcodeReader("t0068NQAAAB3OUjxXnjrCN4RaBZBygOddAUJk5Z/JQOi0xEKZAdOO9ZKBwyKzdju34aiiAv9Oos0jH/r5zpsWUuPU66plu5g=");
			//You can get trial license from "https://www.dynamsoft.com/CustomerPortal/Portal/Triallicense.aspx"
			initBarcodeReader();

		} catch (Exception e) {
			e.printStackTrace();
		}


		m_tFlasht = findViewById(R.id.tv_flash);
		cameraView = findViewById(R.id.foto_camera_view);
		canvasView = findViewById(R.id.hud_view);
		tvContinue = findViewById(R.id.tv_continue);
		viewResult = findViewById(R.id.view_result_img);
		slidingDrawer = findViewById(R.id.sliding_drawer);
		listView = findViewById(R.id.barcode_list);
		dragView = findViewById(R.id.drag_view);
		tvResultCount = findViewById(R.id.result);
		tvDrag = findViewById(R.id.drag_text);
		ivPull = findViewById(R.id.iv_pull);
		ivPhoto = findViewById(R.id.iv_result);
		viewPhoto = findViewById(R.id.view_photo);
		tvBarcodeNumber = findViewById(R.id.tv_barcode_number);
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		setUpCamera();
		mCache = DBRCache.get(this);
		mCache.put("linear", "1");
		mCache.put("qrcode", "1");
		mCache.put("pdf417", "1");
		mCache.put("matrix", "1");
		mCache.put("aztec", "0");
		mCache.put("databar", "0");
		mCache.put("patchcode", "0");
		mCache.put("maxicode", "0");
		mCache.put("microqr", "0");
		mCache.put("micropdf417", "0");
		mCache.put("gs1compositecode", "0");
		mCache.put("numberOfCodes", "0");
        mCache.put("postalcode", "0");
        mCache.put("dotcode", "0");
		//if (mCache.getAsString("numberOfCodes") != null) {
		//	numberOfCodes = Integer.parseInt(mCache.getAsString("numberOfCodes"));
		//} else {
			numberOfCodes = 0;
		//}
		slidingDrawer.setDragView(dragView);
		slidingDrawer.addSlideListener(new SlidingDrawer.OnSlideListener() {
			@Override
			public void onSlide(SlidingDrawer slidingDrawer, float currentSlide) {
				if (slidingDrawer.getState() == SlidingDrawer.COLLAPSED) {
					viewPhoto.setVisibility(View.VISIBLE);
					viewPhoto.bringToFront();
					ivPull.setImageResource(R.drawable.arrow_up);
					tvDrag.setText("More results");
				} else if (slidingDrawer.getState() == SlidingDrawer.EXPANDED) {
					viewPhoto.setVisibility(View.GONE);
					ivPull.setImageResource(R.drawable.arrow_down);
					tvDrag.setText("Scroll down");
					slidingDrawer.bringToFront();
				} else if (slidingDrawer.getState() == SlidingDrawer.SLIDING) {
					slidingDrawer.bringToFront();
					viewPhoto.setVisibility(View.VISIBLE);
				}
			}
		});
		ivPull.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (slidingDrawer.getState() == SlidingDrawer.EXPANDED) {
					slidingDrawer.setState(SlidingDrawer.COLLAPSED);
				} else if (slidingDrawer.getState() == SlidingDrawer.COLLAPSED) {
					slidingDrawer.setState(SlidingDrawer.EXPANDED);
				}
			}
		});
		tvDrag.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (slidingDrawer.getState() == SlidingDrawer.EXPANDED) {
					slidingDrawer.setState(SlidingDrawer.COLLAPSED);
				} else if (slidingDrawer.getState() == SlidingDrawer.COLLAPSED) {
					slidingDrawer.setState(SlidingDrawer.EXPANDED);
				}
			}
		});
		tvContinue.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				viewResult.setVisibility(View.GONE);
				detectStart = false;
			}
		});
	}

	private void startDecodeThread() {
		decodeThread = new HandlerThread("DecodeThread");
		decodeThread.start();
		decodeHandler = new Handler(decodeThread.getLooper()) {
			@Override
			public void handleMessage(Message msg) {
				try {
					Frame frame = (Frame) msg.obj;
					result = result = reader.decodeBuffer(frame.getImage(), frame.getSize().width, frame.getSize().height, frame.getSize().width, EnumImagePixelFormat.IPF_NV21, "");
					PointResult pointResult = new PointResult();
					pointResult.textResults = result;
					Message message = handler.obtainMessage();
					message.obj = pointResult;
					message.what = MSG_PAINT;
					handler.sendMessage(message);
				} catch (Exception e) {
					e.printStackTrace();
				}
				finally {
					detectStart = false;
				}

			}
		};
	}
	private void askForPermissions() {
		String[] perms = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
		if (!EasyPermissions.hasPermissions(this, perms)) {
			hasCameraPermission = false;
			//EasyPermissions.requestPermissions(this, "We need camera permission to provide service.", 0, perms);
			ActivityCompat.requestPermissions(this, perms, 1);
		} else {
			hasCameraPermission = true;
			cameraView.setVisibility(View.VISIBLE);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		requestPermissions();
		askForPermissions();
		if (hasCameraPermission) {
			fotoapparat.start();
		}
		isCameraOpen = true;
	}
	@Override
	public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
		hasCameraPermission = true;
		fotoapparat.start();
	}
	@Override
	public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

	}
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			Intent intent = new Intent(MainActivity.this, SettingActivity.class);
			intent.putExtra("type", barcodeType);
			startActivityForResult(intent, 0);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@SuppressLint("NewApi")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		try {
			int nBarcodeFormat =0;
            int nBarcodeFormat_2 = 0;
			if (mCache.getAsString("linear").equals("1")) {
				nBarcodeFormat = nBarcodeFormat|EnumBarcodeFormat.BF_ONED;
			}
			if (mCache.getAsString("qrcode").equals("1")) {
				nBarcodeFormat = nBarcodeFormat|EnumBarcodeFormat.BF_QR_CODE;
			}
			if (mCache.getAsString("pdf417").equals("1")) {
				nBarcodeFormat = nBarcodeFormat|EnumBarcodeFormat.BF_PDF417;
			}
			if (mCache.getAsString("matrix").equals("1")) {
				nBarcodeFormat = nBarcodeFormat|EnumBarcodeFormat.BF_DATAMATRIX;
			}
			if (mCache.getAsString("aztec").equals("1")) {
				nBarcodeFormat = nBarcodeFormat|EnumBarcodeFormat.BF_AZTEC;
			}
			if (mCache.getAsString("databar").equals("1")) {
				nBarcodeFormat = nBarcodeFormat|EnumBarcodeFormat.BF_GS1_DATABAR;
			}
			if (mCache.getAsString("patchcode").equals("1")) {
				nBarcodeFormat = nBarcodeFormat|EnumBarcodeFormat.BF_PATCHCODE;
			}
			if (mCache.getAsString("maxicode").equals("1")) {
				nBarcodeFormat = nBarcodeFormat|EnumBarcodeFormat.BF_MAXICODE;
			}
			if (mCache.getAsString("microqr").equals("1")) {
				nBarcodeFormat = nBarcodeFormat|EnumBarcodeFormat.BF_MICRO_QR;
			}
			if (mCache.getAsString("micropdf417").equals("1")) {
				nBarcodeFormat = nBarcodeFormat|EnumBarcodeFormat.BF_MICRO_PDF417;
			}
			if (mCache.getAsString("gs1compositecode").equals("1")) {
				nBarcodeFormat = nBarcodeFormat|EnumBarcodeFormat.BF_GS1_COMPOSITE;
			}
            if (mCache.getAsString("postalcode").equals("1")) {
                nBarcodeFormat_2 = nBarcodeFormat_2 | EnumBarcodeFormat_2.BF2_POSTALCODE;
            }
            if (mCache.getAsString("dotcode").equals("1")) {
                nBarcodeFormat_2 = nBarcodeFormat_2 | EnumBarcodeFormat_2.BF2_DOTCODE;
            }
			if (mCache.getAsString("numberOfCodes") != null) {
				numberOfCodes = Integer.parseInt(mCache.getAsString("numberOfCodes"));
			} else {
				numberOfCodes = 0;
				mCache.put("numberOfCodes", "0");
			}

			PublicRuntimeSettings runtimeSettings = reader.getRuntimeSettings();
			runtimeSettings.barcodeFormatIds = nBarcodeFormat;
            runtimeSettings.barcodeFormatIds_2 = nBarcodeFormat_2;
			reader.updateRuntimeSettings(runtimeSettings);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		isFinished = false;
		startDecodeThread();
	}

	@Override
	protected void onStop() {
		super.onStop();
		isFinished = true;
		fotoapparat.stop();
		decodeThread.quitSafely();
		isCameraOpen = false;
	}


	@OnClick(R.id.tv_flash)
	public void onFlashClick() {
		if (isFlashOn) {
			isFlashOn = false;
			fotoapparat.updateConfiguration(UpdateConfiguration.builder().flash(off()).build());
			m_tFlasht.setText("Flash ON");
		} else {
			isFlashOn = true;
			fotoapparat.updateConfiguration(UpdateConfiguration.builder().flash(torch()).build());
			m_tFlasht.setText("Flash OFF");
		}
	}
	private void requestPermissions(){
		if (Build.VERSION.SDK_INT>22){
			try {
				if (ContextCompat.checkSelfPermission(MainActivity.this,"android.permission.WRITE_EXTERNAL_STORAGE")!= PackageManager.PERMISSION_GRANTED) {
					ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			// do nothing
		}
	}
	private void setUpCamera() {
		fotoapparat = Fotoapparat
				.with(this)
				.into(cameraView)
				.previewScaleType(ScaleType.CenterCrop)
				.lensPosition(back())
				.photoResolution(new Function1<Iterable<Resolution>, Resolution>() {
					@Override
					public Resolution invoke(Iterable<Resolution> resolutions) {
						Iterator<Resolution> mResulutions = resolutions.iterator();
						ArrayList<Resolution> resolutionArrayList = new ArrayList<>();
						while (mResulutions.hasNext()) {
							resolutionArrayList.add(mResulutions.next());
						}
						for (int i = 0; i < resolutionArrayList.size() - 1; i++) {
							for (int j = i + 1; j < resolutionArrayList.size(); j++) {
								if (resolutionArrayList.get(i).width * resolutionArrayList.get(i).height < resolutionArrayList.get(j).width * resolutionArrayList.get(j).height) {
									Resolution resolution = new Resolution(resolutionArrayList.get(i).width, resolutionArrayList.get(i).height);
									Resolution resolution1 = new Resolution(resolutionArrayList.get(j).width, resolutionArrayList.get(j).height);
									resolutionArrayList.set(i, resolution1);
									resolutionArrayList.set(j, resolution);
								}
							}
						}
						for (Resolution resolution : resolutionArrayList) {
							if (resolution.width * resolution.height <= 1920 * 1080) {

								//Log.e("m1Resolution", resolution.width + " " + resolution.height);
								return resolution;
							}
						}
						return resolutionArrayList.get(0);
					}
				})
				.frameProcessor(new CodeFrameProcesser())
				.build();

	}
	int hgt,wid;
	class CodeFrameProcesser implements FrameProcessor {
		@Override
		public void process(@NonNull Frame frame) {
			//isDetected = false;
			if (fotPreviewSize == null) {
				handler.sendEmptyMessage(0);
			}
			if (!detectStart && isCameraOpen) {
				detectStart = true;
				wid = frame.getSize().width;
				hgt = frame.getSize().height;
				Message message = decodeHandler.obtainMessage();
				message.obj = frame;
				decodeHandler.sendMessage(message);
			}
		}
	}

}

