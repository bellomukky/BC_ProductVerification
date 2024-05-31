package com.example.authentifi;

import android.content.Intent;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.example.authentifi.RetailerActivity.MainRetailerActivity;
import com.example.authentifi.SellActivity.SellActivity;
import com.example.authentifi.UserActivity.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class ProductPage extends AppCompatActivity {

	TextView productCode, productBrand, productName, productDescription, productManufacturer, productStatus, productStatusDescription, productPickerText, productConfirmedByText;
	ImageButton productImage, productStatusImage, productSell, productConfirm, productPickup, productDeliver;
	ConstraintLayout productOwnerLayout;
	ConstraintLayout productLogisticLayout;
	ConstraintLayout productPickupLayout;
	ConstraintLayout productLogLayout;

	String prodCode;

	View view;
	//int i=3;
	//ImageButton testButton, testButton1;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.progressbar_loading);

		view = View.inflate(ProductPage.this, R.layout.activity_product_page, null);

		//Get UI elements
		productCode = (TextView) view.findViewById(R.id.product_code);
		productBrand = (TextView) view.findViewById(R.id.product_brand);
		productName = (TextView) view.findViewById(R.id.product_name);
		productDescription = (TextView) view.findViewById(R.id.product_description);
		productManufacturer = (TextView) view.findViewById(R.id.product_manufacturer);
		productStatus = (TextView) view.findViewById(R.id.product_status);
		productStatusDescription = (TextView) view.findViewById(R.id.product_status_description);
		productPickerText = (TextView) view.findViewById(R.id.picker_text);
		productConfirmedByText = (TextView) view.findViewById(R.id.confirm_text);
		productImage = (ImageButton) view.findViewById(R.id.product_image);
		productStatusImage = (ImageButton) view.findViewById(R.id.product_status_image);
		productOwnerLayout = (ConstraintLayout) view.findViewById(R.id.owner_section);
		productLogisticLayout = (ConstraintLayout) view.findViewById(R.id.logistic_section);
		productPickupLayout = (ConstraintLayout) view.findViewById(R.id.pickup_section);
		productLogLayout = (ConstraintLayout) view.findViewById(R.id.product_log);

		productConfirm = (ImageButton) view.findViewById(R.id.product_confirm_image);

		productPickup = (ImageButton) view.findViewById(R.id.product_pick_image);
		productDeliver = (ImageButton) view.findViewById(R.id.product_deliver_image);



		Bundle bundle = getIntent().getExtras();
		prodCode = bundle.getString("code");
		productCode.setText(prodCode);

		productConfirm.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				RequestQueue requestQueue = Volley.newRequestQueue(ProductPage.this);
				JSONObject jsonObject = new JSONObject();
				try {
					jsonObject.put("itemCode", prodCode);
					jsonObject.put("email", MainRetailerActivity.email);
				} catch (JSONException e) {
					e.printStackTrace();
				}

				String URL = MainRetailerActivity.address+"/confirmDelivery";
				ConnectionManager.sendData(jsonObject.toString(), requestQueue, URL, new ConnectionManager.VolleyCallback() {
					@Override
					public void onSuccessResponse(String result) {
						Toast toast = Toast.makeText(ProductPage.this,
								"Product delivery has been Confirmed.",
								Toast.LENGTH_LONG);

						toast.show();

						finish();
					}

					@Override
					public void onErrorResponse(VolleyError error) {
						error.printStackTrace();
						Toast toast = Toast.makeText(ProductPage.this,
								"Unable to confirm delivery.",
								Toast.LENGTH_LONG);
						Log.e("ProductPage", "sad");
					}
				});
			}
		});


		productPickup.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				RequestQueue requestQueue = Volley.newRequestQueue(ProductPage.this);
				JSONObject jsonObject = new JSONObject();
				try {
					jsonObject.put("itemCode", prodCode);
					jsonObject.put("logisticEmail", MainActivity.email);
				} catch (JSONException e) {
					e.printStackTrace();
				}

				String URL = MainActivity.address+"/pickItemForDelivery";
				ConnectionManager.sendData(jsonObject.toString(), requestQueue, URL, new ConnectionManager.VolleyCallback() {
					@Override
					public void onSuccessResponse(String result) {
						Toast toast = Toast.makeText(ProductPage.this,
								"Product has been picked up for delivery.",
								Toast.LENGTH_LONG);

						toast.show();

						finish();
					}

					@Override
					public void onErrorResponse(VolleyError error) {
						error.printStackTrace();
						Toast toast = Toast.makeText(ProductPage.this,
								"Unable to pickup product for delivery.",
								Toast.LENGTH_LONG);
						Log.e("ProductPage", "sad");
					}
				});
			}
		});

		productDeliver.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				RequestQueue requestQueue = Volley.newRequestQueue(ProductPage.this);
				JSONObject jsonObject = new JSONObject();
				try {
					jsonObject.put("itemCode", prodCode);
					jsonObject.put("logisticEmail", MainActivity.email);
				} catch (JSONException e) {
					e.printStackTrace();
				}

				String URL = MainActivity.address+"/markItemDelivered";
				ConnectionManager.sendData(jsonObject.toString(), requestQueue, URL, new ConnectionManager.VolleyCallback() {
					@Override
					public void onSuccessResponse(String result) {
						Toast toast = Toast.makeText(ProductPage.this,
								"Product has been mark As Delivered.",
								Toast.LENGTH_LONG);

						toast.show();

						finish();
					}

					@Override
					public void onErrorResponse(VolleyError error) {
						error.printStackTrace();
						Toast toast = Toast.makeText(ProductPage.this,
								"Product is fake.",
								Toast.LENGTH_LONG);
						Log.e("ProductPage", "sad");
					}
				});
			}
		});


		//Get data from server
		try {
			getData();
		} catch (JSONException e) {
			e.printStackTrace();
		}


		//close button
		ImageButton closeButton = (ImageButton) view.findViewById(R.id.close);
		closeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});



		//set product state accordingly
		productValid();

		//Cycle between outputs (testing purposes only)

	}


	private void productManufactured() {
		productStatusImage.setImageResource(R.drawable.ic_error_black);
		productStatus.setText(R.string.product_not_shipped);
		productStatusDescription.setText(R.string.product_not_shipped_description);
		Bundle bundle = getIntent().getExtras();
		Log.e("ProductPage", "isLogistic: "+bundle.getBoolean("isLogistic"));
	}

	private void productSold() {
		productStatusImage.setImageResource(R.drawable.ic_error_black);
		productStatus.setText(R.string.product_not_shipped);
		productStatusDescription.setText(R.string.product_not_shipped_description);
		Bundle bundle = getIntent().getExtras();
		Log.e("ProductPage", "isLogistic: "+bundle.getBoolean("isLogistic"));
		if(MainActivity.email!=null)
		{
			productPickupLayout.setVisibility(View.VISIBLE);
		}

	}

	private void productValid() {
		productStatusImage.setImageResource(R.drawable.ic_check_circle_black);
		productStatus.setText(R.string.product_valid);
		productStatusDescription.setText(R.string.product_valid_description);
	}

	private void productInTransit(JSONObject jsonObject) throws JSONException {
		productStatusImage.setImageResource(R.drawable.ic_transit_black);
		productStatus.setText(R.string.product_in_transit);
		if(jsonObject != null)
		{
			productStatusDescription.setText(getString(R.string.product_picker,
					jsonObject.getString("logisticName")+", "
							+jsonObject.getString("logisticLocation"),
					jsonObject.getString("pickedOn")));
		}
		Bundle bundle = getIntent().getExtras();
		Log.e("ProductPage", "isLogistic: "+bundle.getBoolean("isLogistic"));
		if(MainActivity.email!=null)
		{
			productLogisticLayout.setVisibility(View.VISIBLE);
		}

	}

	private void productMarkedAsDelivered(JSONObject jsonObject) throws JSONException {
		productStatusImage.setImageResource(R.drawable.ic_error_black);
		productStatus.setText(R.string.product_delivery_confirmation);

		productStatusDescription.setText(getString(R.string.product_delivery_info,
				jsonObject.getString("logisticName")+", "
						+jsonObject.getString("logisticLocation")));
		if(MainRetailerActivity.email!=null)
		{
			productOwnerLayout.setVisibility(View.VISIBLE);
		}

	}

	private void productDelivered(JSONObject jsonObject) throws JSONException {
		productStatusImage.setImageResource(R.drawable.ic_check_circle_black);
		productStatus.setText(R.string.product_delivered);
		productStatusDescription.setText(R.string.product_delivered_description);
		productLogLayout.setVisibility(View.VISIBLE);

		productPickerText.setText(getString(R.string.product_picker,
				jsonObject.getString("logisticName")+", "
						+jsonObject.getString("logisticLocation"),
				jsonObject.getString("pickedOn")));

		productConfirmedByText.setText(getString(R.string.product_confirmed,
				jsonObject.getString("retailerName")+", "
						+jsonObject.getString("retailerLocation"),
				jsonObject.getString("deliveredOn")));
	}



	private void productStolen() {
		productStatusImage.setImageResource(R.drawable.ic_error_black);
		productStatus.setText(R.string.product_fake);
		productStatusDescription.setText(R.string.product_stolen_description);
	}



	private void getData() throws JSONException {

		RequestQueue requestQueue = Volley.newRequestQueue(ProductPage.this);
		String address;
		if(MainActivity.address==null)
			address=MainRetailerActivity.address;
		else
			address=MainActivity.address;
		String URL = address+"/getProductDetails";
		final JSONObject jsonBody = new JSONObject();
		jsonBody.put("itemCode", prodCode);
		if(MainRetailerActivity.email != null)
		{
			jsonBody.put("email", MainRetailerActivity.email);
		}
		final String requestBody = jsonBody.toString();
		ConnectionManager.sendData(requestBody, requestQueue, URL, new ConnectionManager.VolleyCallback() {
			@Override
			public void onSuccessResponse(String result) {
				try {
					JSONObject jsonObject = new JSONObject(result);

					//Set data
					productName.setText(jsonObject.getString("model"));
					productBrand.setText(jsonObject.getString("name"));
					productDescription.setText(jsonObject.getString("description"));
					productManufacturer.setText(getString(R.string.product_manufacturer,
							jsonObject.getString("manufacturerName")+", "
									+jsonObject.getString("manufacturerLocation"),
							jsonObject.getString("manufacturerTimestamp")));

					switch(Integer.parseInt(jsonObject.getString("status"))) {
						case 0:
							productManufactured();
							break;
						case 1:
							productSold();;
							break;
						case 2:
							productInTransit(jsonObject);;
							break;
						case 3:
							productMarkedAsDelivered(jsonObject);
							break;
						case 4:
							productDelivered(jsonObject);;
							break;
						default:
							productStolen();
					}
					setContentView(view);

				} catch (JSONException e) {
					e.printStackTrace();
					Log.e("ProductPage", "sad");
				}


			}

			@Override
			public void onErrorResponse(VolleyError error) {

				Toast toast = Toast.makeText(ProductPage.this,
						"Could not connect to server, please try again.",
						Toast.LENGTH_LONG);

				toast.show();
			}
		});
	}


}

//$2b$10$8LgfFIfInVNDvOQU0KZr0OGtB38ISPAT3pIdvhiTolBCS6zyvapYO