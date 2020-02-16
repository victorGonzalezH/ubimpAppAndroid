package com.metricsfab.ubimp.service;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.metricsfab.ubimpservice.R;
import com.metricsfab.utils.ui.IOnEventListener;

public class MainFragment extends Fragment
{

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



    public void onAttach(Context context)
    {
        super.onAttach(context);

        try
        {
            if (context instanceof Activity)
            {
                this.listener = (IOnEventListener)context;
            }

            return;
        }
        catch (ClassCastException exception)
        {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(context.toString());
            stringBuilder.append("must implement OnHeadlineSelectedListener");
            throw new ClassCastException(stringBuilder.toString());
        }
    }


    public View onCreateView(LayoutInflater paramLayoutInflater, ViewGroup paramViewGroup, Bundle paramBundle)
    {
        View view = paramLayoutInflater.inflate(R.layout.fragment_main, paramViewGroup, false);
        this.startServiceSwitch = (Switch)view.findViewById(R.id.startServiceSwitch);
        this.locationImageView = (ImageView)view.findViewById(R.id.locationImageView);

        this.locationTextView = (TextView)view.findViewById(R.id.locationTextView);
        this.connectionImageView = (ImageView)view.findViewById(R.id.connectionImageView);
        this.connectionTextView = (TextView)view.findViewById(R.id.connectionTextView);
        this.longitudeTextView = (TextView)view.findViewById(R.id.longitudeValue);
        this.latitudeTextView = (TextView)view.findViewById(R.id.latitudeValue);
        this.velocityTextView = (TextView)view.findViewById(R.id.velocityValue);
        this.layout = (LinearLayout)view.findViewById(R.id.mainFragment);

        Log.d("GeolocationService", "Creating FragmentStatus");

        this.startServiceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            public void onCheckedChanged(CompoundButton param1CompoundButton, boolean param1Boolean)
            {
                listener.onEventSwitch(param1Boolean);
            }
        });

        return view;
    }


    public void setConnectionServerImage(int paramInt) { this.connectionImageView.setBackgroundResource(paramInt); }


    public void setConnectionServerText(int paramInt) { this.connectionTextView.setText(paramInt); }


    public void setLocation(double paramDouble1, double paramDouble2, float paramFloat)
    {
        this.longitudeTextView.setText(String.valueOf(paramDouble1));
        this.latitudeTextView.setText(String.valueOf(paramDouble2));
        this.velocityTextView.setText(String.valueOf(paramFloat));
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
