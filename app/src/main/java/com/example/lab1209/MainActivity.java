package com.example.lab1209;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.StringWriter;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ListView itemsListView;
    private ArrayList<String> itemsList;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        itemsListView = findViewById(R.id.listView);
        itemsList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, itemsList);
        itemsListView.setAdapter(adapter);

        loadItemsFromJson();

        // click listener for add item button
        Button addNoteButton = findViewById(R.id.addItembutton);
        addNoteButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddListItems.class);
            startActivity(intent);
        });
    }
    private void loadItemsFromJson() {
        try {
            File file = new File(getFilesDir(), "grocery_items.json");
            if (file.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                StringWriter stringWriter = new StringWriter();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringWriter.write(line);
                }
                reader.close();

                // Convert the string content to JSONArray
                JSONArray itemsArray = new JSONArray(stringWriter.toString());

                itemsList.clear();
                for (int i = 0; i < itemsArray.length(); i++) {
                    JSONObject item = itemsArray.getJSONObject(i);
                    String name = item.getString("name");
                    String discount = item.getString("discount"); // Load discount as a String
                    String displayText = name + " - " + discount; // Display the discount text
                    itemsList.add(displayText);
                }
                adapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}