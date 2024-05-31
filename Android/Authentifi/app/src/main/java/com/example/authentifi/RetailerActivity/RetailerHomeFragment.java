package com.example.authentifi.RetailerActivity;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.example.authentifi.Assets.Asset;
import com.example.authentifi.Assets.AssetAdapter;
import com.example.authentifi.ConnectionManager;
import com.example.authentifi.R;
import com.example.authentifi.SellActivity.BuyActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RetailerHomeFragment extends Fragment {

	View view;
	List<Asset> assetList = new ArrayList<>();
	RecyclerView.Adapter mAdapter;

	RecyclerView mRecyclerView;
	ProgressBar progressBar;
	Button buyItem;

	public RetailerHomeFragment() {
		// Required empty public constructor
		super(R.layout.fragment_assets);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.i("RetailerHomeFragment", "RetailerHomeFragment Resumed!");

		if (progressBar != null) {
			Log.i("RetailerHomeFragment", "Progress bar visible");
			progressBar.setVisibility(View.VISIBLE);
		}
		if (mRecyclerView != null) {
			mRecyclerView.setVisibility(View.INVISIBLE);
		}

		Context context = getContext();
		if (context == null) {
			return;
		}
		Log.i("RetailerHomeFragment",  MainRetailerActivity.address);

		RequestQueue requestQueue = Volley.newRequestQueue(context);
		String URL = MainRetailerActivity.address + "/myProducts";

		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("email", MainRetailerActivity.email);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		String requestBody = jsonObject.toString();
		Log.i("RetailerHomeFragment", requestBody);

		ConnectionManager.sendData(requestBody, requestQueue, URL, new ConnectionManager.VolleyCallback() {
			@Override
			public void onSuccessResponse(String result) {
				Log.i("RetailerHomeFragment", "Result Success"+result);
				try {
					if (progressBar != null) {
						progressBar.setVisibility(View.INVISIBLE);
					}
					if (mRecyclerView != null) {
						mRecyclerView.setVisibility(View.VISIBLE);
					}

					JSONArray jsonArray = new JSONArray(result);
					JSONObject tempObject;
					assetList.clear();
					for (int i = 0; i < jsonArray.length(); i++) {
						tempObject = jsonArray.getJSONObject(i);
						assetList.add(new Asset(tempObject.getString("itemCode"),
								tempObject.getString("brand"), tempObject.getString("model")));
					}
					if (mAdapter != null) {
						Log.i("RetailerHomeFragment", "Adapter not null");
						mAdapter.notifyDataSetChanged();
					}

				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onErrorResponse(VolleyError error) {
				Toast toast = Toast.makeText(getContext(),
						"Could not connect to server, please try again.",
						Toast.LENGTH_LONG);
				toast.show();
			}
		});
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		Log.i("RetailerHomeFragment", "View created");



		/*buyItem.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.i("RetailerHomeFragment", "Buy button clicked");
				Intent intent = new Intent(getContext(), BuyActivity.class);
				ActivityOptions options =
						ActivityOptions.makeCustomAnimation(getContext(), R.anim.slide_in_right, R.anim.slide_out_left);
				startActivity(intent, options.toBundle());
			}
		});*/
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_assets, container, false);

		Asset asset = new Asset("somecode", "Nike", "Cloudfoam");
		assetList.add(asset);

		// RecyclerView
		mRecyclerView = view.findViewById(R.id.assetrecycler);
		progressBar = view.findViewById(R.id.progressBar);
		//buyItem = view.findViewById(R.id.buy);

		// Use this setting to improve performance if you know that changes
		// in content do not change the layout size of the RecyclerView
		mRecyclerView.setHasFixedSize(true);


		// Use a linear layout manager
		LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
		mRecyclerView.setLayoutManager(mLayoutManager);

		// Specify an adapter
		mAdapter = new AssetAdapter(assetList);
		mRecyclerView.setAdapter(mAdapter);
		DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
				mLayoutManager.getOrientation());
		mRecyclerView.addItemDecoration(dividerItemDecoration);
		Log.d("RetailerHomeFragment", "onCreateView() called");
		return view;
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
	}
}
