package jonathan.fall.foodmed.PresentMeal;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import jonathan.fall.foodmed.PreviousMeal.MealQuantityVAS;
import jonathan.fall.foodmed.PreviousMeal.QuestionsA;
import jonathan.fall.foodmed.R;

public class ConditionStatement extends Fragment {

    private ImageView notHungry0, notHungry, normal, hungry, ravenous;
    private ImageView notGood0, notGood, okay, well, great;
    private TextView next, previous;
    private String mealsList[];
    private ArrayList<String> checkedItems = new ArrayList<>();
    private ListView mealsListView;

    private int valAppetite, valCondition;

    public static final String SHARED_PREF = "shPrefs";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_conditions_for_present_meal, container, false);
        //Link the textView variables to ours on the layout
        next = (TextView) v.findViewById(R.id.Next);
        previous = (TextView) v.findViewById(R.id.Prev);

        //Inflate the listView with data from the xml
        mealsList = getResources().getStringArray(R.array.meal_propositions);

        //link the listView variable to our listView on the layout
        mealsListView = (ListView) v.findViewById(R.id.mealsList);

        //The listView's adapter
        final ArrayAdapter<String> mealsLVAdapter = new ArrayAdapter<String>(
                getActivity(),
                android.R.layout.simple_list_item_single_choice,
                mealsList
        );

        //set an adapter for our listView
        mealsListView.setAdapter(mealsLVAdapter);

        mealsListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mealsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                //set the navigator's visibility to true
                next.setVisibility(View.VISIBLE);
                // previous.setVisibility(View.VISIBLE);
                String item = (String) adapterView.getItemAtPosition(position);
                mealsLVAdapter.notifyDataSetChanged();
                //Log.i(TAG, "" + item);
                if (checkedItems.contains(item)) {
                    checkedItems.remove(item);
/*                    for (String num : checkedItems) {
                        System.out.println("-------------------------------------------------------is checked: "+num);
                    }*/
                } else {
                    checkedItems.clear();
                    checkedItems.add(item);
/*                    for (String num : checkedItems) {
                        System.out.println("-------------------------------------------------------is checked: "+num);
                    }*/
                }
            }
        });


        //Methods for next and previous labels
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deletePreviousQuestions();
                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new QuestionsA())
                        .addToBackStack(null)
                        .commit();
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveMultipleData();
                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new MealMain())
                        .commit();
            }
        });

        //The seekbars (sliders)
        SeekBar userAppetite = v.findViewById(R.id.appetite);
        SeekBar userCondition = v.findViewById(R.id.condition);

        //Appetite ImageViews
        notHungry0 = v.findViewById(R.id.not_hungry_at_all);
        notHungry = v.findViewById(R.id.not_hungry);
        normal = v.findViewById(R.id.i_could_eat);
        hungry = v.findViewById(R.id.hungry);
        ravenous = v.findViewById(R.id.ravenous);

        //Condition ImageViews
        notGood0 = v.findViewById(R.id.not_good_at_all);
        notGood = v.findViewById(R.id.not_good);
        okay = v.findViewById(R.id.okay);
        well = v.findViewById(R.id.well);
        great = v.findViewById(R.id.great);

        //Setting the appetite seek bar
        userAppetite.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                switch (i) {
                    case 0:
                        valAppetite = 0;
                        notHungry0.setVisibility(View.VISIBLE);
                        notHungry.setVisibility(View.GONE);
                        normal.setVisibility(View.GONE);
                        hungry.setVisibility(View.GONE);
                        ravenous.setVisibility(View.GONE);
                        break;
                    case 1:
                        valAppetite = 25;
                        notHungry.setVisibility(View.VISIBLE);
                        notHungry0.setVisibility(View.GONE);
                        normal.setVisibility(View.GONE);
                        hungry.setVisibility(View.GONE);
                        ravenous.setVisibility(View.GONE);
                        break;
                    case 2:
                        valAppetite = 50;
                        normal.setVisibility(View.VISIBLE);
                        notHungry0.setVisibility(View.GONE);
                        notHungry.setVisibility(View.GONE);
                        hungry.setVisibility(View.GONE);
                        ravenous.setVisibility(View.GONE);
                        break;
                    case 3:
                        valAppetite = 75;
                        hungry.setVisibility(View.VISIBLE);
                        notHungry0.setVisibility(View.GONE);
                        notHungry.setVisibility(View.GONE);
                        normal.setVisibility(View.GONE);
                        ravenous.setVisibility(View.GONE);
                        break;
                    case 4:
                        valAppetite = 100;
                        ravenous.setVisibility(View.VISIBLE);
                        notHungry0.setVisibility(View.GONE);
                        notHungry.setVisibility(View.GONE);
                        hungry.setVisibility(View.GONE);
                        normal.setVisibility(View.GONE);
                        break;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //Setting the condition seek bar
        userCondition.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                switch (i) {
                    case 0:
                        valCondition = 0;
                        notGood0.setVisibility(View.VISIBLE);
                        notGood.setVisibility(View.GONE);
                        okay.setVisibility(View.GONE);
                        well.setVisibility(View.GONE);
                        great.setVisibility(View.GONE);
                        break;
                    case 1:
                        valCondition = 25;
                        notGood.setVisibility(View.VISIBLE);
                        notGood0.setVisibility(View.GONE);
                        okay.setVisibility(View.GONE);
                        well.setVisibility(View.GONE);
                        great.setVisibility(View.GONE);
                        break;
                    case 2:
                        valCondition = 50;
                        okay.setVisibility(View.VISIBLE);
                        notGood.setVisibility(View.GONE);
                        notGood0.setVisibility(View.GONE);
                        well.setVisibility(View.GONE);
                        great.setVisibility(View.GONE);
                        break;
                    case 3:
                        valCondition = 75;
                        well.setVisibility(View.VISIBLE);
                        notGood.setVisibility(View.GONE);
                        okay.setVisibility(View.GONE);
                        notGood0.setVisibility(View.GONE);
                        great.setVisibility(View.GONE);
                        break;
                    case 4:
                        valCondition = 100;
                        great.setVisibility(View.VISIBLE);
                        notGood.setVisibility(View.GONE);
                        okay.setVisibility(View.GONE);
                        well.setVisibility(View.GONE);
                        notGood0.setVisibility(View.GONE);
                        break;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        return v;
    }

    public void saveMultipleData() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        //Meals first
        for (String meal : checkedItems) {
            editor.putBoolean(meal, true);
        }
        //Then the appetite and conditions
        editor.putFloat("Appetite", valAppetite);
        editor.putFloat("Condition", valCondition);

        editor.commit();
    }

    public void deletePreviousQuestions() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
        String questionsList[] = getResources().getStringArray(R.array.first_questions_block);
        SharedPreferences.Editor editor = sharedPref.edit();
        for (String item : questionsList) {
            // if(sharedPref.getString(item,"")){
            editor.putBoolean(item, false);
            //}
        }
        editor.commit();
    }

}
