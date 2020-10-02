package com.navigine.naviginedemo;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.navigine.idl.java.Point;
import com.navigine.view.LocationView;

public class MainActivity extends AppCompatActivity {

    LocationView mLocationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLocationView = findViewById(R.id.location_view);
        mLocationView.init(getApplicationContext());
        mLocationView.setSublocation(D.SUBLOCATION_ID);

        mLocationView.getTouchInput().setLongPressResponder((x, y) -> mLocationView.setTargetPoint(new Point(x, y)));
    }
}
