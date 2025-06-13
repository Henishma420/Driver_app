package com.eles.driver;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class AcceptedDetails extends AppCompatActivity {

    private TextView latText, lonText, placeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accepted_details);

        latText = findViewById(R.id.latitude_text);
        lonText = findViewById(R.id.longitude_text);
        placeText = findViewById(R.id.place_name_text);

        double lat = getIntent().getDoubleExtra("lat", 0);
        double lon = getIntent().getDoubleExtra("lon", 0);

        latText.setText("Latitude: " + lat);
        lonText.setText("Longitude: " + lon);

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
            if (!addresses.isEmpty()) {
                Address address = addresses.get(0);
                String placeName = address.getAddressLine(0);
                placeText.setText("Location: " + placeName);
            } else {
                placeText.setText("Location: Unknown");
            }
        } catch (IOException e) {
            placeText.setText("Error fetching location");
            e.printStackTrace();
        }
    }
}

