package com.example.authentifi.Assets;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import com.example.authentifi.ProductPage;
import com.example.authentifi.R;

public class AssetAdapter extends RecyclerView.Adapter<AssetAdapter.MyViewHolder> {
	private String[] mDataset;

	private List<Asset> mAssetList;

	@Override
	public int getItemCount() {
		return mAssetList.size();
	}

	// Provide a reference to the views for each data item
	// Complex data items may need more than one view per item, and
	// you provide access to all the views for a data item in a view holder
	public static class MyViewHolder extends RecyclerView.ViewHolder {
		// each data item is just a string in this case
		public TextView mCode, mName;

		public MyViewHolder(ConstraintLayout v) {
			super(v);
			mCode = (TextView) v.findViewById(R.id.asset_code);
			mName = (TextView) v.findViewById(R.id.asset_name);
		}
	}

	// Provide a suitable constructor (depends on the kind of dataset)
	public AssetAdapter(List<Asset> myDataset) {
		mAssetList = myDataset;
	}

	// Create new views (invoked by the layout manager)
	@Override
	public AssetAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
	                                                 int viewType) {
		// create a new view
		ConstraintLayout v = (ConstraintLayout) LayoutInflater.from(parent.getContext())
				.inflate(R.layout.asset_item, parent, false);

		MyViewHolder vh = new MyViewHolder(v);
		return vh;
	}

	// Replace the contents of a view (invoked by the layout manager)
	@Override
	public void onBindViewHolder(MyViewHolder holder, int position) {
		// - get element from your dataset at this position
		// - replace the contents of the view with that element
		holder.mCode.setText(mAssetList.get(position).productCode);
		holder.mName.setText(mAssetList.get(position).productModel);

		holder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Use getAdapterPosition to ensure getting the correct item index
				int adapterPosition = holder.getAdapterPosition();
				if (adapterPosition != RecyclerView.NO_POSITION) {
					// Get the data at the new position
					Asset asset = mAssetList.get(adapterPosition);

					// Create an intent to start a new activity
					Intent intent = new Intent(v.getContext(), ProductPage.class);
					Bundle bundle = new Bundle();
					bundle.putString("code", asset.productCode);  // Use data from the clicked item
					bundle.putBoolean("isOwner", true);
					intent.putExtras(bundle);
					v.getContext().startActivity(intent);
				}
			}

		});
	}

	// Return the size of your dataset (invoked by the layout manager)




	//Everything I added:

}