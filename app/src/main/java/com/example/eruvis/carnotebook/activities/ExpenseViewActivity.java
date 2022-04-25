package com.example.eruvis.carnotebook.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.eruvis.carnotebook.Common;
import com.example.eruvis.carnotebook.R;
import com.example.eruvis.carnotebook.models.Expense;

import java.text.SimpleDateFormat;
import java.util.Locale;

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

public class ExpenseViewActivity extends AppCompatActivity {
    private static final String LOG_TAG = ExpenseViewActivity.class.getSimpleName();

    private Toolbar toolbar;
    private ImageView ivBg;
    private LinearLayout layoutVolumeAndPrice;
    private TextInputLayout inputPrice, inputFuelType, inputFuelGrade;
    private EditText etDateTime, etMileage, etAmount, etVolume, etPrice, spFuelType, spFuelGrade, etEventNote;
    private TextView tvTypeExpense;

    private Expense expense;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_view);

        expense = (Expense) getIntent().getSerializableExtra(EXTRA_EXPENSE);

        initControls();
        checkTypeExpense();
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

        ivBg = findViewById(R.id.iv_bg);

        layoutVolumeAndPrice = findViewById(R.id.ll_volume_and_price_per);

        tvTypeExpense = findViewById(R.id.tv_type_expense);

        inputPrice = findViewById(R.id.input_price_per);
        inputFuelType = findViewById(R.id.input_fuel_type);
        inputFuelGrade = findViewById(R.id.input_fuel_grade);

        etDateTime = findViewById(R.id.input_date_time_et);
        etMileage = findViewById(R.id.input_mileage_et);
        etAmount = findViewById(R.id.input_amount_et);
        etVolume = findViewById(R.id.input_volume_et);
        etPrice = findViewById(R.id.input_price_per_et);
        spFuelType = findViewById(R.id.input_fuel_type_et);
        spFuelGrade = findViewById(R.id.input_fuel_grade_et);
        etEventNote = findViewById(R.id.input_event_note_et);
    }

    public void expenseDataSet() {
        etDateTime.setText(new SimpleDateFormat("dd.MM.yy HH:mm", Locale.getDefault()).format(expense.getDate()));

        String mileage = String.valueOf(expense.getMileage());
        switch (Common.prefDistanceUnits) {
            case EXTRA_KM:
                mileage += " " + getString(R.string.kilometers);
                break;
            case EXTRA_MIL:
                mileage += " " + getString(R.string.miles);
                break;
        }
        etMileage.setText(mileage);

        String amount = String.valueOf(expense.getAmount());
        String price = String.valueOf(expense.getPrice());
        switch (Common.prefCurrencyUnit) {
            case EXTRA_RUB:
                amount += " " + getString(R.string.rub);
                price += " " + getString(R.string.rub);
                break;
            case EXTRA_USD:
                amount += " " + getString(R.string.usd);
                price += " " + getString(R.string.usd);
                break;
            case EXTRA_UAH:
                amount += " " + getString(R.string.uah);
                price += " " + getString(R.string.uah);
                break;
        }
        etAmount.setText(amount);
        etPrice.setText(price);

        if (expense.getType().equals(EXTRA_TYPE_REFUELING)) {
            layoutVolumeAndPrice.setVisibility(View.VISIBLE);
            if (expense.getFuelType().equals(getString(R.string.gas))) {
                inputFuelType.setVisibility(View.VISIBLE);
                spFuelType.setText(expense.getFuelType());
            } else {
                inputFuelGrade.setVisibility(View.VISIBLE);
                spFuelGrade.setText(expense.getFuelGrade());
            }

            String volume = String.valueOf(expense.getVolume());
            switch (Common.prefFuelUnits) {
                case EXTRA_LIT:
                    volume += " " + getString(R.string.liters_l);
                    inputPrice.setHint(getString(R.string.expense_price_per_hint) + " " + (getString(R.string.liters)));
                    break;
                case EXTRA_GAL:
                    volume += " " + getString(R.string.gallons_g);
                    inputPrice.setHint(getString(R.string.expense_price_per_hint) + " " + (getString(R.string.gallons)));
                    break;
            }
            etVolume.setText(volume);
        }

        etEventNote.setText(expense.getNote().equals(EXTRA_EMPTY) ? "-" : expense.getNote());
    }

    private void checkTypeExpense() {
        Log.w(LOG_TAG, expense.getType());
        switch (expense.getType()) {
            case EXTRA_TYPE_REFUELING:
                tvTypeExpense.setText(getString(R.string.fab_refueling_label));
                layoutVolumeAndPrice.setVisibility(View.VISIBLE);
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

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }
}
