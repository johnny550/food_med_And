package jonathan.fall.foodmed.PreviousMeal;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import jonathan.fall.foodmed.PresentMeal.ConditionStatement;
import jonathan.fall.foodmed.R;

public class QuestionsA extends Fragment {

    private String questionsList[];
    private ArrayList<String> checkedItems = new ArrayList<>();
    private static final String TAG = "MyActivity";
    private TextView nextLabel;
    private TextView prevLabel;
    private ListView blockA;
    private View view;
    private ListView questionsListView;

    public static final String SHARED_PREF= "shPrefs";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_first_block_of_questions, container, false);

        prevLabel = (TextView) view.findViewById(R.id.prev);
        nextLabel = (TextView) view.findViewById(R.id.next);
        questionsList = getResources().getStringArray(R.array.first_questions_block);

        questionsListView = (ListView) view.findViewById(R.id.questionsList);

        //The listView's adapter
        final ArrayAdapter<String> questionsLVAdapter = new ArrayAdapter<String>(
                getActivity(),
                android.R.layout.select_dialog_multichoice,
                questionsList
        );

        questionsListView.setAdapter(questionsLVAdapter);

        questionsListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        questionsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                //set the navigator's visibility to true
                nextLabel.setVisibility(View.VISIBLE);
                //prevLabel.setVisibility(View.VISIBLE);
                String item = (String) adapterView.getItemAtPosition(position);
                questionsLVAdapter.notifyDataSetChanged();
                //Log.i(TAG, "" + item);
                if (checkedItems.contains(item)) {
                    checkedItems.remove(item);
/*                    for (String num : checkedItems) {
                        System.out.println("-------------------------------------------------------is checked: "+num);
                    }*/
                } else {
                    checkedItems.add(item);
/*                    for (String num : checkedItems) {
                        System.out.println("-------------------------------------------------------is checked: "+num);
                    }*/
                }
            }
        });

        prevLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deletePreviousRecording();
                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new MealQuantityVAS())
                        .addToBackStack(null)
                        .commit();
            }
        });
        nextLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                spReinitialization();
                saveQuestions();
                getFragmentManager()
                        .beginTransaction()
                        .addToBackStack(null)
                        .replace(R.id.fragment_container, new ConditionStatement())
                        .commit();
            }
        });


        return view;
    }

    public void spReinitialization() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        //the questions
        String fullQuestionsList[] = getResources().getStringArray(R.array.first_questions_block);
        for (String question : fullQuestionsList) {
            editor.putBoolean(question, false);
        }

        //the meal quantity, condition and appetite...--not needed because they take a single value
        editor.commit();
    }

    public void saveQuestions(){
        SharedPreferences sharedPref = getActivity().getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        for(String question :checkedItems){
            editor.putBoolean(question, true);
        }
        editor.commit();
    }
    public void deletePreviousRecording(){
        SharedPreferences sharedPref = getActivity().getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("volume_meal", "0");
        editor.commit();
    }

/*    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        //save every element's state
        for (String i : checkedItems) {
            outState.putString(i, "checked");
        }

    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            int pos;
            for (pos = 0; pos < questionsListView.getCount(); pos++) {
                String myItem = questionsListView.getItemAtPosition(pos).toString();
                String toBeChecked = savedInstanceState.getString(myItem);
                if (toBeChecked.equalsIgnoreCase(questionsListView.getItemAtPosition(pos).toString())) {
                    questionsListView.setSelection(pos);
                    questionsListView.getSelectedView().setSelected(true);
                }

            }
        }
    }*/
}
