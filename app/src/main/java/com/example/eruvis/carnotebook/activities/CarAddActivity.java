package com.example.eruvis.carnotebook.activities;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.eruvis.carnotebook.models.Car;
import com.example.eruvis.carnotebook.fragments.SpinnerFragment;
import com.example.eruvis.carnotebook.GenerateRandom;
import com.example.eruvis.carnotebook.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static com.example.eruvis.carnotebook.AppConst.COLLECTION_CARS;
import static com.example.eruvis.carnotebook.AppConst.COLLECTION_USERS;
import static com.example.eruvis.carnotebook.AppConst.EXTRA_CAR;
import static com.example.eruvis.carnotebook.AppConst.EXTRA_EMPTY;

public class CarAddActivity extends AppCompatActivity {
    private static final String LOG_TAG = CarAddActivity.class.getSimpleName();

    private Toolbar toolbar;
    private ImageView civCarPhoto;
    private Uri imageUri;
    private EditText etCarBrand, etCarModel, spCarYearIssue, etCarMileage, spCarFuelType;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore db;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_add);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        KeyboardVisibilityEvent.setEventListener(this, new KeyboardVisibilityEventListener() {
            @Override
            public void onVisibilityChanged(boolean isOpen) {
                if (!isOpen && getCurrentFocus() != null) getCurrentFocus().clearFocus();
            }
        });

        initControls();
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
        toolbar.setOnMenuItemClickListener(menuListener);

        imageUri = null;
        civCarPhoto = (ImageView) findViewById(R.id.civ_car_photo);

        civCarPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSelectImageClick(view);
            }
        });

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        etCarBrand = findViewById(R.id.input_car_brand_et);
        etCarModel = findViewById(R.id.input_car_model_et);

        spCarYearIssue = findViewById(R.id.input_car_year_issue_sp);
        spCarYearIssue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomSpinnerYearIssue(v);
            }
        });

        etCarMileage = findViewById(R.id.input_car_mileage_et);

        spCarFuelType = findViewById(R.id.input_car_fuel_type_sp);
        spCarFuelType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomSpinnerFuelType(v);
            }
        });
    }

    public void showCustomSpinnerYearIssue(View v) {
        ArrayList<String> list = new ArrayList<String>();
        int year = Calendar.getInstance().get(Calendar.YEAR);
        while (year != 1969) {
            list.add(String.valueOf(year));
            year--;
        }

        int titleId = R.string.car_year_issue_hint;
        int editTextId = R.id.input_car_year_issue_sp;
        SpinnerFragment spinnerFragment = SpinnerFragment.newInstance(titleId, 0, list, editTextId);
        spinnerFragment.show(getSupportFragmentManager(), "customSpinner");
    }

    public void showCustomSpinnerFuelType(View v) {
        int titleId = R.string.car_fuel_type_hint;
        int listId = R.array.fuel_type_array;
        int editTextId = R.id.input_car_fuel_type_sp;
        SpinnerFragment spinnerFragment = SpinnerFragment.newInstance(titleId, listId, null, editTextId);
        spinnerFragment.show(getSupportFragmentManager(), "customSpinner");
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    private Toolbar.OnMenuItemClickListener menuListener =
            new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(@NonNull MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
                        case R.id.btn_add_car:
                            progressBar.setVisibility(View.VISIBLE);
                            onAddCar();
                            progressBar.setVisibility(View.INVISIBLE);
                            break;
                    }
                    return true;
                }
            };

    public void onAddCar() {
        String photoUri = imageUri != null ? imageUri.toString() : EXTRA_EMPTY;
        String brand = etCarBrand.getText().toString().trim();
        String model = etCarModel.getText().toString().trim();
        String name = brand + ' ' + model;
        String yearIssue = spCarYearIssue.getText().toString().trim();
        String mileage = etCarMileage.getText().toString().trim();
        String fuelType = spCarFuelType.getText().toString().trim();
        Date dateAdd = Calendar.getInstance().getTime();

        if (!validateInputs(brand, model, yearIssue, mileage, fuelType)) {
            return;
        }
        Date dateIssue = null;
        try {
            dateIssue = new SimpleDateFormat("yyyy", Locale.getDefault()).parse(yearIssue);
        } catch (ParseException e) {
            Log.w(LOG_TAG, e.getMessage());
        }
        final String documentName = GenerateRandom.randomString(14)
                + new SimpleDateFormat("ddMMyyyyHHmmss", Locale.getDefault())
                .format(Calendar.getInstance().getTime());

        final Car car = new Car(
                documentName,
                photoUri,
                brand,
                model,
                name,
                EXTRA_EMPTY,
                dateIssue,
                Integer.parseInt(mileage),
                fuelType,
                null,
                EXTRA_EMPTY,
                EXTRA_EMPTY,
                dateAdd);


        final CollectionReference dbCars;
        dbCars = db
                .collection(COLLECTION_USERS)
                .document(firebaseUser.getUid())
                .collection(COLLECTION_CARS);


        dbCars.document(documentName).set(car).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(LOG_TAG, "DocumentSnapshot added with ID: " + documentName);
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(LOG_TAG, "Error adding document", e);

                    }
                });

        Intent returnIntent = new Intent();
        returnIntent.putExtra(EXTRA_CAR, car);
        setResult(RESULT_OK, returnIntent);
        finish();

    }

    public boolean validateInputs(String brand, String model, String yearIssue,
                                  String mileage, String fuelType) {
        boolean valid = true;

        String carYearIssueHint = getText(R.string.car_year_issue_hint).toString();
        String carFuelTypeHint = getText(R.string.car_fuel_type_hint).toString();

        etCarBrand.setError(null);
        etCarModel.setError(null);
        spCarYearIssue.setError(null);
        etCarMileage.setError(null);
        spCarFuelType.setError(null);

        if (TextUtils.isEmpty(brand)) {
            etCarBrand.setError(getText(R.string.er_fill_field));
            valid = false;
        }

        if (TextUtils.isEmpty(model)) {
            etCarModel.setError(getText(R.string.er_fill_field));
            valid = false;
        }

        if (TextUtils.isEmpty(yearIssue) || yearIssue.equals(carYearIssueHint)) {
            spCarYearIssue.setError(getText(R.string.er_fill_field));
            valid = false;
        }

        if (TextUtils.isEmpty(mileage)) {
            etCarMileage.setError(getText(R.string.er_fill_field));
            valid = false;
        }

        if (TextUtils.isEmpty(fuelType) || fuelType.equals(carFuelTypeHint)) {
            spCarFuelType.setError(getText(R.string.er_fill_field));
            valid = false;
        }

        return valid;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_car, menu);
        return true;
    }

    public void onSelectImageClick(View view) {
        CropImage.activity(null)
                //.setCropMenuCropButtonIcon(R.drawable.baseline_done_white_48) // Установка иконки "принять(обрезать)"

                .setGuidelines(CropImageView.Guidelines.ON_TOUCH) // Включить линии
                .setCropShape(CropImageView.CropShape.OVAL) // Вид обрезания "ОВАЛ"
                .setAspectRatio(1, 1) // Соотношение сторон

                .setAllowFlipping(false)
                .setAllowCounterRotation(false)
                .setAllowRotation(false)

                .start(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageUri = result.getUri();
                Log.w("IMAGE", "uri:" + imageUri.toString());
                ((ImageView) findViewById(R.id.civ_car_photo)).setImageURI(imageUri);
                Toast.makeText(
                        this, "Cropping successful, Sample: " + result.getSampleSize(), Toast.LENGTH_LONG)
                        .show();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, "Cropping failed: " + result.getError(), Toast.LENGTH_LONG).show();
            }
        }
    }

}