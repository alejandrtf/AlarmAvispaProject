package es.alejandrtf.alarmavispa.utilities;

import android.content.Context;

import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import es.alejandrtf.alarmavispa.R;
import es.alejandrtf.alarmavispa.models.Sighting;
import es.alejandrtf.alarmavispa.models.SightingType;
import es.alejandrtf.alarmavispa.models.State;

import static es.alejandrtf.alarmavispa.models.State.NOT_INTERVENTION_SIGHTING;
import static es.alejandrtf.alarmavispa.models.State.PENDING_RESOLUTION;
import static es.alejandrtf.alarmavispa.models.State.PENDING_VERIFICATION;
import static es.alejandrtf.alarmavispa.models.State.VERIFIED;

public class GoogleMaps {
    /**
     * Method that obtains the appropriate icon as wasps or nests have been seen
     *
     * @param context  context
     * @param sighting Sighting object that contains the information about what you have seen
     * @return
     */
    public static BitmapDescriptor getIconToAddInMap(Context context, Sighting sighting) {
        int backgroundColor = 0;

        if (sighting != null) {

            String state = sighting.getState();
            if (state != null) {
                if (state.equals(NOT_INTERVENTION_SIGHTING.getLocalizedName(context)))
                    backgroundColor = ContextCompat.getColor(context, R.color.not_intervention_sighting);
                else if (state.equals(PENDING_RESOLUTION.getLocalizedName(context)))
                    backgroundColor = ContextCompat.getColor(context, R.color.pending_resolution);
                else if (state.equals(PENDING_VERIFICATION.getLocalizedName(context)))
                    backgroundColor = ContextCompat.getColor(context, R.color.pending_verification);
                else if (state.equals(State.REPORTED.getLocalizedName(context)))
                    backgroundColor = ContextCompat.getColor(context, R.color.reported);
                else if (state.equals(State.SOLVED.getLocalizedName(context)))
                    backgroundColor = ContextCompat.getColor(context, R.color.solved);
                else if (state.equals(VERIFIED.getLocalizedName(context)))
                    backgroundColor = ContextCompat.getColor(context, R.color.verified);
                else backgroundColor = ContextCompat.getColor(context, R.color.reported);
            } else backgroundColor = ContextCompat.getColor(context, R.color.reported);

            if (sighting.getSightingType().equals(SightingType.NEST.getLocalizedName(context)))
                return Images.getBitmapFromVector(context, R.drawable.hornest_nest, R.drawable.location_marker_background, backgroundColor);
            else
                return Images.getBitmapFromVector(context, R.drawable.wasp_36dp, R.drawable.location_marker_background, backgroundColor);
        } else return BitmapDescriptorFactory.defaultMarker();
    }
}
