package jonathan.fall.foodmed.PreviousMeal;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import jonathan.fall.foodmed.R;

public class MealQuantityVAS extends Fragment {
    //get the VAS
    private ImageView mood_VSatisfied, mood_satisfied, mood_neutral, mood_dissatisfied, mood_VDissatisfied;
    private TextView nextLabel, valSlider;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_meal_quantity, container, false);

        // set a change listener on the SeekBar
        SeekBar mySeekBar = v.findViewById(R.id.qt_eaten);

        nextLabel = (TextView) v.findViewById(R.id.next);
        valSlider = (TextView) v.findViewById(R.id.ValueOfSlider);

        //VAS settings
        mood_VSatisfied = (ImageView) v.findViewById(R.id.ate_all);
        mood_satisfied = (ImageView) v.findViewById(R.id.almost_all);
        mood_neutral = (ImageView) v.findViewById(R.id.half_of_it);
        mood_dissatisfied = (ImageView) v.findViewById(R.id.not_even_half);
        mood_VDissatisfied = (ImageView) v.findViewById(R.id.did_not);

        mySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                switch (i) {
                    case 0:
                        valSlider.setText(R.string.zero_consumed);
                        mood_VDissatisfied.setVisibility(View.VISIBLE);
                        mood_dissatisfied.setVisibility(View.GONE);
                        mood_neutral.setVisibility(View.GONE);
                        mood_satisfied.setVisibility(View.GONE);
                        mood_VSatisfied.setVisibility(View.GONE);
                        break;
                    case 1:
                        valSlider.setText(R.string.one_consumed);
                        mood_dissatisfied.setVisibility(View.VISIBLE);
                        mood_VDissatisfied.setVisibility(View.GONE);
                        mood_neutral.setVisibility(View.GONE);
                        mood_satisfied.setVisibility(View.GONE);
                        mood_VSatisfied.setVisibility(View.GONE);
                        break;
                    case 2:
                        valSlider.setText(R.string.two_consumed);
                        mood_neutral.setVisibility(View.VISIBLE);
                        mood_dissatisfied.setVisibility(View.GONE);
                        mood_VDissatisfied.setVisibility(View.GONE);
                        mood_satisfied.setVisibility(View.GONE);
                        mood_VSatisfied.setVisibility(View.GONE);
                        break;
                    case 3:
                        valSlider.setText(R.string.three_consumed);
                        mood_satisfied.setVisibility(View.VISIBLE);
                        mood_neutral.setVisibility(View.GONE);
                        mood_dissatisfied.setVisibility(View.GONE);
                        mood_VDissatisfied.setVisibility(View.GONE);
                        mood_VSatisfied.setVisibility(View.GONE);
                        break;
                    case 4:
                        valSlider.setText(R.string.four_consumed);
                        mood_VSatisfied.setVisibility(View.VISIBLE);
                        mood_neutral.setVisibility(View.GONE);
                        mood_dissatisfied.setVisibility(View.GONE);
                        mood_VDissatisfied.setVisibility(View.GONE);
                        mood_satisfied.setVisibility(View.GONE);
                        break;
                }
                nextLabel.setVisibility(View.VISIBLE);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        nextLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new QuestionsA())
                        .addToBackStack(null)
                        .commit();
            }
        });
        return v;
    }

    public void onBackPressed(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("Please confirm");
        builder.setMessage("Are you willing to exit the app?");
        builder.setCancelable(true);

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Do something when user want to exit the app
                // Let allow the system to handle the event, such as exit the app
                //MainActivity.super.onBackPressed();
                getActivity().finish();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Do something when want to stay in the app
                //Toast.makeText(mContext,"thank you",Toast.LENGTH_LONG).show();
            }
        });

        // Create the alert dialog using alert dialog builder
        AlertDialog dialog = builder.create();

        // Finally, display the dialog when user press back button
        dialog.show();
    }


}