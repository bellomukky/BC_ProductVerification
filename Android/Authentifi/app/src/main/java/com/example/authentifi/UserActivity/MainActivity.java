package com.example.authentifi.UserActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.authentifi.Deliveries.DeliveriesFragment;
import com.example.authentifi.MyProfileFragment;
import com.example.authentifi.ProductPage;
import com.example.authentifi.R;
import com.example.authentifi.RetailerActivity.MainRetailerActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.zxing.client.android.Intents;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class MainActivity extends AppCompatActivity {

	FragmentManager fragmentManager;
	public static String email, address;

	public static boolean isLogistic;

	private static final int REQUEST_CAMERA_PERMISSION = 200;

	TextView title;
	ImageButton profileButton, closeButton;
	FloatingActionButton scanButton;

	public MainActivity() {
		super(R.layout.activity_main);
		MainRetailerActivity.email = null;
	}


	private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
			result -> {
				if(result.getContents() == null) {
					Intent originalIntent = result.getOriginalIntent();
					if (originalIntent == null) {
						Log.d("MainActivity", "Cancelled scan");
						Toast.makeText(MainActivity.this, "Cancelled", Toast.LENGTH_LONG).show();
					} else if(originalIntent.hasExtra(Intents.Scan.MISSING_CAMERA_PERMISSION)) {
						Log.d("MainActivity", "Cancelled scan due to missing camera permission");
						Toast.makeText(MainActivity.this, "Cancelled due to missing camera permission", Toast.LENGTH_LONG).show();
					}
				} else {
					Log.d("MainActivity", "Scanned");
					Log.i("QRcode", result.getContents());
					// handle scan result
					Intent intent =  new Intent(MainActivity.this, ProductPage.class);
					Bundle bundle = new Bundle();
					bundle.putString("code", result.getContents());

					bundle.putBoolean("isRetailer", false);
					bundle.putBoolean("isLogistic", true);

					intent.putExtras(bundle);
					startActivity(intent);
				}
			});


	private void askForPermission(String permission, Integer requestCode) {
		if (ContextCompat.checkSelfPermission(MainActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {

			// Should we show an explanation?
			if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permission)) {

				//This is called if user has denied the permission before
				//In this case I am just asking the permission again
				ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);

			}
			else {

				ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);
			}
		} /*else {
            Toast.makeText(this, "" + permission + " is already granted.", Toast.LENGTH_SHORT).show();
        }*/
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		askForPermission(Manifest.permission.CAMERA, REQUEST_CAMERA_PERMISSION);

	email = getIntent().getStringExtra("email");
	address = getIntent().getStringExtra("address");

	isLogistic =  true;

		title = findViewById(R.id.title_head);
		title.setText(R.string.deliveries_head);

		final int fragmentContainer = R.id.fragment_container;
		fragmentManager = getSupportFragmentManager();
		Bundle bundle = new Bundle();

		bundle.putBoolean("isRetailer", false);
		bundle.putBoolean("isLogistic", true);

		final Fragment profileFragment = new MyProfileFragment();
		final Fragment deliverisFragment = new DeliveriesFragment();
		deliverisFragment.setArguments(bundle);

		fragmentManager.beginTransaction().add(fragmentContainer, deliverisFragment).commit();

		profileButton = findViewById(R.id.profile_button);
		profileButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				profileButton.setVisibility(View.INVISIBLE);

				fragmentManager.beginTransaction()
						.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
						.replace(fragmentContainer, profileFragment)
						.commit();

				title.setText(R.string.profile_head);
				closeButton.setVisibility(View.VISIBLE);
			}
		});

		closeButton = findViewById(R.id.close_button);
		closeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				closeButton.setVisibility(View.INVISIBLE);

				fragmentManager.beginTransaction()
						.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
						.replace(fragmentContainer, deliverisFragment)
						.commit();

				title.setText(R.string.deliveries_head);
				profileButton.setVisibility(View.VISIBLE);
			}
		});

		scanButton = (FloatingActionButton)findViewById(R.id.scan_button);
		scanButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//	Intent intent = new Intent(MainActivity.this, Viewfind.class);
				//	startActivity(intent);
				barcodeLauncher.launch(new ScanOptions());
			}
		});
	}


	/*public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		IntentResult scanResult = IntentIntegrator
				.parseActivityResult(requestCode, resultCode, intent);
		Log.d("scanResult", scanResult.toString());
		if (scanResult != null) {
			Log.i("QRcode", scanResult.getContents());
			// handle scan result
			intent = new Intent(MainActivity.this, ProductPage.class);
			Bundle bundle = new Bundle();
			bundle.putString("code", scanResult.getContents());

			if(false)
				bundle.putBoolean("isOwner", true);
			else
				bundle.putBoolean("isOwner", false);

			intent.putExtras(bundle);
			startActivity(intent);
		}else{
			// else continue with any other code you need in the method
			super.onActivityResult(requestCode, resultCode, intent);
		}


	}*/


	void pushlogs(Fragment fragment) {

		Log.d("yolo added", Boolean.toString(fragment.isAdded()));
		Log.d("yolo stsvd", Boolean.toString(fragment.isStateSaved()));
		Log.d("yolo detac", Boolean.toString(fragment.isDetached()));
		Log.d("yolo hidde", Boolean.toString(fragment.isHidden()));
		Log.d("yolo visib", Boolean.toString(fragment.isVisible()));
		Log.d("yolo resum", Boolean.toString(fragment.isResumed()));
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
			return true;
		}

		return super.onOptionsItemSelected(item);
	}





}


/** TODO:
Android App:

 Enviroment 1(Customer):

 Login(username, password)

 Signup(email,password,name,photo(*),phone)

 MyAssets(List of assets, when you click on the assets it takes you to ProductPage)

 ProductPage(Brand name, model no,image,desc,status & Manufacturer(location,timestamp) & ++PreviousOwner(name,timestamp))
 (Button for SELL -> which goes to SellActivity with the code)
 (Button for Report Stolen -> and response)

 Scanner (Can scan NFC code, once scanned goes to ProductPage
 (Brand name, model no,image,desc,status & Manufacturer(location,timestamp))

 MyProfile(Details, and listview of transaction history)
		buyer    (Scanner for scanning sellers QR Code -> Scanner for scanning NFC code -> Confirm)


 Enviroment 2(Retailer):

 Login(username, password)

 Scanner(scans the NFC code, then the same process of second hand)
*/
