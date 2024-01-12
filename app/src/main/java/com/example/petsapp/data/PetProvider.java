package com.example.petsapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.petsapp.data.PetContract.PetEntry;

public class PetProvider extends ContentProvider {
    private static final int PETS = 100;
    private static final int PET_ID = 101;

    private static final String LOG_TAG = PetProvider.class.getSimpleName();

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS, PETS);
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS + "/#", PET_ID);
    }

    private PetDbHelper mDbHelper;
    @Override
    public boolean onCreate() {
        mDbHelper = new PetDbHelper(getContext().getApplicationContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch(match){
            case PETS:
                cursor = db.query(PetEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case PET_ID:
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                cursor = db.query(PetEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unkown URI format: " + uri);
        }

        /** Setting notification uri for the loader to listen to*/
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return PetContract.CONTENT_LIST_TYPE;
            case PET_ID:
                return PetContract.CONTETN_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unkown URI: " + uri + " with match: " + match);
        }
    }

    private Uri insertPet(Uri uri, ContentValues values){
        /**
         * Data Sanity checks
         */
        String name = values.getAsString(PetEntry.COLUMN_PET_NAME);
        if(name == null || name.length() == 0){
            /** Below line would abruptly end the activity, instead I just return a null URI, so it displays a toast in the activity. */
            //throw new IllegalArgumentException("Pet name can't be blank!");
            return null;
        }

        Integer gender = values.getAsInteger(PetEntry.COLUMN_PET_GENDER);
        if(gender == null || !PetEntry.isValidGender(gender)){
            return null;
        }

        Integer weight = values.getAsInteger(PetEntry.COLUMN_PET_WEIGHT);
        if(weight == null || weight <= 0){
            return null;
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        long newRowId = db.insert(PetEntry.TABLE_NAME, null, values);
        if(newRowId == -1){
            Log.e(LOG_TAG, "Failed to insert Row: " + uri);
            return null;
        }

        /** Setting notification uri for the Loader to listen to*/
        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, newRowId);
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch(match){
            case PETS:
                return insertPet(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for URI format: " + uri);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        int rowsDeleted;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                rowsDeleted = db.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PET_ID:
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = db.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Delete operation not supported for URI format: " + uri);
        }

        if(rowsDeleted != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    private int updatePet(Uri uri, ContentValues values, String selection, String[] selectionArgs){
        /**
         * Sanity Checks:
         */

        if(values.containsKey(PetEntry.COLUMN_PET_NAME)){
            String name = values.getAsString(PetEntry.COLUMN_PET_NAME);
            if(name == null || name.length() == 0){
                /**
                 * I'm not supposed to, but to differentiate valid update queries that change zero rows,
                 * I'm assigning -1 as the code for invalid update queries, which will be handled by the activity that is calling the update query.
                 */
                 return -1;
            }
        }

        if(values.containsKey(PetEntry.COLUMN_PET_GENDER)){
            Integer gender = values.getAsInteger(PetEntry.COLUMN_PET_GENDER);
            if(gender == null || !PetEntry.isValidGender(gender)){
                return -1;
            }
        }

        if(values.containsKey(PetEntry.COLUMN_PET_WEIGHT)){
            Integer weight = values.getAsInteger(PetEntry.COLUMN_PET_WEIGHT);
            if(weight == null || weight <= 0){
                return -1;
            }
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        int rowsUpdated = db.update(PetEntry.TABLE_NAME, values, selection, selectionArgs);

        /** Setting notification uri for the loader to listen to*/
        if(rowsUpdated != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        int match = sUriMatcher.match(uri);
        switch(match){
            case PETS:
                return updatePet(uri, values, selection, selectionArgs);
            case PET_ID:
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updatePet(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for URI format: " + uri);
        }
    }
}
