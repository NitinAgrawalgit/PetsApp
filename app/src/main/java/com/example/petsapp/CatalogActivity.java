package com.example.petsapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.petsapp.data.PetDbHelper;
import com.example.petsapp.data.PetContract.PetEntry;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class CatalogActivity extends AppCompatActivity {

    private PetDbHelper mDbHelper;
    TextView displayTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        displayTV = findViewById(R.id.text_view_pet);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        mDbHelper = new PetDbHelper(this);

        displayDatabaseInfo();
    }

    @Override
    protected void onStart() {
        super.onStart();
        displayDatabaseInfo();
    }

    private void displayDatabaseInfo() {
        PetDbHelper mDbHelper = new PetDbHelper(this);

        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + PetEntry.TABLE_NAME, null);

        String[] projection = {
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED,
                PetEntry.COLUMN_PET_GENDER,
                PetEntry.COLUMN_PET_WEIGHT
        };

        cursor = db.query(PetEntry.TABLE_NAME, projection, null, null, null, null, null);
        try {
//            TextView displayView = (TextView) findViewById(R.id.text_view_pet);
//            displayView.setText("Number of rows in pets database table: " + cursor.getCount());

            int idColumnIndex = cursor.getColumnIndex(PetEntry._ID);
            int nameColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_NAME);
            int breedColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_BREED);
            int genderColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_GENDER);
            int weightColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_WEIGHT);

            while(cursor.moveToNext()){
                int currentId = cursor.getInt(idColumnIndex);
                String currentName = cursor.getString(nameColumnIndex);
                String currentBreed = cursor.getString(breedColumnIndex);
                int currentGender = cursor.getInt(genderColumnIndex);
                int currentWeight = cursor.getInt(weightColumnIndex);

                String genderString = "null";
                switch(currentGender){
                    case PetEntry.GENDER_UNKNOWN:
                        genderString = "unknown";
                        break;
                    case PetEntry.GENDER_MALE:
                        genderString = "male";
                        break;
                    case PetEntry.GENDER_FEMALE:
                        genderString = "female";
                        break;
                }

                StringBuilder currentRow = new StringBuilder();
                currentRow.append(currentId + " | ");
                currentRow.append(currentName + " | ");
                currentRow.append(currentBreed + " | ");
                currentRow.append(genderString + " | ");
                currentRow.append(currentWeight + "\n");

                displayTV.append(currentRow);
            }

        } finally {
            // Always close the cursor when you're done reading from it. This releases all its
            // resources and makes it invalid.
            cursor.close();
        }
    }

    private void insertPet(){
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(PetEntry.COLUMN_PET_NAME, "Toto");
        contentValues.put(PetEntry.COLUMN_PET_BREED, "Terrier");
        contentValues.put(PetEntry.COLUMN_PET_GENDER, PetEntry.GENDER_MALE);
        contentValues.put(PetEntry.COLUMN_PET_WEIGHT, 7);

        long newRowId = db.insert(PetEntry.TABLE_NAME, null, contentValues);
        if(newRowId == -1){
            Toast.makeText(this, "Error! Row not inserted", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(this, "New row inserted: " + newRowId, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_insert_dummy_data){
            insertPet();
            displayDatabaseInfo();
            return true;
        }else if(id == R.id.action_delete_all_entries){
            //Do nothing for now
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}