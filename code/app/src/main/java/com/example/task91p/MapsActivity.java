package com.example.task91p;

import android.database.Cursor;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        dbHelper = new DBHelper(this);
    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        loadMarkers();
    }

    private void loadMarkers() {
        Cursor cursor = dbHelper.getAllItems();
        if (cursor.moveToFirst()) {
            do {
                String postType = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_POST_TYPE));
                String name = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_NAME));
                String location = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_LOCATION));

                try {
                    String[] latLng = location.split(", ");
                    double latitude = Double.parseDouble(latLng[0]);
                    double longitude = Double.parseDouble(latLng[1]);

                    LatLng itemLocation = new LatLng(latitude, longitude);
                    mMap.addMarker(new MarkerOptions()
                            .position(itemLocation)
                            .title(postType + " - " + name));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(itemLocation, 10));
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    // Handle invalid location format
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
    }
}