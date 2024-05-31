package com.example.authentifi.RetailerActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.example.authentifi.MyProfileFragment;
import com.example.authentifi.ProductPage;
import com.example.authentifi.R;
import com.example.authentifi.UserActivity.MainActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.zxing.client.android.Intents;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class MainRetailerActivity extends AppCompatActivity {

	TextView title;
	ImageButton profileButton, closeButton;
	public static String email, address;

	public static boolean isRetailer = false;
	FragmentManager fragmentManager;

	FloatingActionButton scanButton;
	private static final int REQUEST_CAMERA_PERMISSION = 200;

	public MainRetailerActivity() {
		super(R.layout.activity_main_retailer);
		MainRetailerActivity.email = null;
	}

	private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
			result -> {
				if(result.getContents() == null) {
					Intent originalIntent = result.getOriginalIntent();
					if (originalIntent == null) {
						Log.d("MainActivity", "Cancelled scan");
						Toast.makeText(MainRetailerActivity.this, "Cancelled", Toast.LENGTH_LONG).show();
					} else if(originalIntent.hasExtra(Intents.Scan.MISSING_CAMERA_PERMISSION)) {
						Log.d("MainActivity", "Cancelled scan due to missing camera permission");
						Toast.makeText(MainRetailerActivity.this, "Cancelled due to missing camera permission", Toast.LENGTH_LONG).show();
					}
				} else {
					Log.d("MainActivity", "Scanned");
					Log.i("QRcode", result.getContents());
					// handle scan result
					Intent intent =  new Intent(MainRetailerActivity.this, ProductPage.class);
					Bundle bundle = new Bundle();
					bundle.putString("code", result.getContents());

					bundle.putBoolean("isRetailer", true);
					bundle.putBoolean("isLogistic", false);

					intent.putExtras(bundle);
					startActivity(intent);
				}
			});

	private void askForPermission(String permission, Integer requestCode) {
		if (ContextCompat.checkSelfPermission(MainRetailerActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
			if (ActivityCompat.shouldShowRequestPermissionRationale(MainRetailerActivity.this, permission)) {
				ActivityCompat.requestPermissions(MainRetailerActivity.this, new String[]{permission}, requestCode);
			} else {
				ActivityCompat.requestPermissions(MainRetailerActivity.this, new String[]{permission}, requestCode);
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_retailer);

		askForPermission(Manifest.permission.CAMERA, REQUEST_CAMERA_PERMISSION);

		email = getIntent().getStringExtra("email");
		address = getIntent().getStringExtra("address");
		isRetailer = true;

		title = findViewById(R.id.title_head);
		title.setText(R.string.products_head);

		final int fragmentContainer = R.id.fragment_container;
		fragmentManager = getSupportFragmentManager();
		final Fragment homeFragment = new RetailerHomeFragment();
		final Fragment profileFragment = new MyProfileFragment();
		fragmentManager.beginTransaction().add(fragmentContainer, homeFragment).commit();

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
						.replace(fragmentContainer, homeFragment)
						.commit();

				title.setText(R.string.products_head);
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

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == REQUEST_CAMERA_PERMISSION) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				// Permission granted
			} else {
				// Permission denied
				// Handle the case where the user denied the permission
			}
		}
	}
}
