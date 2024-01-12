package com.example.petsapp;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.petsapp.data.PetContract;
import com.example.petsapp.data.PetContract.PetEntry;

public class PetCursorAdapter extends CursorAdapter {

    public PetCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pet_list_item, parent, false);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        int nameColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_NAME);
        int breedColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_BREED);
        int genderColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_GENDER);
        int weightColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_WEIGHT);

        TextView nameView = view.findViewById(R.id.name_view);
        nameView.setText(cursor.getString(nameColumnIndex));

        TextView breedView = view.findViewById(R.id.breed_view);
        breedView.setText(cursor.getString(breedColumnIndex));

        TextView weightView = view.findViewById(R.id.weight_view);
        weightView.setText(cursor.getInt(weightColumnIndex) + " Kgs");

        ImageView genderView = view.findViewById(R.id.gender_view);
        int gender = cursor.getInt(genderColumnIndex);
        switch (gender) {
            case 0:
                genderView.setVisibility(View.INVISIBLE);
                break;
            case 1:
                genderView.setImageResource(R.drawable.gender_male);
                break;
            case 2:
                genderView.setImageResource(R.drawable.gender_female);
                break;
            default:
                genderView.setVisibility(View.GONE);
        }
    }
}
