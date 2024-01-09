package com.example.petsapp.data;

import android.net.Uri;
import android.provider.BaseColumns;

final public class PetContract {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public static final String CONTENT_AUTHORITY = "com.example.petsapp";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_PETS = PetEntry.TABLE_NAME;

    private PetContract() {}


    public static final class PetEntry implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PETS);

        //Changed from pets to petstable
        public final static String TABLE_NAME = "petstable";
        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_PET_NAME ="name";
        public final static String COLUMN_PET_BREED = "breed";
        public final static String COLUMN_PET_GENDER = "gender";
        public final static String COLUMN_PET_WEIGHT = "weight";


        public static final int GENDER_UNKNOWN = 0;
        public static final int GENDER_MALE = 1;
        public static final int GENDER_FEMALE = 2;
    }

}
