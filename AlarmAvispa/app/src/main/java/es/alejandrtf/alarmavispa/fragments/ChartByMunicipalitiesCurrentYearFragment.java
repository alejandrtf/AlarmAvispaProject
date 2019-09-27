package es.alejandrtf.alarmavispa.fragments;


import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.anychart.APIlib;
import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian3d;
import com.anychart.data.Mapping;
import com.anychart.data.Set;
import com.anychart.enums.Anchor;
import com.anychart.enums.HoverMode;
import com.anychart.enums.TooltipPositionMode;
import com.anychart.graphics.vector.SolidFill;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import es.alejandrtf.alarmavispa.R;
import es.alejandrtf.alarmavispa.models.MunicipalityNotifications;
import es.alejandrtf.alarmavispa.storage.classes.NumberNotificationsYearMunicipalityFirestore;
import es.alejandrtf.alarmavispa.storage.interfaces.NumberNotificationsYearMunicipalityAsync;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChartByMunicipalitiesCurrentYearFragment extends Fragment implements NumberNotificationsYearMunicipalityAsync.OnCompleteGetMunicipalitiesListener {
    private AnyChartView anyChartView;
    private String TAG = "ChartByMunicipalitiesCurrentYear";

    //Firebase Firestore variables
    private NumberNotificationsYearMunicipalityAsync numberNotificationsYearMunicipality; //interface
    private List<MunicipalityNotifications> listMunicipalities;
    // AnyChart
    private List<DataEntry> data;
    private Cartesian3d bar3d;

    public ChartByMunicipalitiesCurrentYearFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        String year = String.valueOf(calendar.get(Calendar.YEAR));
        // get the helpers to make CRUD operations in Cloud Firestore with nodes: NumberNotificationsYearMunicipalityAsync
        numberNotificationsYearMunicipality = new NumberNotificationsYearMunicipalityFirestore(getContext());
        numberNotificationsYearMunicipality.getMunicipalitiesInfoThatYear(year, new NumberNotificationsYearMunicipalityAsync.OnCompleteGetMunicipalitiesListener() {
            @Override
            public void onCompleteGetMunicipalities(List<MunicipalityNotifications> list) {
                listMunicipalities = list;
                data = new ArrayList<>();
                Set set = null;

                // fill data to chart
                for (MunicipalityNotifications municipality :
                        listMunicipalities) {
                    data.add(new CustomDataEntry(municipality.getMunicipality(), municipality.getNumberSightings()));
                }
                set = Set.instantiate();
                set.data(data);

                Mapping barData = set.mapAs("{x: 'x', value: ' value'}");
                bar3d.bar(barData).name(getString(R.string.chartBar3dSightingByMunicipalityYAxisTitle));

                bar3d.legend().enabled(true);
                bar3d.legend().fontSize(12d);
                bar3d.legend().padding(0d, 0d, 20d, 0d);

                bar3d.interactivity().hoverMode(HoverMode.SINGLE);
                bar3d.tooltip()
                        .positionMode(TooltipPositionMode.POINT)
                        .position("right")
                        .anchor(Anchor.LEFT_CENTER)
                        .offsetX(5d)
                        .offsetY(0d)
                        .format("{%Value}");

                bar3d.zAspect("10%")
                        .zPadding(20d)
                        .zAngle(45d)
                        .zDistribution(true);


                anyChartView.setChart(bar3d);


            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, e.getLocalizedMessage());
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_any_chart_view, container, false);

        anyChartView = v.findViewById(R.id.any_chart_view);
        APIlib.getInstance().setActiveAnyChartView(anyChartView);
        anyChartView.setProgressBar((v.findViewById(R.id.progress_bar)));
        bar3d = AnyChart.bar3d();
        bar3d.background().fill(new SolidFill("#FFCCBC", 0.3));
        bar3d.animation(true);
        bar3d.padding(10d, 40d, 5d, 20d);
        bar3d.title(getString(R.string.chartBar3dSightingByMunicipalityTitle));
        bar3d.yScale().minimum(0d);
        bar3d.xAxis(0).labels()
                .rotation(-90d)
                .padding(0d, 0d, 20d, 0d);
        bar3d.yAxis(0).labels().format("{%Value}{groupsSeparator: }");
        bar3d.yAxis(0).title(getString(R.string.chartBar3dSightingByMunicipalityYAxisTitle));



        return v;
    }


    @Override
    public void onCompleteGetMunicipalities(List<MunicipalityNotifications> list) {

    }

    @Override
    public void onError(Exception e) {

    }


    private class CustomDataEntry extends ValueDataEntry {
        CustomDataEntry(String x, Number value) {
            super(x, value);

        }
    }
}
