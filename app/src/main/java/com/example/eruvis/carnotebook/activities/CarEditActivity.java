package com.example.eruvis.carnotebook.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.eruvis.carnotebook.models.Car;
import com.example.eruvis.carnotebook.fragments.SpinnerFragment;
import com.example.eruvis.carnotebook.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;

import java.text.DateFormat;
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

public class CarEditActivity extends AppCompatActivity {
    private static final String LOG_TAG = CarEditActivity.class.getSimpleName();

    private Toolbar toolbar;
    private ProgressBar progressBar;

    private ImageView civCarPhoto;
    private Uri imageUri;

    private EditText etCarBrand, etCarModel, etCarColor, spCarYearIssue, etCarMileage, spCarFuelType, etCarPurchaseDate, etCarNumber, etCarVIN;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore db;

    private Car car;
    private DatePicker datePicker;
    private DateFormat dateFormat;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_edit);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        KeyboardVisibilityEvent.setEventListener(this, new KeyboardVisibilityEventListener() {
            @Override
            public void onVisibilityChanged(boolean isOpen) {
                if (!isOpen && getCurrentFocus() != null) getCurrentFocus().clearFocus();
            }
        });

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
        toolbar.setOnMenuItemClickListener(menuListener);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        progressBar = findViewById(R.id.progress_bar);

        civCarPhoto = findViewById(R.id.civ_car_photo);
        civCarPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSelectImageClick(view);
            }
        });

        etCarBrand = findViewById(R.id.input_car_brand_et);
        etCarModel = findViewById(R.id.input_car_model_et);
        etCarColor = findViewById(R.id.input_car_color_et);

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

        dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
        etCarPurchaseDate = findViewById(R.id.input_purchase_date_et);
        etCarPurchaseDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    initDateDialog().show();
                } catch (ParseException e) {
                    Log.e(LOG_TAG, "Failed to parse date value. Exception message: " + e.getMessage());
                }
            }
        });

        etCarNumber = findViewById(R.id.input_car_number_et);
        etCarVIN = findViewById(R.id.input_car_vin_et);
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

    public void carDataSet() {
        if (!car.getPhotoUri().equals(EXTRA_EMPTY)) {
            civCarPhoto.setImageURI(Uri.parse(car.getPhotoUri()));
        }
        etCarBrand.setText(car.getBrand());
        etCarModel.setText(car.getModel());
        etCarColor.setText(car.getColor().equals(EXTRA_EMPTY) ? "" : car.getColor());
        spCarYearIssue.setText(new SimpleDateFormat("yyyy", Locale.getDefault()).format(car.getYearIssue()));
        etCarMileage.setText(String.valueOf(car.getMileage()));
        spCarFuelType.setText(car.getFuelType());
        etCarPurchaseDate.setText(car.getPurchaseDate() != null ?
                new SimpleDateFormat("dd.MM.yy", Locale.getDefault())
                        .format(car.getPurchaseDate()) : "");
        etCarNumber.setText(car.getNumber().equals(EXTRA_EMPTY) ? "" : car.getNumber());
        etCarVIN.setText(car.getVin().equals(EXTRA_EMPTY) ? "" : car.getVin());
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    public void onSelectImageClick(View view) {
        CropImage.activity(null)
                //.setCropMenuCropButtonIcon(R.drawable.baseline_done_white_48) // Установка иконки "принять(обрезать)"

                .setGuidelines(CropImageView.Guidelines.ON_TOUCH)
                .setCropShape(CropImageView.CropShape.OVAL)
                .setAspectRatio(1, 1)

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

    private android.support.v7.app.AlertDialog initDateDialog() throws ParseException {
        LayoutInflater inflater = getLayoutInflater();
        final ViewGroup parent = null;
        final View dateView = inflater.inflate(R.layout.custom_date_picker_dialog_car_edit, parent);

        initDatePicker(dateView);
        setDatePickersData();

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        builder.setView(dateView);
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
                etCarPurchaseDate.setText(dateFormat.format(newDate.getTime()));
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        return builder.create();
    }

    private void initDatePicker(View datePickerView) {
        datePicker = datePickerView.findViewById(R.id.dp_date);
        datePicker.setSpinnersShown(true);
    }

    private void setDatePickersData() throws ParseException {
        Calendar calendar = Calendar.getInstance();
        if (etCarPurchaseDate.getText().toString().equals("")) {
            return;
        }
        calendar.setTime(dateFormat.parse(etCarPurchaseDate.getText().toString()));
        datePicker.updateDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_car, menu);
        return true;
    }

    private Toolbar.OnMenuItemClickListener menuListener =
            new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(@NonNull MenuItem menuItem) {
                    if (menuItem.getItemId() == R.id.btn_save) {
                        progressBar.setVisibility(View.VISIBLE);
                        onSave();
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                    return true;
                }
            };

    public void onSave() {
        String photoUri = imageUri != null ? imageUri.toString() : car.getPhotoUri();
        String brand = etCarBrand.getText().toString().trim();
        String model = etCarModel.getText().toString().trim();
        String name = brand + ' ' + model;
        String color = etCarColor.getText().toString().trim();
        String yearIssue = spCarYearIssue.getText().toString().trim();
        String mileage = etCarMileage.getText().toString().trim();
        String fuelType = spCarFuelType.getText().toString().trim();
        Date purchaseDate = null;
        if (!etCarPurchaseDate.getText().toString().isEmpty()) {
            try {
                purchaseDate = new SimpleDateFormat("dd.MM.yy", Locale.getDefault())
                        .parse(etCarPurchaseDate.getText().toString());
                Log.w(LOG_TAG, purchaseDate.toString());
            } catch (ParseException e) {
                Log.w(LOG_TAG, e.getMessage());
            }
        }
        Date dateIssue = null;
        try {
            dateIssue = new SimpleDateFormat("yyyy", Locale.getDefault()).parse(yearIssue);
        } catch (ParseException e) {
            Log.w(LOG_TAG, e.getMessage());
        }
        String number = etCarNumber.getText().toString().trim();
        String vin = etCarVIN.getText().toString().trim();


        if (!validateInputs(brand, model, yearIssue, mileage, fuelType)) {
            return;
        }

        CollectionReference dbCars;
        dbCars = db
                .collection(COLLECTION_USERS)
                .document(firebaseUser.getUid())
                .collection(COLLECTION_CARS);

        Car c = new Car(
                car.getIdDoc(),
                photoUri,
                brand,
                model,
                name,
                color.isEmpty() ? EXTRA_EMPTY : color,
                dateIssue,
                Integer.parseInt(mileage),
                fuelType,
                purchaseDate,
                number.isEmpty() ? EXTRA_EMPTY : number,
                vin.isEmpty() ? EXTRA_EMPTY : vin,
                car.getDateAdd());

        dbCars.document(car.getIdDoc())
                .set(c);


        Intent returnIntent = new Intent();
        returnIntent.putExtra(EXTRA_CAR, c);
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
}
