package com.example.eruvis.carnotebook.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.eruvis.carnotebook.Common;
import com.example.eruvis.carnotebook.GenerateRandom;
import com.example.eruvis.carnotebook.R;
import com.example.eruvis.carnotebook.fragments.SpinnerFragment;
import com.example.eruvis.carnotebook.models.Expense;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import static com.example.eruvis.carnotebook.AppConst.LAST_REFUELING_FUEL_GRADE_KEY;
import static com.example.eruvis.carnotebook.AppConst.LAST_REFUELING_FUEL_PRICE_KEY;
import static com.example.eruvis.carnotebook.AppConst.LAST_REFUELING_FUEL_TYPE_KEY;

public class ExpenseAddActivity extends AppCompatActivity {
    private static final String LOG_TAG = ExpenseAddActivity.class.getSimpleName();

    private Toolbar toolbar;
    private ProgressBar progressBar;

    private ImageView ivBg;
    private TextView tvTypeExpense;
    private LinearLayout layoutVolumeAndPrice;
    private TextInputLayout inputMileage, inputAmount, inputVolume, inputPrice, inputFuelType, inputFuelGrade;
    private EditText etDateTime, etMileage, etAmount, etVolume, etPrice, spFuelType, spFuelGrade, etEventNote;

    private DatePicker datePicker;
    private TimePicker timePicker;
    private DateFormat dateFormat;

    private SharedPreferences sharedPreferences;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore db;

    private String typeActivity = EXTRA_TYPE_OTHER;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_add);

        KeyboardVisibilityEvent.setEventListener(this, new KeyboardVisibilityEventListener() {
            @Override
            public void onVisibilityChanged(boolean isOpen) {
                if (!isOpen && getCurrentFocus() != null) getCurrentFocus().clearFocus();
            }
        });

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);


        initControls();
        updateUI();
        if (typeActivity.equals(EXTRA_TYPE_REFUELING)) {
            setLastRefuelingData();
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_expense, menu);
        return true;
    }

    private void initControls() {
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
        etDateTime.setText(dateFormat.format(new Date()));
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
        etAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validEditText(s, etAmount);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        etAmount.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    changeEditTextAmount();
                }
                return false;
            }
        });
        etAmount.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                changeEditTextAmount();
            }
        });

        etVolume = findViewById(R.id.input_volume_et);
        etVolume.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validEditText(s, etVolume);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        etVolume.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    changeEditTextVolume();
                }
                return false;
            }
        });
        etVolume.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                changeEditTextVolume();
            }
        });

        etPrice = findViewById(R.id.input_price_per_et);
        etPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validEditText(s, etPrice);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        etPrice.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    changeEditTextPrice();
                }
                return false;
            }
        });
        etPrice.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    changeEditTextPrice();
                }
            }
        });

        spFuelType = findViewById(R.id.input_fuel_type_et);
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
        spFuelType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomSpinnerFuelType(v);
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

    private void changeEditTextAmount() {
        String sAmount = etAmount.getText().toString();
        if (sAmount.length() != 0) {
            double amount = Double.parseDouble(sAmount);
            if (amount != 0) {
                String sVolume = etVolume.getText().toString();
                String sPrice = etPrice.getText().toString();

                if (sPrice.length() != 0) {
                    double price = Double.parseDouble(sPrice);
                    if (price != 0) {
                        double volume = amount / price;
                        etVolume.setText(String.format(Locale.getDefault(), "%.2f", volume));
                    }
                } else if (sVolume.length() != 0) {
                    double volume = Double.parseDouble(sVolume);
                    if (volume != 0) {
                        double price = amount / volume;
                        etPrice.setText(String.format(Locale.getDefault(), "%.2f", price));
                    }
                }
            }
        }
    }

    private void changeEditTextVolume() {
        String sVolume = etVolume.getText().toString();
        if (sVolume.length() != 0) {
            double volume = Double.parseDouble(sVolume);
            if (volume != 0) {
                String sAmount = etAmount.getText().toString();
                String sPrice = etPrice.getText().toString();

                if (sPrice.length() != 0) {
                    double price = Double.parseDouble(sPrice);
                    if (price != 0) {
                        double amount = volume * price;
                        etAmount.setText(String.format(Locale.getDefault(), "%.2f", amount));
                    }
                } else if (sAmount.length() != 0) {
                    double amount = Double.parseDouble(sAmount);
                    if (amount != 0) {
                        double price = amount / volume;
                        etPrice.setText(String.format(Locale.getDefault(), "%.2f", price));
                    }
                }
            }
        }
    }

    private void changeEditTextPrice() {
        String sPrice = etPrice.getText().toString();
        if (sPrice.length() != 0) {
            double price = Double.parseDouble(sPrice);
            if (price != 0) {
                String sAmount = etAmount.getText().toString();
                String sVolume = etVolume.getText().toString();

                if (sVolume.length() != 0) {
                    double volume = Double.parseDouble(sVolume);
                    if (volume != 0) {
                        double amount = volume * price;
                        etAmount.setText(String.format(Locale.getDefault(), "%.2f", amount));
                    }
                } else if (sAmount.length() != 0) {
                    double amount = Double.parseDouble(sAmount);
                    if (amount != 0) {
                        double volume = amount / price;
                        etVolume.setText(String.format(Locale.getDefault(), "%.2f", volume));
                    }
                }
            }
        }
    }

    private void setLastRefuelingData() {
        if (Common.prefLastRefuelingFuelType) {
            if (Common.selectedCarFuelType.equals(getString(R.string.gasoline_plus_gas))
                    && Common.lastRefuelingFuelType != null) {
                spFuelType.setText(Common.lastRefuelingFuelType);
                if (Common.lastRefuelingFuelType.equals(getString(R.string.gasoline))) {
                    spFuelGrade.setText(Common.lastRefuelingFuelGrade);
                }
            } else if (!Common.selectedCarFuelType.equals(getString(R.string.gas))
                    && Common.lastRefuelingFuelGrade != null) {
                if (Common.lastRefuelingFuelType.equals(Common.selectedCarFuelType)) {
                    spFuelGrade.setText(Common.lastRefuelingFuelGrade);
                }
            }
        }
        if (Common.prefLastRefuelingFuelPrice && Common.lastRefuelingFuelPrice != null) {
            etPrice.setText(Common.lastRefuelingFuelPrice);
        }
    }

    private void saveLastRefuelingData() {
        if (Common.selectedCarFuelType.equals(getString(R.string.gasoline_plus_gas))) {
            Common.lastRefuelingFuelType = spFuelType.getText().toString();
            if (Common.lastRefuelingFuelType.equals(getString(R.string.gasoline))) {
                Common.lastRefuelingFuelGrade = spFuelGrade.getText().toString();
            }
        } else if (!Common.selectedCarFuelType.equals(getString(R.string.gas))) {
            Common.lastRefuelingFuelType = Common.selectedCarFuelType;
            Common.lastRefuelingFuelGrade = spFuelGrade.getText().toString();
        }
        Common.lastRefuelingFuelPrice = etPrice.getText().toString();

        savePreference();
    }

    private void savePreference() {
        sharedPreferences.edit().putString(LAST_REFUELING_FUEL_TYPE_KEY, Common.lastRefuelingFuelType).apply();
        sharedPreferences.edit().putString(LAST_REFUELING_FUEL_GRADE_KEY, Common.lastRefuelingFuelGrade).apply();
        sharedPreferences.edit().putString(LAST_REFUELING_FUEL_PRICE_KEY, Common.lastRefuelingFuelPrice).apply();
    }

    private void validEditText(CharSequence s, EditText et) {
        int len = s.length();

        if (len != 0) {
            if (s.toString().equals(".") || s.toString().equals(",")) {
                et.setText(null);
                return;
            }

            if (s.toString().indexOf(',') != -1) {
                et.setText(s.toString().replace(",", "."));
                et.setSelection(et.getText().toString().length());
            } else if (s.toString().indexOf('.') != -1) {
                int lenPoint = lenPoint = len - s.toString().replace(".", "").length();
                //Log.w(LOG_TAG, "string: " + s.toString() + " length: " + len + " count point: " + lenPoint);
                if (lenPoint > 1) {
                    len--;
                    et.setText(new StringBuilder(s)
                            .deleteCharAt(s.toString().indexOf("."))
                            .subSequence(0, len));
                    et.setSelection(len);
                }
            }
        }
    }

    private void checkTypeExpense() {
        typeActivity = getIntent().getStringExtra(EXTRA_TYPE_ACTIVITY);

        switch (typeActivity) {
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

    private Toolbar.OnMenuItemClickListener menuListener =
            new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(@NonNull MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
                        case R.id.btn_add_expense:
                            progressBar.setVisibility(View.VISIBLE);
                            onAddExpense();
                            progressBar.setVisibility(View.INVISIBLE);
                            break;
                    }
                    return true;
                }
            };

    public void onAddExpense() {
        String type = typeActivity;
        String date = etDateTime.getText().toString().trim();
        String mileage = etMileage.getText().toString().trim();
        String amount = etAmount.getText().toString().trim().replace(",", ".");
        String volume = etVolume.getText().toString().trim().replace(",", ".");
        String price = etPrice.getText().toString().trim().replace(",", ".");
        String fuelType = Common.selectedCarFuelType.equals(getString(R.string.gasoline_plus_gas)) ?
                spFuelType.getText().toString() : Common.selectedCarFuelType;
        String fuelGrade = spFuelGrade.getText().toString();
        String note = etEventNote.getText().toString().trim();
        Date dateAdd = Calendar.getInstance().getTime();

        if (!validateInputs(mileage, amount, volume, price, fuelType, fuelGrade)) {
            return;
        }

        Date dateTime = null;
        try {
            dateTime = new SimpleDateFormat("dd.MM.yy HH:mm", Locale.getDefault()).parse(date);
            Log.w(LOG_TAG, dateTime.toString());
        } catch (ParseException e) {
            Log.w(LOG_TAG, e.getMessage());
        }

        final String documentName = GenerateRandom.randomString(14) +
                new SimpleDateFormat("ddMMyyyyHHmmss", Locale.getDefault())
                        .format(Calendar.getInstance().getTime());

        fuelGrade = fuelGrade.isEmpty() ? EXTRA_EMPTY : fuelGrade;
        if (Common.selectedCarFuelType.equals(getString(R.string.gasoline_plus_gas))) {
            if (fuelType.equals(getString(R.string.gas))) {
                fuelGrade = EXTRA_EMPTY;
            }
        }

        if (typeActivity.equals(EXTRA_TYPE_REFUELING)) {
            saveLastRefuelingData();
        }

        final Expense expense = new Expense(
                documentName,
                type,
                dateTime,
                Integer.parseInt(mileage),
                Double.parseDouble(amount),
                typeActivity.equals(EXTRA_TYPE_REFUELING) ? Double.parseDouble(volume) : 0,
                typeActivity.equals(EXTRA_TYPE_REFUELING) ? Double.parseDouble(price) : 0,
                typeActivity.equals(EXTRA_TYPE_REFUELING) ? fuelType : EXTRA_EMPTY,
                typeActivity.equals(EXTRA_TYPE_REFUELING) ? fuelGrade : EXTRA_EMPTY,
                note.isEmpty() ? EXTRA_EMPTY : note,
                dateAdd);


        final CollectionReference dbExpenses;
        dbExpenses = db
                .collection(COLLECTION_USERS)
                .document(firebaseUser.getUid())
                .collection(COLLECTION_CARS)
                .document(Common.selectedCarIdDoc)
                .collection(COLLECTION_EXPENSES);

        dbExpenses.document(documentName).set(expense).addOnSuccessListener(new OnSuccessListener<Void>() {
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
        returnIntent.putExtra(EXTRA_EXPENSE, expense);
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

        if (typeActivity.equals(EXTRA_TYPE_REFUELING)) {
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
