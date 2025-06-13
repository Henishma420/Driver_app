package com.eles.driver;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class SOSAdapter extends RecyclerView.Adapter<SOSAdapter.SOSViewHolder> {

    private Context context;
    private List<SOSData> sosList;

    public SOSAdapter(Context context, List<SOSData> sosList) {
        this.context = context;
        this.sosList = sosList;
    }

    @NonNull
    @Override
    public SOSViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_sos, parent, false);
        return new SOSViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SOSViewHolder holder, int position) {
        SOSData data = sosList.get(position);

        holder.tvLatitude.setText("Lat: " + data.latitude);
        holder.tvLongitude.setText("Long: " + data.longitude);
        holder.tvStatus.setText("Status: " + data.status);

        holder.btnAccept.setOnClickListener(v -> {
            Intent intent = new Intent(context, NewMap.class);
            intent.putExtra("lat", data.latitude);
            intent.putExtra("lon", data.longitude);
            intent.putExtra("request_id", data.requestId);
            context.startActivity(intent);
        });


        holder.btnReject.setOnClickListener(v -> {

            sosList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, sosList.size());
            Toast.makeText(context, "Removed request", Toast.LENGTH_SHORT).show();
        });

    }

    @Override
    public int getItemCount() {
        return sosList.size();
    }

    public static class SOSViewHolder extends RecyclerView.ViewHolder {
        TextView tvLatitude, tvLongitude, tvStatus;
        Button btnAccept, btnReject;

        public SOSViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLatitude = itemView.findViewById(R.id.tvLatitude);
            tvLongitude = itemView.findViewById(R.id.tvLongitude);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}
