package es.alejandrtf.alarmavispa.views;


import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;



import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;


import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;


import es.alejandrtf.alarmavispa.R;
import es.alejandrtf.alarmavispa.models.Sighting;


public class CustomMarkerInfoWindowGoogleMap implements GoogleMap.InfoWindowAdapter {

    private Context context;
    private final View mInfoView;
    private int initialHeight = 0;
    private int targetHeight = 0;


    public CustomMarkerInfoWindowGoogleMap(Context ctx) {
        context = ctx;
        mInfoView = LayoutInflater.from(context).inflate(R.layout.custom_marker_info_window_view_google_map, null);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        renderSightingInfo(marker);
        return mInfoView;
    }


    /**
     * Method renders the information of marker
     *
     * @param marker the marker
     */
    private void renderSightingInfo(final Marker marker) {
        Sighting sighting = (Sighting) marker.getTag();

        TextView tvSightingState = mInfoView.findViewById(R.id.tvSightingStateCardView);
        final ImageView ivPhotoSighting = mInfoView.findViewById(R.id.ivPhotoSightingCardView);
        TextView tvCodeSightingCardView = mInfoView.findViewById(R.id.tvCodeSightingCardView);
        TextView tvSightingStateDetailCardView = mInfoView.findViewById(R.id.tvSightingStateDetailCardView);
        TextView tvSightingTypeDetailCardView = mInfoView.findViewById(R.id.tvSightingTypeDetailCardView);
        TextView tvSightingStandDetailCardView = mInfoView.findViewById(R.id.tvSightingStandDetailCardView);
        TextView tvSightingLocalityDetailCardView = mInfoView.findViewById(R.id.tvSightingLocalityDetailCardView);
        TextView tvSightingMunicipalityDetailCardView = mInfoView.findViewById(R.id.tvSightingMunicipalityDetailCardView);

        tvSightingState.setText(sighting.getState());
        Picasso.get()
                .load(sighting.getPhoto().get(0))
                .resize(context.getResources().getDimensionPixelSize(R.dimen.photo_sighting_info_window_width),
                        context.getResources().getDimensionPixelSize(R.dimen.photo_sighting_info_window_height))
                .centerInside()
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(ivPhotoSighting, new Callback() {
                    @Override
                    public void onSuccess() {
                        if (marker.isInfoWindowShown()) {
                            //reload infowindow
                            marker.hideInfoWindow();
                            marker.showInfoWindow();
                        }
                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });
        tvCodeSightingCardView.setText(marker.getTitle());
        tvSightingStateDetailCardView.setText(sighting.getState());
        tvSightingTypeDetailCardView.setText(sighting.getSightingType());
        tvSightingStandDetailCardView.setText(sighting.getNestStand());
        tvSightingLocalityDetailCardView.setText(sighting.getLocality());
        tvSightingMunicipalityDetailCardView.setText(sighting.getMunicipality());
    }


    public void collapseExpandSightingInformationCardView() {

        ValueAnimator slideAnimation = null;

        final ConstraintLayout clContainerExpandableInformation = mInfoView.findViewById(R.id.clContainerExpandableInformation);
        if (clContainerExpandableInformation.getVisibility() == View.GONE) {
            // it's collapsed - I must expand it

            // expand it
            clContainerExpandableInformation.setVisibility(View.VISIBLE);
            ((ImageView) mInfoView.findViewById(R.id.ivArrowDownCodeCardView)).setImageResource(R.drawable.arrow_up);

            // I take the new height measurement (either expanded or contracted)
            clContainerExpandableInformation.measure(View.MeasureSpec.makeMeasureSpec(clContainerExpandableInformation.getMeasuredWidth(),
                    View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(ViewGroup.LayoutParams.WRAP_CONTENT, View.MeasureSpec.UNSPECIFIED));
            if (targetHeight == 0) {
                targetHeight = clContainerExpandableInformation.getMeasuredHeight() / 2;
                initialHeight = targetHeight;
            }
        } else {
            // it's expanded - I must collapse it

            // collapse it
            clContainerExpandableInformation.setVisibility(View.GONE);
            ((ImageView) mInfoView.findViewById(R.id.ivArrowDownCodeCardView)).setImageResource(R.drawable.arrow_down);

        }


        // create the animation

        slideAnimation = ValueAnimator.ofInt(initialHeight, targetHeight)
                .setDuration(500);


        slideAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation1) {
                Integer value = (Integer) animation1.getAnimatedValue();
                clContainerExpandableInformation.getLayoutParams().height = value.intValue();
                clContainerExpandableInformation.requestLayout();

            }
        });

        AnimatorSet animationSet = new AnimatorSet();
        ((AnimatorSet) animationSet).setInterpolator(new AccelerateDecelerateInterpolator());
        ((AnimatorSet) animationSet).play(slideAnimation);
        animationSet.start();

    }
}
