package es.alejandrtf.alarmavispa.fragments;


import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;


import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import es.alejandrtf.alarmavispa.R;
import es.alejandrtf.alarmavispa.storage.classes.Users;

/**
 * A simple {@link Fragment} subclass.
 */
public class CloseSessionFragment extends Fragment {


    public CloseSessionFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseAuth.getInstance().signOut();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //delete user from preferences
        Users.deleteUserFromPreferences(getContext());
        //check if user is logged
        if(Users.isUserLogged(getActivity())){
            //change menu of Navigation View

            ((NavigationView) getActivity().findViewById(R.id.nav_view)).getMenu().setGroupVisible(R.id.section_user_not_logged,false);
            ((NavigationView)getActivity().findViewById(R.id.nav_view)).getMenu().setGroupVisible(R.id.section_user_logged,true);
        }else{
            //change menu of Navigation View
            ((NavigationView)getActivity().findViewById(R.id.nav_view)).getMenu().setGroupVisible(R.id.section_user_not_logged,true);
            ((NavigationView)getActivity().findViewById(R.id.nav_view)).getMenu().setGroupVisible(R.id.section_user_logged,false);

        }
        //navigate to mainFragment from app
        Navigation.findNavController(getActivity(), R.id.my_nav_host_fragment).navigate(R.id.action_closeSessionOptionDest_to_notifySightingFragment);
    }
}
