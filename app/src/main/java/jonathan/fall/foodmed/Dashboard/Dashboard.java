package jonathan.fall.foodmed.Dashboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import databaseSettings.DatabaseHelper;
import jonathan.fall.foodmed.R;

public class Dashboard extends Fragment {
    public DatabaseHelper myDBHelper;
    private ListView dbCont;
    private ImageView img;
    public static final String SHARED_PREF = "shPrefs";
    private TextView noDataLab;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dashboard, container, false);

        dbCont = v.findViewById(R.id.dbContent);
        myDBHelper = new DatabaseHelper(getActivity());
        img = v.findViewById(R.id.img);
        noDataLab = v.findViewById(R.id.lab);
        noDataLab.setVisibility(View.INVISIBLE);
        try {
            populateList();
        } catch (NullPointerException ex) {
            img.setVisibility(View.INVISIBLE);
            noDataLab.setVisibility(View.VISIBLE);
        }
        return v;
    }

    public void populateList() {
        Cursor data = myDBHelper.getData(getPatientID());
        ArrayList<Double> list = new ArrayList<>();
        String imagePath = null;
        while (data.moveToNext()) {
            // list.add(data.getDouble(0));
            list.add(data.getDouble(3));
            list.add(data.getDouble(4));
            //the image
            imagePath = data.getString(2);
        }
        ListAdapter adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, list);
        dbCont.setAdapter(adapter);

        File image = new File(imagePath);
        img.setImageBitmap(BitmapFactory.decodeFile(image.getAbsolutePath()));

    }

    public String getPatientID() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
        String patientID = sharedPref.getString("patientID", "0000");
        return patientID;
    }


}
