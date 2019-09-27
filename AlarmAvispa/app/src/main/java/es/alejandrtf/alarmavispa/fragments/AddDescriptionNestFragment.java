package es.alejandrtf.alarmavispa.fragments;


import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

import es.alejandrtf.alarmavispa.R;
import es.alejandrtf.alarmavispa.models.Sighting;


/**
 * A simple {@link Fragment} subclass.
 */
public class AddDescriptionNestFragment extends Fragment {
    private static final String TAG = "AddDescripNestFragment";

    // Layout variables
    private Spinner spNestStand;
    private SeekBar sbNestHeight;
    private TextView tvProgressSeekbar, tvHelpNestType;
    private MaterialButton btNext;
    private RadioGroup rgNestType, rgWasAround;
    private ImageView ivNestImage;

    private Gson gson;
    private Sighting currentSighting;


    public AddDescriptionNestFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gson = new Gson();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_add_description_nest, container, false);
        setReferencesViewsToXML(v);
        return v;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String previousScreenInformationSighting = AddDescriptionNestFragmentArgs.fromBundle(getArguments()).getInformationFromLocation();
        //deserialize. This object contains the information of previous screen
        currentSighting = gson.fromJson(previousScreenInformationSighting, Sighting.class);
        initUI();
        setActionsToButtons();

    }


    //region CUSTOM METHODS

    /**
     * Gets  XML references for JAVA View objects
     */
    private void setReferencesViewsToXML(View v) {
        spNestStand = v.findViewById(R.id.spNestStand);
        sbNestHeight = v.findViewById(R.id.sbNestHeight);
        tvProgressSeekbar = v.findViewById(R.id.tvProgressSeekbar);
        tvHelpNestType = v.findViewById(R.id.tvLabelHelpDoubtsType);
        btNext = v.findViewById(R.id.btNextAddDescriptionNestFragment);
        rgNestType = v.findViewById(R.id.rgNestType);
        rgWasAround = v.findViewById(R.id.rgWaspAround);
        ivNestImage=v.findViewById(R.id.ornamentImage);
    }


    /**
     * Set up to each button its actions
     */
    private void setActionsToButtons() {
        rgNestType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int idOptionChecked) {
                if(idOptionChecked==R.id.rbPrimarySecondary){
                    ivNestImage.setImageResource(R.drawable.secondary_nest);
                }else{
                    ivNestImage.setImageResource(R.drawable.embryonic_nest);
                }
            }
        });

        sbNestHeight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                int val = (progress * (sbNestHeight.getWidth() - 2 * sbNestHeight.getThumbOffset())) / sbNestHeight.getMax();
                tvProgressSeekbar.setText("" + progress);
                tvProgressSeekbar.setX(sbNestHeight.getX() + val + sbNestHeight.getThumbOffset() / 2);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                tvProgressSeekbar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                tvProgressSeekbar.setVisibility(View.GONE);
            }
        });


        tvHelpNestType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                //set custom title and message
                builder.setTitle(R.string.titleDialogNestsTypes)
                        .setView(getLayoutInflater().inflate(R.layout.dialog_nests_types_differences, null));

                //set buttons
                builder.setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });


        btNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // add the information contained in this screen to the current sighting
                // obtained from the previous screen.
                currentSighting.setNestType(((RadioButton) getView().findViewById(rgNestType.getCheckedRadioButtonId())).getText().toString());
                currentSighting.setNestStand(spNestStand.getSelectedItem().toString());
                currentSighting.setHeight(sbNestHeight.getProgress());
                boolean wasAround = (rgWasAround.getCheckedRadioButtonId() == R.id.rbYesWasp) ? true : false;
                currentSighting.setWaspAround(wasAround);

                //serialize the Sighting object
                String sigtingObjectString = gson.toJson(currentSighting);
                Log.i(TAG,sigtingObjectString);

                //navigate to the AddWhoAreYouFragment passing the current info about the sighting serialized
                AddDescriptionNestFragmentDirections.ActionAddDescriptionNestFragmentToAddWhoAreYouFragment action = AddDescriptionNestFragmentDirections.actionAddDescriptionNestFragmentToAddWhoAreYouFragment(sigtingObjectString);
                Navigation.findNavController(view).navigate(action);

            }
        });

    }

    /**
     * Set up some views as spinner and so on
     */
    private void initUI() {
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.nest_stand_array, R.layout.item_spinner);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        // Apply the adapter to the spinner
        spNestStand.setAdapter(adapter);
    }
    //endregion

}
