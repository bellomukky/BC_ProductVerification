package com.example.authentifi.SellActivity;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.example.authentifi.ConnectionManager;
import com.example.authentifi.UserActivity.MainActivity;
import com.example.authentifi.R;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A simple {@link Fragment} subclass.
 */
public class BuyStep2 extends Fragment {

	TextView textView;

	public BuyStep2() {
		// Required empty public constructor
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		// Inflate the layout for this fragment

		IntentIntegrator.forSupportFragment(this)
				.setPrompt("Scan the Product's QR code")
				.setOrientationLocked(false)
				.initiateScan();


		View view= inflater.inflate(R.layout.fragment_buy_step1, container, false);

		textView = view.findViewById(R.id.nexttocontinue);



		return view;

	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
		if(result != null) {
			RequestQueue requestQueue = Volley.newRequestQueue(getContext());
			JSONObject jsonObject = new JSONObject();
			try {
				jsonObject.put("code", result.getContents());
				jsonObject.put("QRCode", BuyActivity.qrcode);
			} catch (JSONException e) {
				e.printStackTrace();
			}

			String URL = MainActivity.address+"/getProductDetails";
			ConnectionManager.sendData(jsonObject.toString(), requestQueue, URL, new ConnectionManager.VolleyCallback() {
				@Override
				public void onSuccessResponse(String result) {
					textView.setVisibility(View.VISIBLE);
				}

				@Override
				public void onErrorResponse(VolleyError error) {

				}
			});
		}
	}
}
