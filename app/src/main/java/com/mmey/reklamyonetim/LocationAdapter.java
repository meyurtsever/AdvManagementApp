package com.mmey.reklamyonetim;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationViewHolder> {
    private ArrayList<Company> mCompaniesNearBy;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public static class LocationViewHolder extends RecyclerView.ViewHolder {
        public TextView mTTextView1;
        public TextView mTTextView2;

        public LocationViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            mTTextView1 = (TextView) itemView.findViewById(R.id.textViewSirket);
            mTTextView2 = (TextView) itemView.findViewById(R.id.textViewAciklama);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }

    public LocationAdapter(ArrayList<Company> companiesNearBy) {
        mCompaniesNearBy = companiesNearBy;
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.location_item, viewGroup, false);
        LocationViewHolder lvh = new LocationViewHolder(v, mListener);
        return lvh;
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder locationViewHolder, int i) {
        Company currentCompany = mCompaniesNearBy.get(i);

        locationViewHolder.mTTextView1.setText(currentCompany.getCompanyName());
        locationViewHolder.mTTextView2.setText(currentCompany.getCampaignContent() + " | Sona erme: " + currentCompany.getCampaignExpireDate());
    }

    @Override
    public int getItemCount() {
        return mCompaniesNearBy.size();
    }
}