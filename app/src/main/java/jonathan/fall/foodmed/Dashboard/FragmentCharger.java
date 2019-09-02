package jonathan.fall.foodmed.Dashboard;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.commons.validator.routines.UrlValidator;

import java.util.Objects;

import jonathan.fall.foodmed.LoginActivity;
import jonathan.fall.foodmed.PreviousMeal.MealQuantityVAS;
import jonathan.fall.foodmed.R;
import jonathan.fall.foodmed.notifications.Notifications;
import jonathan.fall.foodmed.settings.Settings;

public class FragmentCharger extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawer;
    public static final String SHARED_PREF = "shPrefs";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_charger);

        Toolbar toolbar = findViewById(R.id.toolbar);
        NavigationView nv = findViewById(R.id.nav_view);
        View headerView = nv.getHeaderView(0);

        String patID= getPatientID();

        //Check if the patient ID and url have been set or not
        if(checkSettings() && !(patID.equalsIgnoreCase("0000"))){
            //all is okay do nothing and let the user proceed
        }
        else{
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setTitle("Something is missing!!")
                    .setMessage("Please check your settings")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.fragment_container, new Settings())
                                    .commit();
                        }
                    }).show();
        }
        //put the patient id
        TextView pID = headerView.findViewById(R.id.pID);
        pID.append(" "+patID);
        //if needed to change the user photo
        ImageView photo = headerView.findViewById(R.id.photo);


        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navView = findViewById(R.id.nav_view);

        navView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawer.addDrawerListener(toggle);
        toggle.syncState();

        /**
         * Open the page presenting the first set of questions
         * as soon ad the user logs in
         *
         **/
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new MealQuantityVAS()).commit();
            navView.setCheckedItem(R.id.nav_add_meal);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_notifications:
                //what to do when this item is clicked
                //Opening fragment
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new Notifications()).commit();
                break;
            case R.id.nav_settings:
                //what to do when this item is clicked
                //Opening fragment
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new Settings()).commit();
                break;
            case R.id.nav_add_meal:
                //what to do when this item is clicked
                //Opening fragment
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new MealQuantityVAS()).commit();
                break;
            case R.id.nav_dashboard:
                //what to do when this item is clicked
                //Opening fragment
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new Dashboard()).commit();
                break;
            case R.id.nav_logout:
                //delete the stored patient ID
                emptyPatientID();
                //get out
                startActivity(new Intent(getApplicationContext(), Dashboard.class));
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void emptyPatientID() {
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("patientID", "");
        editor.commit();
    }

    public String getPatientID() {
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
        String patientID = sharedPref.getString("patientID", "0000");
        return patientID;
    }

    public boolean checkSettings(){
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
        String desURL = sharedPref.getString("destinationURL", "no url");
        String pid =sharedPref.getString("patientID", "0000");
        String regex = "\\d+";
        if((new UrlValidator().isValid(desURL)) && pid.matches(regex)){
            return true;
        }
        else
            return false;
    }

}
