package com.metricsfab.ubimp.main;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.metricsfab.ubimpservice.R;
import com.metricsfab.utils.ui.IOnEventListener;

/**
 * A simple {@link Fragment} subclass.
 *
 * create an instance of this fragment.
 */
public class MainFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    ImageView connectionImageView;

    TextView connectionTextView;

    TextView latitudeTextView;

    LinearLayout layout;

    private IOnEventListener listener;

    ImageView locationImageView;

    TextView locationTextView;

    TextView longitudeTextView;

    Switch startServiceSwitch;

    TextView velocityTextView;

    public MainFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment main.
     */
    // TODO: Rename and change types and number of parameters
    public static MainFragment newInstance(String param1, String param2) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public void onAttach(@NonNull Context context)
    {
        super.onAttach(context);

        try
        {
            if (context instanceof Activity)
            {
                this.listener = (IOnEventListener)context;
            }

        }
        catch (ClassCastException exception)
        {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(context.toString());
            stringBuilder.append("must implement OnHeadlineSelectedListener");
            throw new ClassCastException(stringBuilder.toString());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        this.startServiceSwitch = (Switch)view.findViewById(R.id.startRequestLocationSwitch);
        this.locationImageView = (ImageView)view.findViewById(R.id.locationImageView);

        this.locationTextView = (TextView)view.findViewById(R.id.locationTextView);
        this.connectionImageView = (ImageView)view.findViewById(R.id.connectionImageView);
        this.connectionTextView = (TextView)view.findViewById(R.id.connectionTextView);
        this.longitudeTextView = (TextView)view.findViewById(R.id.longitudeValue);
        this.latitudeTextView = (TextView)view.findViewById(R.id.latitudeValue);
        this.velocityTextView = (TextView)view.findViewById(R.id.velocityValue);
        this.layout = (LinearLayout)view.findViewById(R.id.linearLayoutMainFragment);

        this.startServiceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            public void onCheckedChanged(CompoundButton param1CompoundButton, boolean param1Boolean)
            {
                listener.onEventSwitch(param1Boolean);
            }
        });


        // Inflate the layout for this fragment
        return view;
    }

    public void setConnectionServerImage(int paramInt) { this.connectionImageView.setBackgroundResource(paramInt); }


    public void setConnectionServerText(int paramInt) { this.connectionTextView.setText(paramInt); }


    public void setLocation(double latitude, double longitude, float speed)
    {
        this.latitudeTextView.setText(String.valueOf(latitude));
        this.longitudeTextView.setText(String.valueOf(longitude));
        this.velocityTextView.setText(String.valueOf(speed));
    }

    public void setLocationImage(int paramInt) { this.locationImageView.setBackgroundResource(paramInt); }

    public void setLocationText(int paramInt) { this.locationTextView.setText(paramInt); }

    public void setStartServiceSwitch(boolean paramBoolean) { this.startServiceSwitch.setChecked(paramBoolean); }

    public void setWidgetsVisibility(int paramInt) {
        this.layout.setVisibility(paramInt);
        this.locationImageView.setVisibility(paramInt);
        this.locationTextView.setVisibility(paramInt);
        this.connectionImageView.setVisibility(paramInt);
        this.connectionTextView.setVisibility(paramInt);
        this.startServiceSwitch.setVisibility(paramInt);
        this.longitudeTextView.setVisibility(paramInt);
        this.latitudeTextView.setVisibility(paramInt);
        this.velocityTextView.setVisibility(paramInt);
    }


}