package com.example.eruvis.carnotebook.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.eruvis.carnotebook.Common;
import com.example.eruvis.carnotebook.activities.ExpenseViewActivity;
import com.example.eruvis.carnotebook.models.Expense;
import com.example.eruvis.carnotebook.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import static com.example.eruvis.carnotebook.AppConst.EXTRA_EXPENSE;
import static com.example.eruvis.carnotebook.AppConst.EXTRA_GAL;
import static com.example.eruvis.carnotebook.AppConst.EXTRA_KM;
import static com.example.eruvis.carnotebook.AppConst.EXTRA_LIT;
import static com.example.eruvis.carnotebook.AppConst.EXTRA_MIL;
import static com.example.eruvis.carnotebook.AppConst.EXTRA_RUB;
import static com.example.eruvis.carnotebook.AppConst.EXTRA_TYPE_FINE;
import static com.example.eruvis.carnotebook.AppConst.EXTRA_TYPE_OTHER;
import static com.example.eruvis.carnotebook.AppConst.EXTRA_TYPE_PARKING;
import static com.example.eruvis.carnotebook.AppConst.EXTRA_TYPE_REFUELING;
import static com.example.eruvis.carnotebook.AppConst.EXTRA_TYPE_SERVICE;
import static com.example.eruvis.carnotebook.AppConst.EXTRA_TYPE_SPARES;
import static com.example.eruvis.carnotebook.AppConst.EXTRA_TYPE_WASH;
import static com.example.eruvis.carnotebook.AppConst.EXTRA_UAH;
import static com.example.eruvis.carnotebook.AppConst.EXTRA_USD;

public class ExpensesRecyclerAdapter extends RecyclerView.Adapter<ExpensesRecyclerAdapter.ExpensesViewHolder>{

    private Context context;
    private List<Expense> expenseList;

    public ExpensesRecyclerAdapter(Context context, List<Expense> expenseList) {
        this.context = context;
        this.expenseList = expenseList;
    }

    private onLongItemClickListener mOnLongItemClickListener;

    public void setOnLongItemClickListener(onLongItemClickListener onLongItemClickListener) {
        mOnLongItemClickListener = onLongItemClickListener;
    }

    public interface onLongItemClickListener {
        void ItemLongClicked(View v, int position);
    }

    @NonNull
    @Override
    public ExpensesViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        return new ExpensesViewHolder(
                LayoutInflater.from(context).inflate(R.layout.item_expense, viewGroup, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull final ExpensesViewHolder expensesViewHolder, int position) {
        Expense expense = expenseList.get(expensesViewHolder.getAdapterPosition());

        switch (expense.getType()) {
            case EXTRA_TYPE_REFUELING:
                expensesViewHolder.ivEdge.setBackgroundColor(context.getResources()
                        .getColor(R.color.colorRefueling));
                expensesViewHolder.ivIcon.setColorFilter(context.getResources().getColor(R.color.colorRefueling));
                expensesViewHolder.ivIcon.setImageResource(R.drawable.ic_fuel);

                if (expense.getFuelType().equals(context.getResources().getString(R.string.gas))) {
                    expensesViewHolder.tvFuelGrade.setVisibility(View.GONE);
                    expensesViewHolder.tvFuelType.setVisibility(View.VISIBLE);
                    expensesViewHolder.tvFuelType.setText(expense.getFuelType());
                } else {
                    expensesViewHolder.tvFuelType.setVisibility(View.GONE);
                    expensesViewHolder.tvFuelGrade.setVisibility(View.VISIBLE);
                }
                expensesViewHolder.tvVolume.setVisibility(View.VISIBLE);

                String volume = String.valueOf(expense.getVolume());
                switch (Common.prefFuelUnits) {
                    case EXTRA_LIT:
                        volume += " " + context.getString(R.string.liters_l);
                        break;
                    case EXTRA_GAL:
                        volume += " " + context.getString(R.string.gallons_g);
                        break;
                }
                expensesViewHolder.tvVolume.setText(volume);
                expensesViewHolder.tvFuelGrade.setText(expense.getFuelGrade());

                expensesViewHolder.tvType.setText(context.getResources().getString(R.string.fab_refueling_label));
                break;
            case EXTRA_TYPE_WASH:
                expensesViewHolder.ivEdge.setBackgroundColor(context.getResources().getColor(R.color.colorCarWash));
                expensesViewHolder.ivIcon.setColorFilter(context.getResources().getColor(R.color.colorCarWash));
                expensesViewHolder.ivIcon.setImageResource(R.drawable.ic_car_wash);

                expensesViewHolder.tvType.setText(context.getResources().getString(R.string.fab_wash_label));

                expensesViewHolder.tvFuelType.setVisibility(View.GONE);
                expensesViewHolder.tvFuelGrade.setVisibility(View.GONE);
                expensesViewHolder.tvVolume.setVisibility(View.GONE);
                break;
            case EXTRA_TYPE_SERVICE:
                expensesViewHolder.ivEdge.setBackgroundColor(context.getResources().getColor(R.color.colorService));
                expensesViewHolder.ivIcon.setColorFilter(context.getResources().getColor(R.color.colorService));
                expensesViewHolder.ivIcon.setImageResource(R.drawable.ic_service);

                expensesViewHolder.tvType.setText(context.getResources().getString(R.string.fab_service_label));

                expensesViewHolder.tvFuelType.setVisibility(View.GONE);
                expensesViewHolder.tvFuelGrade.setVisibility(View.GONE);
                expensesViewHolder.tvVolume.setVisibility(View.GONE);
                break;
            case EXTRA_TYPE_PARKING:
                expensesViewHolder.ivEdge.setBackgroundColor(context.getResources().getColor(R.color.colorParking));
                expensesViewHolder.ivIcon.setColorFilter(context.getResources().getColor(R.color.colorParking));
                expensesViewHolder.ivIcon.setImageResource(R.drawable.ic_parking);

                expensesViewHolder.tvType.setText(context.getResources().getString(R.string.fab_parking_label));

                expensesViewHolder.tvFuelType.setVisibility(View.GONE);
                expensesViewHolder.tvFuelGrade.setVisibility(View.GONE);
                expensesViewHolder.tvVolume.setVisibility(View.GONE);
                break;
            case EXTRA_TYPE_FINE:
                expensesViewHolder.ivEdge.setBackgroundColor(context.getResources().getColor(R.color.colorFine));
                expensesViewHolder.ivIcon.setColorFilter(context.getResources().getColor(R.color.colorFine));
                expensesViewHolder.ivIcon.setImageResource(R.drawable.ic_fine);

                expensesViewHolder.tvType.setText(context.getResources().getString(R.string.fab_fine_label));

                expensesViewHolder.tvFuelType.setVisibility(View.GONE);
                expensesViewHolder.tvFuelGrade.setVisibility(View.GONE);
                expensesViewHolder.tvVolume.setVisibility(View.GONE);
                break;
            case EXTRA_TYPE_SPARES:
                expensesViewHolder.ivEdge.setBackgroundColor(context.getResources().getColor(R.color.colorSpares));
                expensesViewHolder.ivIcon.setColorFilter(context.getResources().getColor(R.color.colorSpares));
                expensesViewHolder.ivIcon.setImageResource(R.drawable.ic_parts);

                expensesViewHolder.tvType.setText(context.getResources().getString(R.string.fab_spares_label));

                expensesViewHolder.tvFuelType.setVisibility(View.GONE);
                expensesViewHolder.tvFuelGrade.setVisibility(View.GONE);
                expensesViewHolder.tvVolume.setVisibility(View.GONE);
                break;
            case EXTRA_TYPE_OTHER:
                expensesViewHolder.ivEdge.setBackgroundColor(context.getResources().getColor(R.color.colorOther));
                expensesViewHolder.ivIcon.setColorFilter(context.getResources().getColor(R.color.colorOther));
                expensesViewHolder.ivIcon.setImageResource(R.drawable.ic_wallet);

                expensesViewHolder.tvType.setText(context.getResources().getString(R.string.fab_other_label));

                expensesViewHolder.tvFuelType.setVisibility(View.GONE);
                expensesViewHolder.tvFuelGrade.setVisibility(View.GONE);
                expensesViewHolder.tvVolume.setVisibility(View.GONE);
                break;
        }

        expensesViewHolder.tvNote.setVisibility(View.GONE);

        if (!expense.getNote().equals("empty")) {
            expensesViewHolder.tvNote.setVisibility(View.VISIBLE);
            expensesViewHolder.tvNote.setText(expense.getNote());
        }

        String mileage = String.valueOf(expense.getMileage());
        switch (Common.prefDistanceUnits) {
            case EXTRA_KM:
                mileage += " " + context.getString(R.string.kilometers);
                break;
            case EXTRA_MIL:
                mileage += " " + context.getString(R.string.miles);
                break;
        }
        String amount = String.valueOf(expense.getAmount());
        switch (Common.prefCurrencyUnit) {
            case EXTRA_RUB:
                amount += " " + context.getString(R.string.rub);
                break;
            case EXTRA_USD:
                amount += " " + context.getString(R.string.usd);
                break;
            case EXTRA_UAH:
                amount += " " + context.getString(R.string.uah);
                break;
        }

        expensesViewHolder.tvDate.setText(new SimpleDateFormat("dd.MM.yy HH:mm", Locale.getDefault()).format(expense.getDate()));
        expensesViewHolder.tvMileage.setText(mileage);
        expensesViewHolder.tvAmount.setText(amount);

        expensesViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mOnLongItemClickListener != null) {
                    mOnLongItemClickListener.ItemLongClicked(v, expensesViewHolder.getAdapterPosition());
                }
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return expenseList.isEmpty() ? 0 : expenseList.size();
    }

    public class ExpensesViewHolder extends RecyclerView.ViewHolder {
        ImageView ivEdge, ivIcon;
        TextView tvDate, tvMileage, tvFuelGrade, tvFuelType, tvNote, tvType, tvAmount, tvVolume;

        public ExpensesViewHolder(@NonNull final View itemView) {
            super(itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Expense expense = expenseList.get(getAdapterPosition());
                    Intent intent = new Intent(context, ExpenseViewActivity.class);
                    intent.putExtra(EXTRA_EXPENSE, expense);
                    context.startActivity(intent);
                }
            });

            ivEdge = itemView.findViewById(R.id.iv_item_expense_edge);
            ivIcon = itemView.findViewById(R.id.iv_item_expense_icon);

            tvDate = itemView.findViewById(R.id.tv_item_expense_date_time);
            tvMileage = itemView.findViewById(R.id.tv_item_expense_mileage);
            tvFuelGrade = itemView.findViewById(R.id.tv_item_expense_fuel_grade);
            tvFuelType = itemView.findViewById(R.id.tv_item_expense_fuel_type);
            tvNote = itemView.findViewById(R.id.tv_item_expense_note);
            tvType = itemView.findViewById(R.id.tv_item_expense_type);
            tvAmount = itemView.findViewById(R.id.tv_item_expense_amount);
            tvVolume = itemView.findViewById(R.id.tv_item_expense_volume);

        }
    }
}