package com.example.eruvis.carnotebook.activities;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.eruvis.carnotebook.models.Car;
import com.example.eruvis.carnotebook.R;

import java.text.SimpleDateFormat;
import java.util.Locale;

import static com.example.eruvis.carnotebook.AppConst.EXTRA_CAR;
import static com.example.eruvis.carnotebook.AppConst.EXTRA_EMPTY;

public class CarViewActivity extends AppCompatActivity {
    private static final String LOG_TAG = CarViewActivity.class.getSimpleName();

    private Toolbar toolbar;
    private CollapsingToolbarLayout cToolbarLayout;
    private ImageView ivCarPhoto;
    private EditText etCarBrand, etCarModel, etCarColor, spCarYearIssue, etCarMileage, spCarFuelType, etCarPurchaseDate, etCarNumber, etCarVIN;

    private Car car;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_view);

        car = (Car) getIntent().getSerializableExtra(EXTRA_CAR);

        initControls();
        carDataSet();
    }

    public void initControls() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        cToolbarLayout = findViewById(R.id.c_toolbar_layout);
        ivCarPhoto = findViewById(R.id.iv_car_photo);
        etCarBrand = findViewById(R.id.input_car_brand_et);
        etCarModel = findViewById(R.id.input_car_model_et);
        etCarColor = findViewById(R.id.input_car_color_et);
        spCarYearIssue = findViewById(R.id.input_car_year_issue_sp);
        etCarMileage = findViewById(R.id.input_car_mileage_et);
        spCarFuelType = findViewById(R.id.input_car_fuel_type_sp);
        etCarPurchaseDate = findViewById(R.id.input_purchase_date_et);
        etCarNumber = findViewById(R.id.input_car_number_et);
        etCarVIN = findViewById(R.id.input_car_vin_et);
    }

    public void carDataSet() {
        if (!car.getPhotoUri().equals(EXTRA_EMPTY)) {
            ivCarPhoto.setImageURI(Uri.parse(car.getPhotoUri()));
        }
        else {
            cToolbarLayout.setExpandedTitleColor(getResources().getColor(R.color.colorBackgroundToolbar));
        }
        Log.w(LOG_TAG, car.getPhotoUri());
        cToolbarLayout.setTitle(car.getName());
        etCarBrand.setText(car.getBrand());
        etCarModel.setText(car.getModel());
        etCarColor.setText(car.getColor().equals(EXTRA_EMPTY) ? "-" : car.getColor());
        spCarYearIssue.setText(new SimpleDateFormat("yyyy", Locale.getDefault()).format(car.getYearIssue()));
        etCarMileage.setText(String.valueOf(car.getMileage()));
        spCarFuelType.setText(car.getFuelType());
        etCarPurchaseDate.setText(car.getPurchaseDate() != null ?
                new SimpleDateFormat("dd.MM.yy", Locale.getDefault())
                        .format(car.getPurchaseDate()) : "");
        etCarNumber.setText(car.getNumber().equals(EXTRA_EMPTY) ? "-" : car.getNumber());
        etCarVIN.setText(car.getVin().equals(EXTRA_EMPTY) ? "-" : car.getVin());
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }
}
