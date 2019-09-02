package jonathan.fall.foodmed.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Objects;

import jonathan.fall.foodmed.PreviousMeal.MealQuantityVAS;
import jonathan.fall.foodmed.R;

public class Settings extends Fragment {

    public TextView pid, urlTV, doneBT;
    public static final String SHARED_PREF = "shPrefs";
    public String urlVal, pidVal;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_settings, container, false);
        pid = v.findViewById(R.id.pid);
        urlTV = v.findViewById(R.id.url);
        doneBT = v.findViewById(R.id.validate);
        doneBT.setVisibility(View.INVISIBLE);


        String params = checkSettings();
        if(!(TextUtils.isEmpty(params))){
            urlTV.setText(params.substring(0, params.indexOf("+")));
            pid.setText(params.substring(params.indexOf("+")+1));
        }
        if (TextUtils.isEmpty(urlVal) || TextUtils.isEmpty(pidVal)) {
            doneBT.setVisibility(View.VISIBLE);
        }

        doneBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //save the url and the patient ID
                SharedPreferences sharedPref = getActivity().getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                urlVal = urlTV.getText().toString();
                pidVal = pid.getText().toString();
                editor.putString("destinationURL", urlVal);
                editor.putString("patientID", pidVal);
                editor.commit();

                Objects.requireNonNull(getFragmentManager())
                        .beginTransaction()
                        .replace(R.id.fragment_container, new MealQuantityVAS())
                        .addToBackStack(null)
                        .commit();
            }
        });

        return v;
    }


    public String checkSettings() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
        String desURL = sharedPref.getString("destinationURL", "no url");
        String pNum = sharedPref.getString("patientID", "0000");
        return desURL + "+" + pNum;
    }
}
