package es.alejandrtf.alarmavispa.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.messaging.FirebaseMessaging;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import es.alejandrtf.alarmavispa.NavGraphDirections;
import es.alejandrtf.alarmavispa.R;
import es.alejandrtf.alarmavispa.storage.classes.Users;



public class MainActivity extends AppCompatActivity {

    // navigation
    Toolbar toolbar;
    NavController navController;
    BottomNavigationView bottomNavigation;
    NavigationView navigationView;
    DrawerLayout drawerLayout;
    AppBarConfiguration appBarConfiguration;

    private static MainActivity current;

    public static MainActivity getCurrentContext() {
        return current;
    }

    private String TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Getting the Navigation Controller
        navController = Navigation.findNavController(this, R.id.my_nav_host_fragment);

        setupBottomNavigationMenu(navController);
        setupNavigationViewMenu(navController);

        FirebaseMessaging.getInstance().subscribeToTopic("NewSighting")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            Log.d(TAG, "OK");
                        } else
                            Log.d(TAG, "ERROR");
                    }
                });


    }


    @Override
    protected void onStart() {
        super.onStart();

        //check if user is logged
        if (Users.isUserLogged(this)) {
            //change menu of Navigation View
            ((NavigationView) findViewById(R.id.nav_view)).getMenu().setGroupVisible(R.id.section_user_not_logged, false);
            ((NavigationView) findViewById(R.id.nav_view)).getMenu().setGroupVisible(R.id.section_user_logged, true);
        } else {
            //change menu of Navigation View
            ((NavigationView) findViewById(R.id.nav_view)).getMenu().setGroupVisible(R.id.section_user_not_logged, true);
            ((NavigationView) findViewById(R.id.nav_view)).getMenu().setGroupVisible(R.id.section_user_logged, false);

        }

        current = this;


    }


    @Override
    protected void onResume() {
        super.onResume();
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.keySet().size() > 5) {
            final String latitude = extras.getString("latitude");
            final String longitude = extras.getString("longitude");

            for (String key : extras.keySet()) {
                getIntent().removeExtra(key);
            }

            extras = null;

            AlertDialog ad = new AlertDialog.Builder(this).create();
            ad.setTitle(getString(R.string.title_dialog_notification_new_registered_sighting));
            ad.setMessage(getString(R.string.coordinates_new_sighting_title) + " (" + latitude + " , " + longitude + ")");
            ad.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.go),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            dialog.dismiss();
                            //navigate to the MapSightingFragment  passing as argument the latitude and longitude of the new registered sighting
                            //this navigation is using "safeArgs". See https://developer.android.com/guide/navigation/navigation-pass-data#Safe-args
                            NavDirections actionGlobalToMapSightingFragmentDest =
                                    NavGraphDirections.actionGlobalToMapSightingFragmentDest(latitude, longitude);
                            navController.navigate(actionGlobalToMapSightingFragmentDest);

                        }
                    });
            ad.setIcon(R.drawable.small_logo);
            ad.show();

        }
    }

    //Setting Up the back button
    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration);
    }


    //region MÉTODOS PROPIOS

    /**
     * Sets up the navigation controller to Bottom Nav
     *
     * @param navController-object that manages app navigation within a NavHost
     */
    private void setupBottomNavigationMenu(NavController navController) {
        bottomNavigation = findViewById(R.id.bottomNavigation);
        NavigationUI.setupWithNavController(bottomNavigation, navController);
    }


    /**
     * Sets up the navigation controller to the NavigationView  which is nested within
     * a DrawerLayout
     *
     * @param navController-object that manages app navigation within a NavHost
     */
    private void setupNavigationViewMenu(NavController navController) {
        navigationView = findViewById(R.id.nav_view);

        drawerLayout = findViewById(R.id.drawer_layout);
        //links actionbar with NavigationView
        appBarConfiguration =
                new AppBarConfiguration.Builder(R.id.informationHornetFragment,
                        R.id.notifySightingFragment,
                        R.id.mapSightingFragment,
                        R.id.trapFragment)
                        .setDrawerLayout(drawerLayout)
                        .build();
        NavigationUI.setupWithNavController(navigationView, navController);
        //In order to display hamburger icon on a top-level destination
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

    }


    //endregion

}
