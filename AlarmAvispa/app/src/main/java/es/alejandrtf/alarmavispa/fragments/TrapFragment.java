package es.alejandrtf.alarmavispa.fragments;


import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import es.alejandrtf.alarmavispa.NavGraphDirections;
import es.alejandrtf.alarmavispa.R;
import es.alejandrtf.alarmavispa.storage.classes.Users;


/**
 * A simple {@link Fragment} subclass.
 */
public class TrapFragment extends Fragment {
    private NavController navController;

    public TrapFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_trap, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //check if user is logged or not. It's required to be logged for this option
        if(!Users.isUserLogged(getContext())) {
            // send user to login screen

            navController = Navigation.findNavController(getActivity(), R.id.my_nav_host_fragment);

            //  navigate to the LoginActivity (to the fragment: LoginFragment) passing as argument
            // the fragment identifier from which you are navigating in order to go back after.
            //This navigation is using "safeArgs". See https://developer.android.com/guide/navigation/navigation-pass-data#Safe-args
            NavDirections actionGlobalToLoginActivityDest = NavGraphDirections.actionGlobalToLoginActivityDest(navController.getCurrentDestination().getLabel().toString());
            navController.navigate(actionGlobalToLoginActivityDest);
        }else{
            //is logged
            Log.d("TrapFragment","Logueado");
        }
    }
}
