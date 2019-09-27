package es.alejandrtf.alarmavispa.fragments;



import android.content.DialogInterface;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;


import android.view.View;


import es.alejandrtf.alarmavispa.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class AboutFragment extends Fragment {


    public AboutFragment() {
        // Required empty public constructor
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        showAlertDialogAbout();
    }


    //region CUSTOM METHODS

    /**
     * Method shows an alert dialog with a custom layout
     */
    private void showAlertDialogAbout() {


        AlertDialog.Builder ad = new AlertDialog.Builder(getContext());
        ad.setTitle(getString(R.string.about));

        View alertDialogLayout = getLayoutInflater().inflate(R.layout.alertdialog_about, null);
        ad.setView(alertDialogLayout);
        ad.setCancelable(false);
        ad.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                //navigate to mainFragment from app
                Navigation.findNavController(getActivity(), R.id.my_nav_host_fragment).navigate(R.id.action_aboutFragment_to_notifySightingFragment);

            }
        });
        ad.create().show();
    }
    //endregion
}



