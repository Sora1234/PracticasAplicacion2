package com.example.cristianxool.practicasaplicacion2;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;

/**
 * Created by Cristian Xool on 20/06/2016.
 */
public class  InformerApp extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_info);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int weigth = dm.widthPixels;
        int heigth = dm.heightPixels;

        getWindow().setLayout((int)(weigth*.8),(int)(heigth*.4));
    }
}
