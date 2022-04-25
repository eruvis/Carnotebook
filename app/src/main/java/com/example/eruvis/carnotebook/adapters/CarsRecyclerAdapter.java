package com.example.eruvis.carnotebook.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.eruvis.carnotebook.activities.CarViewActivity;
import com.example.eruvis.carnotebook.Common;
import com.example.eruvis.carnotebook.models.Car;
import com.example.eruvis.carnotebook.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.eruvis.carnotebook.AppConst.EXTRA_CAR;
import static com.example.eruvis.carnotebook.AppConst.EXTRA_KM;
import static com.example.eruvis.carnotebook.AppConst.EXTRA_MIL;

public class CarsRecyclerAdapter extends RecyclerView.Adapter<CarsRecyclerAdapter.CarsViewHolder> {

    private Context context;
    private List<Car> carList;

    public CarsRecyclerAdapter(Context context, List<Car> carList) {
        this.context = context;
        this.carList = carList;
    }

    @NonNull
    @Override
    public CarsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        return new CarsViewHolder(
                LayoutInflater.from(context).inflate(R.layout.item_car, viewGroup, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull CarsViewHolder carsViewHolder, int position) {
        Car car = carList.get(carsViewHolder.getAdapterPosition());

        if (Common.selectedPosition == carsViewHolder.getAdapterPosition()) {
            carsViewHolder.itemView.setSelected(true);
            carsViewHolder.ivSelected.setBackgroundColor(context.getResources().getColor(R.color.colorSelected));
        } else {
            carsViewHolder.ivSelected.setBackgroundColor(context.getResources().getColor(R.color.colorNotSelected));
        }


        if (!car.getPhotoUri().equals("empty")) {
            carsViewHolder.civCarPhoto.setImageURI(Uri.parse(car.getPhotoUri()));
        }
        String year = new SimpleDateFormat("yyyy", Locale.getDefault()).format(car.getYearIssue()) + " " + context.getString(R.string.year_y) + '.';
        String mileage = String.valueOf(car.getMileage());
        switch (Common.prefDistanceUnits) {
            case EXTRA_KM:
                mileage += " " + context.getString(R.string.kilometers);
                break;
            case EXTRA_MIL:
                mileage += " " + context.getString(R.string.miles);
                break;
        }
        String dateAdd = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(car.getDateAdd());
        carsViewHolder.tvName.setText(car.getName());
        carsViewHolder.tvYear.setText(year);
        carsViewHolder.tvMileage.setText(mileage);
        carsViewHolder.tvDateAdd.setText(dateAdd);
    }

    @Override
    public int getItemCount() {
        return carList.isEmpty() ? 0 : carList.size();
    }

    public class CarsViewHolder extends RecyclerView.ViewHolder {
        ImageView ivSelected;
        CircleImageView civCarPhoto;
        TextView tvName, tvYear, tvMileage, tvDateAdd;

        public CarsViewHolder(@NonNull final View itemView) {
            super(itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Car car = carList.get(getAdapterPosition());
                    Intent intent = new Intent(context, CarViewActivity.class);
                    intent.putExtra(EXTRA_CAR, car);
                    context.startActivity(intent);
                }
            });

            ivSelected = itemView.findViewById(R.id.iv_item_car_selected);
            civCarPhoto = itemView.findViewById(R.id.civ_item_car_photo);
            tvName = itemView.findViewById(R.id.tv_item_car_name);
            tvYear = itemView.findViewById(R.id.tv_item_car_year);
            tvMileage = itemView.findViewById(R.id.tv_item_car_mileage);
            tvDateAdd = itemView.findViewById(R.id.tv_item_car_date_add);
        }
    }
}