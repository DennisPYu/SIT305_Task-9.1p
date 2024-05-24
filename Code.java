/*
-----------------------CreateAdvertActivity.java-----------------------
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








-----------------------DBHelper.java-----------------------
package com.example.task91p;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "lost_found.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "adverts";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_POST_TYPE = "post_type";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_PHONE = "phone";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_LOCATION = "location";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_POST_TYPE + " TEXT,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_PHONE + " TEXT,"
                + COLUMN_DESCRIPTION + " TEXT,"
                + COLUMN_DATE + " TEXT,"
                + COLUMN_LOCATION + " TEXT" + ")";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void insertAdvert(String postType, String name, String phone, String description, String date, String location) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_POST_TYPE, postType);
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_PHONE, phone);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_LOCATION, location);

        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public Cursor getAllItems() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
    }

    public Cursor getItemDetails(String name, String postType) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_NAME + "=? AND " + COLUMN_POST_TYPE + "=?", new String[]{name, postType});
    }

    public void deleteItem(String name, String postType) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_NAME + "=? AND " + COLUMN_POST_TYPE + "=?", new String[]{name, postType});
        db.close();
    }
}









-----------------------ItemDetailActivity.java-----------------------
package com.example.task91p;

import android.content.Intent;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ItemDetailActivity extends AppCompatActivity {

    private TextView textViewPostType;
    private TextView textViewName;
    private TextView textViewPhone;
    private TextView textViewDescription;
    private TextView textViewDate;
    private TextView textViewLocation;
    private Button buttonDelete;
    private DBHelper dbHelper;
    private String postType;
    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        textViewPostType = findViewById(R.id.textView_post_type);
        textViewPhone = findViewById(R.id.textView_phone);
        textViewDescription = findViewById(R.id.textView_description);
        textViewDate = findViewById(R.id.textView_date);
        textViewLocation = findViewById(R.id.textView_location);
        buttonDelete = findViewById(R.id.button_delete);

        dbHelper = new DBHelper(this);

        Intent intent = getIntent();
        postType = intent.getStringExtra("postType");
        name = intent.getStringExtra("name");

        loadItemDetails();

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteItem();
            }
        });
    }

    private void loadItemDetails() {
        Cursor cursor = dbHelper.getItemDetails(name, postType);
        if (cursor.moveToFirst()) {
            String postType = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_POST_TYPE));
            String name = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_NAME));
            String phone = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_PHONE));
            String description = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DESCRIPTION));
            String date = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DATE));
            String location = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_LOCATION));

            textViewPostType.setText(postType + ": " + name);
            textViewPhone.setText("Contact number: " + phone);
            textViewDescription.setText(description);
            textViewDate.setText(postType +" Date: "+ formatDate(date));
            textViewLocation.setText("At: " + getAddressFromLatLng(location));
        }
        cursor.close();
    }

    private void deleteItem() {
        dbHelper.deleteItem(name, postType);
        setResult(RESULT_OK);
        finish();
    }

    private String formatDate(String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        try {
            Date date = dateFormat.parse(dateString);
            Calendar calendar = Calendar.getInstance();
            long currentTime = calendar.getTimeInMillis();
            calendar.setTime(date);
            long itemTime = calendar.getTimeInMillis();
            long difference = currentTime - itemTime;
            long days = difference / (1000 * 60 * 60 * 24);

            if (days <= 3) {
                return days + " days ago";
            } else {
                return dateString;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return dateString;
        }
    }
    private String getAddressFromLatLng(String location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        String[] latLng = location.split(", ");
        try {
            double latitude = Double.parseDouble(latLng[0]);
            double longitude = Double.parseDouble(latLng[1]);
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address fetchedAddress = addresses.get(0);
                StringBuilder strAddress = new StringBuilder();
                for (int i = 0; i <= fetchedAddress.getMaxAddressLineIndex(); i++) {
                    strAddress.append(fetchedAddress.getAddressLine(i)).append(" ");
                }
                return strAddress.toString();
            } else {
                return "Unknown location";
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
            return location; // return coordinates if unable to fetch address
        }
    }
}










-----------------------MainActivity.java-----------------------

package com.example.task91p;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button buttonCreate;
    private Button buttonViewList;
    private Button buttonShowMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonCreate = findViewById(R.id.button_create);
        buttonViewList = findViewById(R.id.button_view_list);
        buttonShowMap = findViewById(R.id.button_show_map);

        buttonCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CreateAdvertActivity.class);
                startActivity(intent);
            }
        });

        buttonViewList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ViewListActivity.class);
                startActivity(intent);
            }
        });
        buttonShowMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });
    }
}







-----------------------MapsActivity.java-----------------------

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













-----------------------ViewListActivity.java-----------------------

package com.example.task91p;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class ViewListActivity extends AppCompatActivity {
    private static final String TAG = "ViewListActivity";

    private ListView listViewItems;
    private DBHelper dbHelper;
    private ArrayList<String> itemList;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_list);

        listViewItems = findViewById(R.id.listView_items);
        dbHelper = new DBHelper(this);
        itemList = new ArrayList<>();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, itemList);
        listViewItems.setAdapter(adapter);

        listViewItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = ((TextView) view).getText().toString();
                String[] parts = item.split(" - ");
                String postType = parts[0];
                String name = parts[1];

                Intent intent = new Intent(ViewListActivity.this, ItemDetailActivity.class);
                intent.putExtra("postType", postType);
                intent.putExtra("name", name);
                startActivityForResult(intent, 1);
            }
        });

        loadItems();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadItems();
    }

    private void loadItems() {
        itemList.clear();
        Cursor cursor = dbHelper.getAllItems();
        if (cursor.moveToFirst()) {
            do {
                Log.d(TAG, "Columns: " + cursor.getColumnNames());
                String postType = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_POST_TYPE));
                String name = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_NAME));
                itemList.add(postType + " - " + name);
            } while (cursor.moveToNext());
        }
        cursor.close();
        adapter.notifyDataSetChanged();
    }
}









 */