package es.alejandrtf.alarmavispa.fragments;


import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.anychart.APIlib;
import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.chart.common.listener.Event;
import com.anychart.chart.common.listener.ListenersInterface;
import com.anychart.charts.Pie;
import com.anychart.enums.Align;
import com.anychart.enums.LegendLayout;
import com.anychart.graphics.vector.SolidFill;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.alejandrtf.alarmavispa.R;
import es.alejandrtf.alarmavispa.models.Sighting;
import es.alejandrtf.alarmavispa.storage.classes.SightingFirestore;
import es.alejandrtf.alarmavispa.storage.interfaces.SightingAsync;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChartByMunicipalitiesCurrentMonthFragment extends Fragment implements SightingAsync.OnCompleteGetSightingsMonthYearListener
{
    private AnyChartView anyChartView;
    private String TAG = "ChartByMunicipalitiesCurrentMonth";

    //Firebase Firestore variables
    private SightingAsync sightings;
    private List<Sighting> sightingList;

    // AnyChart
    private List<DataEntry> data;
    private Pie pie;


    public ChartByMunicipalitiesCurrentMonthFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        String year = String.valueOf(calendar.get(Calendar.YEAR));
        int month=calendar.get(Calendar.MONTH)+1;//january==0, february==1,...

        // get the helpers to make CRUD operations in Cloud Firestore with nodes: SightingAsync
        sightings = new SightingFirestore(getContext());
        sightings.getSightingsMonthAndYear(year, month, new SightingAsync.OnCompleteGetSightingsMonthYearListener() {
            @Override
            public void onCompleteGetSightingsMonthYear(List<Sighting> list) {
               if(list.size()>0) {
                   sightingList = list;
                   Map<String,Integer>numberSightingsByMunicipality=getNumberSightingsByMunicipalityFrom(sightingList);
                   data=new ArrayList<>();
                   for (Map.Entry<String,Integer>entry :
                           numberSightingsByMunicipality.entrySet()) {
                       data.add(new ValueDataEntry(entry.getKey(),entry.getValue()));

                   }
                   pie.data(data);
                   pie.title(getString(R.string.map_sighting_menu_option_municipalities_monthly_chart));
                   pie.labels().position("inside");
                   pie.labels().fontSize(12d);
                   pie.legend().title().enabled(true);
                   pie.legend().title()
                           .text(getString(R.string.municipalities))
                           .padding(0d,0d,10d,0d);
                   pie.legend()
                           .position("center-bottom")
                           .itemsLayout(LegendLayout.HORIZONTAL)
                           .align(Align.CENTER);
                   anyChartView.setChart(pie);


               }else{
                   Toast.makeText(getContext(), getString(R.string.not_data), Toast.LENGTH_LONG).show();
               }
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
        pie = AnyChart.pie3d();
        pie.background().fill(new SolidFill("#FFCCBC", 0.3));
        pie.setOnClickListener(new ListenersInterface.OnClickListener(new String[]{"x","value"}) {
            @Override
            public void onClick(Event event) {
                Toast.makeText(getContext(), event.getData().get("x")+":"+event.getData().get("value"), Toast.LENGTH_SHORT).show();
            }
        });
        return v;
    }


    /** Method gets for each municipality in that list, the total number of sightings.
     *
     * @param list list that contains many sightings (some of them for the same municipality)
     * @return a map where the key is the municipality and the object is a number that represents how many
     * sightings there were in that municipality
     */
    private Map<String,Integer>getNumberSightingsByMunicipalityFrom(List<Sighting>list){
        Map<String,Integer> map=new HashMap<>();
        if(list.size()>0){
            for(Sighting sighting:list){
                String key=sighting.getMunicipality();
                if(map.containsKey(key))
                    map.put(key,map.get(key)+1);
                else
                    map.put(key,1);
            }
        }
        return map;

    }


    @Override
    public void onCompleteGetSightingsMonthYear(List<Sighting> sightingList) {

    }
}
