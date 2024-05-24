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