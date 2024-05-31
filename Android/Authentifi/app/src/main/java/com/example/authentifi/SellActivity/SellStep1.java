package com.example.authentifi.SellActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.example.authentifi.RetailerActivity.MainRetailerActivity;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.example.authentifi.ConnectionManager;
import com.example.authentifi.UserActivity.MainActivity;
import com.example.authentifi.R;

import org.json.JSONException;
import org.json.JSONObject;

public class SellStep1 extends Fragment {

	ImageView qrview;

	public SellStep1() {
		// Required empty public constructor
	}



	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_sell_step1, container, false);

		qrview = view.findViewById(R.id.qrview);
		String address,email;
		if(MainActivity.address==null) {
			email = MainRetailerActivity.email;
			address = MainRetailerActivity.address;
		}
		else {
			address = MainActivity.address;
			email = MainActivity.email;
		}
//		Log.i("Kaldon-ss1", MainRetailerActivity.email);
		String URL = address+"/sell";
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("code", getActivity().getIntent().getExtras().getString("code"));
			jsonObject.put("email", email);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		String requestBody = jsonObject.toString();
		RequestQueue requestQueue = Volley.newRequestQueue(getContext());

		ConnectionManager.sendData(requestBody, requestQueue, URL, new ConnectionManager.VolleyCallback() {
			@Override
			public void onSuccessResponse(String result) {
				Log.i("Kaldon-res", result);
				SellActivity.qrcode = result;
				MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
				try {
					BitMatrix bitMatrix = multiFormatWriter.encode(result, BarcodeFormat.QR_CODE,200,200);
					BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
					Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
					qrview.setImageBitmap(bitmap);
				} catch (WriterException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onErrorResponse(VolleyError error) {
				Toast toast = Toast.makeText(getContext(),
						"Could not get QRcode, please try again.",
						Toast.LENGTH_LONG);

				toast.show();

			}
		});
		return view;
	}
}
