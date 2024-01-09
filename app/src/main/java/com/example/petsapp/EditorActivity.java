package com.example.petsapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.petsapp.data.PetContract.PetEntry;

import com.example.petsapp.data.PetContract;
import com.example.petsapp.data.PetDbHelper;

public class EditorActivity extends AppCompatActivity {
    private EditText mNameEditText;
    private EditText mBreedEditText;
    private EditText mWeightEditText;
    private Spinner mGenderSpinner;
    private int mGender = 0;

    PetDbHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
        mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);

        mDbHelper = new PetDbHelper(this);

        setupSpinner();
    }

    private void setupSpinner() {
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.array_gender_options, android.R.layout.simple_spinner_item);

        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {
                        mGender = 1; // Male
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        mGender = 2; // Female
                    } else {
                        mGender = 0; // Unknown
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = 0; // Unknown
            }
        });
    }

    private void insertPet(){
        SQLiteDatabase db = mDbHelper.getWritableDatabase();


        String petName = mNameEditText.getText().toString().trim();
        String petBreed = mBreedEditText.getText().toString().trim();
        int petWeight = Integer.parseInt(mWeightEditText.getText().toString());

        ContentValues contentValues = new ContentValues();
        contentValues.put(PetEntry.COLUMN_PET_NAME, petName);
        contentValues.put(PetEntry.COLUMN_PET_BREED, petBreed);
        contentValues.put(PetEntry.COLUMN_PET_GENDER, mGender);
        contentValues.put(PetEntry.COLUMN_PET_WEIGHT, petWeight);

        long newRowId = db.insert(PetEntry.TABLE_NAME, null, contentValues);
        if(newRowId == -1){
            Toast.makeText(this, "Error! Row not inserted", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(this, "New row inserted: " + newRowId, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_save){
            insertPet();
            return true;
        }else if(id == R.id.action_delete){
            return true;
        }else if(id == android.R.id.home){
            // Navigate back to parent activity (CatalogActivity)
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}