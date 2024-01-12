package com.example.petsapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.petsapp.data.PetDbHelper;
import com.example.petsapp.data.PetContract.PetEntry;
import com.example.petsapp.data.PetProvider;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int PET_LOADER = 0;

    PetCursorAdapter mCursorAdapter;

    ListView listView;

    View emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        listView = findViewById(R.id.pets_list_view);
        emptyView = findViewById(R.id.empty_view);

        listView.setEmptyView(emptyView);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        mCursorAdapter = new PetCursorAdapter(this, null);
        listView.setAdapter(mCursorAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Uri currentPetUri = ContentUris.withAppendedId(PetEntry.CONTENT_URI, id);

                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                intent.setData(currentPetUri);
                startActivity(intent);
            }
        });

        LoaderManager.getInstance(this).initLoader(PET_LOADER, null,this);
    }

    private void insertDummyPet(){
        PetDbHelper mDbHelper = new PetDbHelper(this);

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(PetEntry.COLUMN_PET_NAME, "Toto");
        contentValues.put(PetEntry.COLUMN_PET_BREED, "Terrier");
        contentValues.put(PetEntry.COLUMN_PET_GENDER, PetEntry.GENDER_MALE);
        contentValues.put(PetEntry.COLUMN_PET_WEIGHT, 7);

        Uri newUri = getContentResolver().insert(PetEntry.CONTENT_URI, contentValues);
        if(newUri == null){
            Toast.makeText(this, "Error! Pet save failed", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(this, "Pet Saved Successfully", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    private void deleteAllPetsAlert(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete all data?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteAllPets();
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
    private void deleteAllPets(){
        int rowsDeleted = getContentResolver().delete(PetEntry.CONTENT_URI, null, null);
        Toast.makeText(this, "Rows Deleted: " + rowsDeleted, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_insert_dummy_data){
            insertDummyPet();
            //displayDatabaseInfo();
            return true;
        }else if(id == R.id.action_delete_all_entries){
            deleteAllPetsAlert();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {

        return new CursorLoader(this, PetEntry.CONTENT_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }
}