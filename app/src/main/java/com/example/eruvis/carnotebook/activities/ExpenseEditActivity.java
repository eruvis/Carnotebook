package com.example.eruvis.carnotebook.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.eruvis.carnotebook.Common;
import com.example.eruvis.carnotebook.R;
import com.example.eruvis.carnotebook.fragments.SpinnerFragment;
import com.example.eruvis.carnotebook.models.Expense;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

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
import static com.example.eruvis.carnotebook.AppConst.COLLECTION_EXPENSES;
import static com.example.eruvis.carnotebook.AppConst.COLLECTION_USERS;
import static com.example.eruvis.carnotebook.AppConst.EXTRA_EMPTY;
import static com.example.eruvis.carnotebook.AppConst.EXTRA_EXPENSE;
import static com.example.eruvis.carnotebook.AppConst.EXTRA_GAL;
import static com.example.eruvis.carnotebook.AppConst.EXTRA_KM;
import static com.example.eruvis.carnotebook.AppConst.EXTRA_LIT;
import static com.example.eruvis.carnotebook.AppConst.EXTRA_MIL;
import static com.example.eruvis.carnotebook.AppConst.EXTRA_RUB;
import static com.example.eruvis.carnotebook.AppConst.EXTRA_TYPE_ACTIVITY;
import static com.example.eruvis.carnotebook.AppConst.EXTRA_TYPE_FINE;
import static com.example.eruvis.carnotebook.AppConst.EXTRA_TYPE_OTHER;
import static com.example.eruvis.carnotebook.AppConst.EXTRA_TYPE_PARKING;
import static com.example.eruvis.carnotebook.AppConst.EXTRA_TYPE_REFUELING;
import static com.example.eruvis.carnotebook.AppConst.EXTRA_TYPE_SERVICE;
import static com.example.eruvis.carnotebook.AppConst.EXTRA_TYPE_SPARES;
import static com.example.eruvis.carnotebook.AppConst.EXTRA_TYPE_WASH;
import static com.example.eruvis.carnotebook.AppConst.EXTRA_UAH;
import static com.example.eruvis.carnotebook.AppConst.EXTRA_USD;

public class ExpenseEditActivity extends AppCompatActivity {
    private static final String LOG_TAG = ExpenseEditActivity.class.getSimpleName();

    private Toolbar toolbar;
    private ProgressBar progressBar;

    private ImageView ivBg;
    private TextView tvTypeExpense;
    private LinearLayout layoutVolumeAndPrice;
    private TextInputLayout inputMileage, inputAmount, inputVolume, inputPrice,  inputFuelType, inputFuelGrade;
    private EditText etDateTime, etMileage, etAmount, etVolume, etPrice, spFuelType, spFuelGrade, etEventNote;

    private DatePicker datePicker;
    private TimePicker timePicker;
    private DateFormat dateFormat;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseFirestore db;

    Expense expense;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_add);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        KeyboardVisibilityEvent.setEventListener(this, new KeyboardVisibilityEventListener() {
            @Override
            public void onVisibilityChanged(boolean isOpen) {
                if (!isOpen && getCurrentFocus() != null) getCurrentFocus().clearFocus();
            }
        });

        expense = (Expense) getIntent().getSerializableExtra(EXTRA_EXPENSE);

        initControls();
        updateUI();
        expenseDataSet();
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
        ivBg = findViewById(R.id.iv_bg);

        dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault());

        tvTypeExpense = findViewById(R.id.tv_type_expense);

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        layoutVolumeAndPrice = findViewById(R.id.ll_volume_and_price_per);

        inputMileage = findViewById(R.id.input_mileage);
        inputAmount = findViewById(R.id.input_amount);
        inputVolume = findViewById(R.id.input_volume);
        inputPrice = findViewById(R.id.input_price_per);
        inputFuelType = findViewById(R.id.input_fuel_type);
        inputFuelGrade = findViewById(R.id.input_fuel_grade);

        etDateTime = findViewById(R.id.input_date_time_et);
        etDateTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    initDateTimeDialog().show();
                } catch (ParseException e) {
                    Log.e(LOG_TAG, "Failed to parse date value. Exception message: " + e.getMessage());
                }
            }
        });

        etMileage = findViewById(R.id.input_mileage_et);
        etAmount = findViewById(R.id.input_amount_et);
        etVolume = findViewById(R.id.input_volume_et);
        etVolume.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.w(LOG_TAG, String.valueOf(count) + ' ' + start + ' ' + before + ' ' + s);
                if ((s.equals(",") || s.equals(".")) && count == 1) {
                    etVolume.setText("");
                }
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        etPrice = findViewById(R.id.input_price_per_et);

        spFuelType = findViewById(R.id.input_fuel_type_et);
        spFuelType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomSpinnerFuelType(v);
            }
        });
        spFuelType.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                spFuelType.setError(null);
                checkFuelType();
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        spFuelGrade = findViewById(R.id.input_fuel_grade_et);
        spFuelGrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomSpinnerFuelGrade(v);
            }
        });
        spFuelGrade.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                spFuelGrade.setError(null);
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        etEventNote = findViewById(R.id.input_event_note_et);
    }

    private android.support.v7.app.AlertDialog initDateTimeDialog() throws ParseException {
        LayoutInflater inflater = getLayoutInflater();
        final ViewGroup parent = null;
        View dateTimeView = inflater.inflate(R.layout.custom_date_picker_dialog_expense_add, parent);

        initDatePicker(dateTimeView);
        initTimePicker(dateTimeView);
        setDateTimePickersData();

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        builder.setView(dateTimeView);
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Calendar newDate = Calendar.getInstance();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    newDate.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(),
                            timePicker.getHour(), timePicker.getMinute());
                } else {
                    newDate.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(),
                            timePicker.getCurrentHour(), timePicker.getCurrentMinute());
                }
                etDateTime.setText(dateFormat.format(newDate.getTime()));
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

    private void initDatePicker(View dateTimePickerView) {
        datePicker = dateTimePickerView.findViewById(R.id.expense_edit_date_dp);
        datePicker.setSpinnersShown(true);
    }

    private void initTimePicker(View dateTimePickerView) {
        timePicker = dateTimePickerView.findViewById(R.id.expense_edit_time_tp);
        timePicker.setIs24HourView(true);
    }

    private void setDateTimePickersData() throws ParseException {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateFormat.parse(etDateTime.getText().toString()));
        datePicker.updateDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            timePicker.setHour(calendar.get(Calendar.HOUR_OF_DAY));
            timePicker.setMinute(calendar.get(Calendar.MINUTE));
        } else {
            timePicker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
            timePicker.setCurrentMinute(calendar.get(Calendar.MINUTE));
        }
    }

    public void expenseDataSet() {
        etDateTime.setText(new SimpleDateFormat("dd.MM.yy HH:mm", Locale.getDefault()).format(expense.getDate()));
        etMileage.setText(String.valueOf(expense.getMileage()));
        etAmount.setText(String.valueOf(expense.getAmount()));
        etEventNote.setText(expense.getNote().equals(EXTRA_EMPTY) ? "" : expense.getNote());

        if (expense.getType().equals(EXTRA_TYPE_REFUELING)) {
            etVolume.setText(String.valueOf(expense.getVolume()));
            etPrice.setText(String.valueOf(expense.getPrice()));
            spFuelType.setText(expense.getFuelType());
            spFuelGrade.setText(expense.getFuelGrade().equals(EXTRA_EMPTY) ? "" : expense.getFuelGrade());
        }
    }

    public void showCustomSpinnerFuelType(View v) {
        int titleId = R.string.expense_fuel_type_hint;
        int listId = R.array.fuel_type_array_two;
        int editTextId = R.id.input_fuel_type_et;
        SpinnerFragment spinnerFragment = SpinnerFragment.newInstance(titleId, listId, null, editTextId);
        spinnerFragment.show(getSupportFragmentManager(), "customSpinner");
    }

    public void showCustomSpinnerFuelGrade(View v) {
        int titleId = R.string.expense_fuel_grade_hint;
        int listId = Common.selectedCarFuelType.equals(getString(R.string.diesel))
                ? R.array.diesel_type_array : R.array.gasoline_type_array;
        int editTextId = R.id.input_fuel_grade_et;
        SpinnerFragment spinnerFragment = SpinnerFragment.newInstance(titleId, listId, null, editTextId);
        spinnerFragment.show(getSupportFragmentManager(), "customSpinner");
    }

    private void updateUI() {
        checkTypeExpense();

        switch (Common.prefDistanceUnits) {
            case EXTRA_KM:
                inputMileage.setHint(getString(R.string.expense_mileage_hint) + ", " + getString(R.string.kilometers));
                break;
            case EXTRA_MIL:
                inputMileage.setHint(getString(R.string.expense_mileage_hint) + ", " + getString(R.string.miles));
                break;
        }
        switch (Common.prefCurrencyUnit) {
            case EXTRA_RUB:
                inputAmount.setHint(getString(R.string.expense_amount_hint) + ", " + getString(R.string.rub));
                break;
            case EXTRA_USD:
                inputAmount.setHint(getString(R.string.expense_amount_hint) + ", " + getString(R.string.usd));
                break;
            case EXTRA_UAH:
                inputAmount.setHint(getString(R.string.expense_amount_hint) + ", " + getString(R.string.uah));
                break;
        }
        switch (Common.prefFuelUnits) {
            case EXTRA_LIT:
                inputVolume.setHint(getString(R.string.expense_volume_hint) + ", " + (getString(R.string.liters_l)));
                inputPrice.setHint(getString(R.string.expense_price_per_hint) + " " + (getString(R.string.liters)));
                break;
            case EXTRA_GAL:
                inputVolume.setHint(getString(R.string.expense_volume_hint) + ", " + (getString(R.string.gallons_g)));
                inputPrice.setHint(getString(R.string.expense_price_per_hint) + " " + (getString(R.string.gallons)));
                break;
        }
    }

    private void checkTypeExpense() {
        switch (expense.getType()) {
            case EXTRA_TYPE_REFUELING:
                tvTypeExpense.setText(getString(R.string.fab_refueling_label));
                layoutVolumeAndPrice.setVisibility(View.VISIBLE);

                if (Common.selectedCarFuelType.equals(getString(R.string.gasoline_plus_gas))) {
                    inputFuelType.setVisibility(View.VISIBLE);
                } else {
                    if (!Common.selectedCarFuelType.equals(getString(R.string.gas))) {
                        inputFuelGrade.setVisibility(View.VISIBLE);
                    }
                }
                break;

            case EXTRA_TYPE_WASH:
                tvTypeExpense.setText(getString(R.string.fab_wash_label));
                ivBg.setImageResource(R.drawable.img_bg_car_wash_expense);
                break;

            case EXTRA_TYPE_SERVICE:
                tvTypeExpense.setText(getString(R.string.fab_service_label));
                ivBg.setImageResource(R.drawable.img_bg_service_expense);
                break;

            case EXTRA_TYPE_PARKING:
                tvTypeExpense.setText(getString(R.string.fab_parking_label));
                ivBg.setImageResource(R.drawable.img_bg_parking_expense);
                break;

            case EXTRA_TYPE_FINE:
                tvTypeExpense.setText(getString(R.string.fab_fine_label));
                ivBg.setImageResource(R.drawable.img_bg_fine_expense);
                break;

            case EXTRA_TYPE_SPARES:
                tvTypeExpense.setText(getString(R.string.fab_spares_label));
                ivBg.setImageResource(R.drawable.img_bg_spares_expense);
                break;

            case EXTRA_TYPE_OTHER:
                tvTypeExpense.setText(getString(R.string.fab_other_label));
                ivBg.setImageResource(R.drawable.img_bg_other_expense);
                break;
        }
    }

    private void checkFuelType() {
        if (spFuelType.getText().toString().equals(getString(R.string.gasoline))) {
            inputFuelGrade.setVisibility(View.VISIBLE);
        } else {
            inputFuelGrade.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_expense, menu);
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
        String type = expense.getType();
        String date = etDateTime.getText().toString().trim();
        String mileage = etMileage.getText().toString().trim();
        String amount = etAmount.getText().toString().trim().replace(",", ".");
        String volume = etVolume.getText().toString().trim().replace(",", ".");
        String price = etPrice.getText().toString().trim().replace(",", ".");
        String fuelType = Common.selectedCarFuelType.equals(getString(R.string.gasoline_plus_gas)) ?
                spFuelType.getText().toString().trim() : Common.selectedCarFuelType;
        String fuelGrade = spFuelGrade.getText().toString().trim();
        String note = etEventNote.getText().toString().trim();

        if (!validateInputs(mileage, amount, volume, price, fuelType, fuelGrade)) {
            return;
        }

        Date dateTime = null;
        try {
            dateTime = new SimpleDateFormat("dd.MM.yy HH:mm", Locale.getDefault()).parse(date);
        } catch (ParseException e) {
            Log.w(LOG_TAG, e.getMessage());
        }

        fuelGrade = fuelGrade.isEmpty() ? EXTRA_EMPTY : fuelGrade;
        if (Common.selectedCarFuelType.equals(getString(R.string.gasoline_plus_gas))) {
            if (fuelType.equals(getString(R.string.gas))) {
                fuelGrade = EXTRA_EMPTY;
            }
        }

        final Expense e = new Expense(
                expense.getIdDoc(),
                type,
                dateTime,
                Integer.parseInt(mileage),
                Double.parseDouble(amount),
                type.equals(EXTRA_TYPE_REFUELING) ? Double.parseDouble(volume) : 0,
                type.equals(EXTRA_TYPE_REFUELING) ? Double.parseDouble(price) : 0,
                type.equals(EXTRA_TYPE_REFUELING) ? fuelType : EXTRA_EMPTY,
                type.equals(EXTRA_TYPE_REFUELING) ? fuelGrade : EXTRA_EMPTY,
                note.isEmpty() ? EXTRA_EMPTY : note,
                expense.getDateAdd()
        );

        db.collection(COLLECTION_USERS)
                .document(firebaseUser.getUid())
                .collection(COLLECTION_CARS)
                .document(Common.selectedCarIdDoc)
                .collection(COLLECTION_EXPENSES)
                .document(expense.getIdDoc())
                .set(e);


        Intent returnIntent = new Intent();
        returnIntent.putExtra(EXTRA_EXPENSE, e);
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    public boolean validateInputs(String mileage, String amount, String volume,
                                  String price, String fuelType, String fuelGrade) {
        boolean valid = true;

        String fuelTypeHint = getText(R.string.expense_fuel_type_hint).toString();
        String fuelGradeHint = getText(R.string.expense_fuel_grade_hint).toString();

        etMileage.setError(null);
        etAmount.setError(null);

        if (TextUtils.isEmpty(mileage)) {
            etMileage.setError(getText(R.string.er_fill_field));
            valid = false;
        }

        if (TextUtils.isEmpty(amount)) {
            etAmount.setError(getText(R.string.er_fill_field));
            valid = false;
        }

        if (expense.getType().equals(EXTRA_TYPE_REFUELING)) {
            etVolume.setError(null);
            etPrice.setError(null);
            if (TextUtils.isEmpty(volume)) {
                etVolume.setError(getText(R.string.er_fill_field));
                valid = false;
            }

            if (TextUtils.isEmpty(price)) {
                etPrice.setError(getText(R.string.er_fill_field));
                valid = false;
            }

            if (Common.selectedCarFuelType.equals(getString(R.string.gasoline_plus_gas))) {
                spFuelType.setError(null);
                if (TextUtils.isEmpty(fuelType) || fuelGrade.equals(fuelTypeHint)) {
                    spFuelType.setError(getText(R.string.er_fill_field));
                    valid = false;
                }

                if (!spFuelType.getText().toString().isEmpty() && !spFuelType.getText().toString().equals(getString(R.string.gas))) {
                    spFuelGrade.setError(null);
                    if (TextUtils.isEmpty(fuelGrade) || fuelGrade.equals(fuelGradeHint)) {
                        spFuelGrade.setError(getText(R.string.er_fill_field));
                        valid = false;
                    }
                }

            } else {
                spFuelGrade.setError(null);
                if (!Common.selectedCarFuelType.equals(getString(R.string.gas))) {
                    if (TextUtils.isEmpty(fuelGrade) || fuelGrade.equals(fuelGradeHint)) {
                        spFuelGrade.setError(getText(R.string.er_fill_field));
                        valid = false;
                    }
                }
            }
        }

        return valid;
    }
}
