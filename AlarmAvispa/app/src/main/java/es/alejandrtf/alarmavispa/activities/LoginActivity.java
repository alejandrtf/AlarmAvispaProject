package es.alejandrtf.alarmavispa.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.os.Bundle;

import es.alejandrtf.alarmavispa.R;

public class LoginActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        toolbar=findViewById(R.id.toolbarLogin);
        setSupportActionBar(toolbar);

        // Getting the Navigation Controller
        navController = Navigation.findNavController(this, R.id.nav_host_autentication_fragment);
        //links actionbar with NavigationView to show a title in the toolbar based on the destination's label
        appBarConfiguration =
                new AppBarConfiguration.Builder()
                        .build();
        NavigationUI.setupWithNavController(toolbar, navController);


    }


    //Setting Up the back button
    @Override
  public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
    }
}
