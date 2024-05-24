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
