package com.example.task91p;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CreateAdvertActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 44;

    private RadioGroup radioGroup;
    private RadioButton radioLost;
    private RadioButton radioFound;
    private EditText editTextName;
    private EditText editTextPhone;
    private EditText editTextDescription;
    private EditText editTextDate;
    private EditText editTextLocation;
    private Button buttonSave;
    private Button buttonGetCurrentLocation;
    private DBHelper dbHelper;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LatLng selectedLocation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        radioGroup = findViewById(R.id.radioGroup);
        radioLost = findViewById(R.id.radio_lost);
        radioFound = findViewById(R.id.radio_found);
        editTextName = findViewById(R.id.editText_name);
        editTextPhone = findViewById(R.id.editText_phone);
        editTextDescription = findViewById(R.id.editText_description);
        editTextDate = findViewById(R.id.editText_date);
        editTextLocation = findViewById(R.id.editText_location);
        buttonSave = findViewById(R.id.button_save);
        buttonGetCurrentLocation = findViewById(R.id.button_get_current_location);

        dbHelper = new DBHelper(this);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        }

        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                selectedLocation = place.getLatLng();
                editTextLocation.setText(place.getName());
            }

            @Override
            public void onError(@NonNull Status status) {
                Toast.makeText(CreateAdvertActivity.this, "Error: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        editTextDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });
        buttonGetCurrentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCurrentLocation();
            }
        });
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAdvert();
            }
        });
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    }


    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                CreateAdvertActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        editTextDate.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                    }
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void saveAdvert() {
        String postType = radioLost.isChecked() ? "Lost" : radioFound.isChecked() ? "Found" : "";
        String name = editTextName.getText().toString();
        String phone = editTextPhone.getText().toString();
        String description = editTextDescription.getText().toString();
        String date = editTextDate.getText().toString();
        String location = editTextLocation.getText().toString().trim();

        if (TextUtils.isEmpty(postType) || TextUtils.isEmpty(name) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(description) || TextUtils.isEmpty(date) || TextUtils.isEmpty(location)) {
            Toast.makeText(this, "You must fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedLocation != null) {
            location = selectedLocation.latitude + ", " + selectedLocation.longitude;
        }
        dbHelper.insertAdvert(postType, name, phone, description, date, location);

        Toast.makeText(this, "Advert Created!", Toast.LENGTH_SHORT).show();

        finish();
    }



    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                if (location != null) {
                    selectedLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    String address = getAddressFromLatLng(selectedLocation);
                    editTextLocation.setText(address);
                } else {
                    Toast.makeText(CreateAdvertActivity.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission is required to get current location", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private String getAddressFromLatLng(LatLng latLng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        String address = "";
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address fetchedAddress = addresses.get(0);
                StringBuilder strAddress = new StringBuilder();
                for (int i = 0; i <= fetchedAddress.getMaxAddressLineIndex(); i++) {
                    strAddress.append(fetchedAddress.getAddressLine(i)).append(" ");
                }
                address = strAddress.toString();
            } else {
                Log.d("CreateAdvertActivity", "No address found");
                address = "Unknown location";
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("CreateAdvertActivity", "Error in getting address: " + e.getMessage());
            address = "Unable to get location name";
        }
        return address;
    }


}