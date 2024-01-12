package com.example.petsapp;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.petsapp.data.PetContract.PetEntry;

import com.example.petsapp.data.PetContract;
import com.example.petsapp.data.PetDbHelper;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_PET_LOADER = 0;
    private EditText mNameEditText;
    private EditText mBreedEditText;
    private EditText mWeightEditText;
    private Spinner mGenderSpinner;
    private int mGender = 0;

    private Uri mCurrentPetUri;

    private boolean mPetHasChanged = false;

    PetDbHelper mDbHelper;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mPetHasChanged = true;
            return false;
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
        mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);

        mDbHelper = new PetDbHelper(this);

        Intent intent = getIntent();
        mCurrentPetUri = intent.getData();

        setupSpinner();

        if(mCurrentPetUri == null){
            getSupportActionBar().setTitle("Add a Pet");
            invalidateOptionsMenu(); //This calls onPrepareOptionsMenu(). Doing this to remove delete pet menu item, when in insert mode.
        }else {
            getSupportActionBar().setTitle("Edit Pet");
            LoaderManager.getInstance(this).initLoader(EXISTING_PET_LOADER, null, this);
        }

        mNameEditText.setOnTouchListener(mTouchListener);
        mBreedEditText.setOnTouchListener(mTouchListener);
        mWeightEditText.setOnTouchListener(mTouchListener);
        mGenderSpinner.setOnTouchListener(mTouchListener);

        /** onBackPressed() has deprecated so I used this method */
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if(!mPetHasChanged){
                    finish();
                    return;
                }

                DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                };

                showUnsavedChangesDialog(discardButtonClickListener);
            }
        });
    }

    private void setupSpinner() {
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.array_gender_options, R.layout.gender_spinner_item);

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

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = 0; // Unknown
            }
        });
    }



    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Discard Changes and quit editing?");
        builder.setPositiveButton("Discard", discardButtonClickListener);
        builder.setNegativeButton("Keep Editing", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(dialog != null){
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }



    private void insertPet(ContentValues values){
        Uri newUri = getContentResolver().insert(PetEntry.CONTENT_URI, values);
        if(newUri == null){
            Toast.makeText(this, "Error! Pet Save failed", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(this, "New Pet Saved Successfully", Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    private void updatePet(ContentValues values){
        int rowsAffected = getContentResolver().update(mCurrentPetUri, values, null, null);
        if(rowsAffected <= 0){
            Toast.makeText(this, "Error! Pet update failed", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(this, "Pet Updated Successfully", Toast.LENGTH_SHORT).show();
        }
    }

    private void savePet(){
        SQLiteDatabase db = mDbHelper.getWritableDatabase();


        String petName = mNameEditText.getText().toString().trim();
        String petBreed = mBreedEditText.getText().toString().trim();
        String petWeight = mWeightEditText.getText().toString().trim();

        if(mCurrentPetUri == null && TextUtils.isEmpty(petName) && TextUtils.isEmpty(petBreed) && TextUtils.isEmpty(petWeight) && mGender == PetEntry.GENDER_UNKNOWN){
            Toast.makeText(this, "Insufficient Pet Information!", Toast.LENGTH_SHORT).show();
            return;
        }

        int weight;
        if(petWeight.isEmpty()){
            Toast.makeText(this, "Weight column is blank!", Toast.LENGTH_SHORT).show();
            return;
        }else {
            weight = Integer.parseInt(mWeightEditText.getText().toString().trim()); //If the string is empty, .parseInt() throws NumberFormatException. Leaving it for handling later.
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(PetEntry.COLUMN_PET_NAME, petName);
        contentValues.put(PetEntry.COLUMN_PET_BREED, petBreed);
        contentValues.put(PetEntry.COLUMN_PET_GENDER, mGender);
        contentValues.put(PetEntry.COLUMN_PET_WEIGHT, weight);


        if(mCurrentPetUri == null){
            insertPet(contentValues);
        }else {
            updatePet(contentValues);
        }
    }

    private void showDeletePetAlert(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete the Pet?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deletePet();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(dialog != null){
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deletePet(){
        int rowsDeleted = getContentResolver().delete(mCurrentPetUri, null, null);
        if(rowsDeleted != 0){
            Toast.makeText(this, "Pet Deleted Successfully", Toast.LENGTH_SHORT).show();
            finish(); //Going back to Catalog Activity when pet successfully deleted
        }else {
            Toast.makeText(this, "Pet Deletion Failed", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if(mCurrentPetUri == null){
            MenuItem deleteMenuItem = menu.findItem(R.id.action_delete);
            deleteMenuItem.setVisible(false);
        }

        return true;
    }

    private void upButtonPressed(){
        if(!mPetHasChanged){
            NavUtils.navigateUpFromSameTask(this);
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                NavUtils.navigateUpFromSameTask(EditorActivity.this);
            }
        };

        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_save){
            savePet();
            return true;
        }else if(id == R.id.action_delete){
            showDeletePetAlert();
            return true;
        }else if(id == android.R.id.home){
            upButtonPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        String[] projection = {
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED,
                PetEntry.COLUMN_PET_GENDER,
                PetEntry.COLUMN_PET_WEIGHT
        };

        return new CursorLoader(this, mCurrentPetUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        if(cursor == null || cursor.getCount() < 1){
            return;
        }

        if(cursor.moveToFirst()){
            int nameColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_NAME);
            int breedColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_BREED);
            int genderColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_GENDER);
            int weightColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_WEIGHT);

            String name = cursor.getString(nameColumnIndex);
            String breed = cursor.getString(breedColumnIndex);
            int gender = cursor.getInt(genderColumnIndex);
            int weight = cursor.getInt(weightColumnIndex);

            mNameEditText.setText(name);
            mBreedEditText.setText(breed);
            mWeightEditText.setText(Integer.toString(weight));
            switch (gender) {
                case PetEntry.GENDER_MALE:
                    mGenderSpinner.setSelection(1);
                    break;
                case PetEntry.GENDER_FEMALE:
                    mGenderSpinner.setSelection(2);
                    break;
                default:
                    mGenderSpinner.setSelection(0);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mNameEditText.setText("");
        mBreedEditText.setText("");
        mWeightEditText.setText("");
        mGenderSpinner.setSelection(0);
    }
}