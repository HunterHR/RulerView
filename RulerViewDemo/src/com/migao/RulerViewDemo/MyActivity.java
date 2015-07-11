package com.migao.RulerViewDemo;

import android.app.Activity;
import android.os.Bundle;
import com.migao.RulerViewDemo.ruler.RulerView;

public class MyActivity extends Activity {


    private RulerView mgrulerOilWear;
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mgrulerOilWear = (RulerView) findViewById(R.id.mgruler_oil_wear);

        mgrulerOilWear.setValues(62, 77, 88, 97, 60, 76);

    }
}
