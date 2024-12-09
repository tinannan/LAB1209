package com.example.lab1209;

import android.os.Bundle;
import android.text.Html;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AddListItems extends AppCompatActivity {

    private EditText addItemEditText;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.add_list_items);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.list), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        addItemEditText = findViewById(R.id.addItem);
        saveButton = findViewById(R.id.buttonSaveNote);

        saveButton.setOnClickListener(v -> {
            String url = addItemEditText.getText().toString();
            if (!url.isEmpty()) {
                fetchAndSaveData(url);
            } else {
                Toast.makeText(this, "Please paste a valid URL", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchAndSaveData(String url) {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(url).build();
                Response response = client.newCall(request).execute();

                if (response.isSuccessful()) {
                    String htmlContent = response.body().string();

                    // Extract item name
                    String itemNameStart = "item_name\":\"";
                    int startItemNameIndex = htmlContent.indexOf(itemNameStart);
                    if (startItemNameIndex == -1) {
                        Log.e("AddListItems", "Item name not found in the HTML.");
                        return;
                    }
                    startItemNameIndex += itemNameStart.length();
                    int endItemNameIndex = htmlContent.indexOf("\"", startItemNameIndex);
                    if (endItemNameIndex == -1) {
                        Log.e("AddListItems", "End of item name not found.");
                        return;
                    }

                    String itemName = htmlContent.substring(startItemNameIndex, endItemNameIndex);
                    Log.d("AddListItems", "Extracted Item Name: " + itemName);

                    // Extract promo data
                    String scriptStart = "const promo = `";
                    int startIndex = htmlContent.indexOf(scriptStart);
                    if (startIndex == -1) {
                        Log.e("AddListItems", "Promo data not found in the HTML.");
                        return;
                    }
                    startIndex += scriptStart.length();
                    int endIndex = htmlContent.indexOf("`", startIndex);
                    if (endIndex == -1) {
                        Log.e("AddListItems", "End of promo string not found.");
                        return;
                    }

                    String promoData = htmlContent.substring(startIndex, endIndex);
                    Log.d("AddListItems", "Extracted Promo Data: " + promoData);

                    // Decode HTML entities (like &quot;)
                    String decodedPromoData = Html.fromHtml(promoData).toString();
                    Log.d("AddListItems", "Decoded Promo Data: " + decodedPromoData);

                    // Initialize discount to 0 (no discount by default)
                    double discount = 0;
                    String discountText = "No Discount";  // Default text for no discount

                    if (decodedPromoData != null && !decodedPromoData.isEmpty()) {
                        try {
                            // Try parsing the decoded promo data into JSON
                            JSONObject promoJson = new JSONObject(decodedPromoData);
                            if (promoJson.has("percentage")) {
                                discount = promoJson.getDouble("percentage");
                                discountText = "Discount: " + discount + "%";  // Set the discount text
                                Log.d("AddListItems", "Discount: " + discount);
                            } else {
                                Log.e("AddListItems", "Promo data does not contain 'percentage'.");
                            }
                        } catch (JSONException e) {
                            Log.e("AddListItems", "Error parsing promo data: " + e.getMessage());
                            // If parsing fails, assume no discount and set default text
                            discountText = "No Discount";
                        }
                    } else {
                        Log.d("AddListItems", "No promo data found, setting discount to 0.");
                        discountText = "No Discount";  // If promo data is empty, show "No Discount"
                    }

                    // Save both item name and discount text to local JSON
                    saveToLocalJson(itemName, discountText);

                    String finalDiscountText = discountText;
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Item saved with discount: " + finalDiscountText, Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    });
                } else {
                    throw new Exception("Failed to fetch the URL");
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
                Log.e("AddListItems", "Error fetching data", e);
            }
        }).start();
    }



    private void saveToLocalJson(String name, String discountText) {
        try {
            File file = new File(getFilesDir(), "grocery_items.json");
            JSONArray itemsArray;

            // Check if the file exists, and read from it if it does
            if (file.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                reader.close();

                // Convert the string content into a JSONArray
                itemsArray = new JSONArray(stringBuilder.toString());
            } else {
                // If the file doesn't exist, create an empty JSONArray
                itemsArray = new JSONArray();
            }

            // Create a new item and add it to the JSONArray
            JSONObject newItem = new JSONObject();
            newItem.put("name", name);
            newItem.put("discount", discountText);  // Save discount as a string
            itemsArray.put(newItem);

            // Write the updated JSONArray to the file
            FileWriter writer = new FileWriter(file);
            writer.write(itemsArray.toString());
            writer.close();
        } catch (Exception e) {
            Log.e("AddListItems", "Error saving data", e);
        }
    }


}