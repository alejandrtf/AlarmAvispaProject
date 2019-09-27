package es.alejandrtf.alarmavispa.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.os.Bundle;
import android.widget.Toast;

import es.alejandrtf.alarmavispa.R;
import es.alejandrtf.alarmavispa.fragments.AddPhotosFragment;

public class AddSightingActivity extends AppCompatActivity{
    private Toolbar toolbar;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_sighting);

        toolbar = findViewById(R.id.toolbarAddSighting);
        setSupportActionBar(toolbar);

        // Getting the Navigation Controller
        navController = Navigation.findNavController(this, R.id.nav_host_notify_sighting_fragment);
        //links actionbar with NavigationView to show a title in the toolbar based on the destination's label
        appBarConfiguration =
                new AppBarConfiguration.Builder()
                        .build();
        NavigationUI.setupWithNavController(toolbar, navController);

       //String fileUriString= AddSightingActivityArgs.fromBundle(getIntent().getExtras()).getPhotoFileUri();
      // Toast.makeText(this,fileUriString,Toast.LENGTH_LONG).show();

    }



    //Setting Up the back button
    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
    }
}
