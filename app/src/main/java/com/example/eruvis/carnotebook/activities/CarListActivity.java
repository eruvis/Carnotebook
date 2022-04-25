package com.example.eruvis.carnotebook.activities;

import com.example.eruvis.carnotebook.models.Car;
import com.example.eruvis.carnotebook.adapters.CarsRecyclerAdapter;
import com.example.eruvis.carnotebook.Common;
import com.example.eruvis.carnotebook.R;
import com.example.eruvis.carnotebook.SwipeHelper;
import com.github.clans.fab.FloatingActionButton;
import com.rey.material.app.Dialog;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import static com.example.eruvis.carnotebook.AppConst.COLLECTION_CARS;
import static com.example.eruvis.carnotebook.AppConst.COLLECTION_EXPENSES;
import static com.example.eruvis.carnotebook.AppConst.COLLECTION_USERS;
import static com.example.eruvis.carnotebook.AppConst.EXTRA_CAR;
import static com.example.eruvis.carnotebook.AppConst.FUEL_TYPE_KEY;
import static com.example.eruvis.carnotebook.AppConst.ID_DOC_KEY;
import static com.example.eruvis.carnotebook.AppConst.REQUEST_CODE_ADD_CAR;
import static com.example.eruvis.carnotebook.AppConst.REQUEST_CODE_EDIT_CAR;

public class CarListActivity extends AppCompatActivity {
    private static final String LOG_TAG = CarListActivity.class.getSimpleName();

    private Toolbar toolbar;
    private ProgressBar progressBar;
    private FloatingActionButton fabAddCar;
    private TextView tvTotalCars, tvTapToAddCar;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore db;

    private SharedPreferences sharedPreferences;

    private RecyclerView recyclerView;
    private CarsRecyclerAdapter adapter;
    private List<Car> carList;

    private Dialog delDialog;

    private int position;

    private int mScrollOffset = 4;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_list);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        progressBar = findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);

        initCarList();
        initControls();
        checkDb();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_CAR) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    carList.add((Car) data.getSerializableExtra(EXTRA_CAR));
                    tvTapToAddCar.setVisibility(View.GONE);
                    checkSelectPos();
                    setCurrentCarsAmount();
                }
            }
        } else if (requestCode == REQUEST_CODE_EDIT_CAR) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    Car resultCar = (Car) data.getSerializableExtra(EXTRA_CAR);
                    carList.set(position, resultCar);
                    Common.selectedCarFuelType = carList.get(position).getFuelType();
                    savePreference();
                }
            }
        }

        runLayoutAnimation(recyclerView);
    }

    public void initCarList() {
        recyclerView = findViewById(R.id.rv_cars_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        carList = new ArrayList<>();
        adapter = new CarsRecyclerAdapter(this, carList);

        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (Math.abs(dy) > mScrollOffset) {
                    if (dy > 0) {
                        tvTotalCars.animate().translationY(tvTotalCars.getHeight()).start();
                        fabAddCar.hide(true);
                    } else {
                        tvTotalCars.animate().translationY(0).start();
                        fabAddCar.show(true);
                    }
                }
            }
        });
    }

    public void checkDb() {

        db = FirebaseFirestore.getInstance();

        CollectionReference dbCars;
        dbCars = db
                .collection("users")
                .document(firebaseUser.getUid())
                .collection("cars");

        dbCars
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                            for (DocumentSnapshot d : list) {
                                Car c = d.toObject(Car.class);
                                carList.add(c);
                            }
                            setCurrentCarsAmount();

                            checkSelectPos();
                            tvTapToAddCar.setVisibility(View.GONE);
                            adapter.notifyDataSetChanged();
                        } else {
                            tvTapToAddCar.setVisibility(View.VISIBLE);
                        }
                        progressBar.setVisibility(View.INVISIBLE);
                        runLayoutAnimation(recyclerView);
                    }
                });
    }

    public void checkSelectPos() {
        int count = adapter.getItemCount();
        if (Common.selectedCarIdDoc != null) {
            if (count != 0) {
                count--;
                while (count != 0) {
                    if (Common.selectedCarIdDoc.equals(carList.get(count).getIdDoc())) {
                        Common.selectedCarFuelType = carList.get(count).getFuelType();
                        Common.selectedPosition = count;
                        savePreference();
                        return;
                    } else {
                        count--;
                    }
                }
                Common.selectedCarIdDoc = carList.get(0).getIdDoc();
                Common.selectedCarFuelType = carList.get(0).getFuelType();
                Common.selectedPosition = 0;

            } else {
                Common.selectedCarIdDoc = null;
                Common.selectedCarFuelType = null;
                Common.selectedPosition = 0;
            }
        } else {
            if (count != 0) {
                Common.selectedCarIdDoc = carList.get(0).getIdDoc();
                Common.selectedCarFuelType = carList.get(0).getFuelType();
                Common.selectedPosition = 0;
            }
        }
        Common.changedCar = true;
        savePreference();
    }

    public void savePreference() {
        sharedPreferences.edit().putString(ID_DOC_KEY, Common.selectedCarIdDoc).apply();
        sharedPreferences.edit().putString(FUEL_TYPE_KEY, Common.selectedCarFuelType).apply();
    }

    private void runLayoutAnimation(final RecyclerView recyclerView) {
        final Context context = recyclerView.getContext();
        final LayoutAnimationController controller =
                AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_fall_down);

        recyclerView.setLayoutAnimation(controller);
        adapter.notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
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

        tvTapToAddCar = findViewById(R.id.tv_tap_to_add_car);
        tvTotalCars = findViewById(R.id.tv_total_cars);

        fabAddCar = findViewById(R.id.fab_add_car);
        fabAddCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddCar();
            }
        });


        SwipeHelper swipeHelper = new SwipeHelper(this, recyclerView) {
            @Override
            public void instantiateUnderlayButton(final RecyclerView.ViewHolder viewHolder, List<UnderlayButton> underlayButtons) {

                underlayButtons.add(new SwipeHelper.UnderlayButton(
                        "Delete",
                        getResources().getColor(R.color.colorRed),
                        new SwipeHelper.UnderlayButtonClickListener() {
                            @Override
                            public void onClick(int pos) {
                                initDelDialog(pos);
                                Common.changedCar = true;
                            }
                        }
                ));
                underlayButtons.add(new SwipeHelper.UnderlayButton(
                        "Edit",
                        getResources().getColor(R.color.colorYellow),
                        new SwipeHelper.UnderlayButtonClickListener() {
                            @Override
                            public void onClick(int pos) {
                                onEditCar(pos);
                            }
                        }
                ));
                underlayButtons.add(new SwipeHelper.UnderlayButton(
                        "Select",
                        getResources().getColor(R.color.colorGreen),
                        new SwipeHelper.UnderlayButtonClickListener() {
                            @Override
                            public void onClick(int pos) {
                                if (!carList.get(pos).getIdDoc().equals(Common.selectedCarIdDoc))
                                {
                                    Common.changedCar = true;
                                    Common.selectedCarIdDoc = carList.get(pos).getIdDoc();
                                    Common.selectedCarFuelType = carList.get(pos).getFuelType();
                                    savePreference();
                                    adapter.notifyItemChanged(Common.selectedPosition);
                                    Common.selectedPosition = viewHolder.getLayoutPosition();
                                    adapter.notifyItemChanged(Common.selectedPosition);
                                } else {
                                    adapter.notifyItemChanged(Common.selectedPosition);
                                }
                            }
                        }
                ));
            }
        };

    }

    public void initDelDialog(final int pos) {
        delDialog = new Dialog(CarListActivity.this);
        delDialog.contentView(R.layout.dialog_car_delete)
                .actionTextColor(R.color.colorBackgroundToolbar)
                .negativeAction(R.string.cancel)
                .negativeActionClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        delDialog.dismiss();
                    }
                })
                .positiveAction(R.string.delete)
                .positiveActionClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onDelCar(pos);
                        delDialog.dismiss();
                    }
                }).show();
    }

    public void onDelCar(int pos) {
        delCarFromDb(carList.get(pos).getIdDoc());

        carList.remove(pos);

        checkSelectPos();
        adapter.notifyItemChanged(Common.selectedPosition);

        adapter.notifyItemRemoved(pos);
        adapter.notifyItemRangeChanged(pos, adapter.getItemCount());

        setCurrentCarsAmount();

        if (adapter.getItemCount() == 0) {
            tvTapToAddCar.setVisibility(View.VISIBLE);
        }
    }

    public void delCarFromDb(String nameDoc) {
        db = FirebaseFirestore.getInstance();
        db.collection(COLLECTION_USERS)
                .document(firebaseUser.getUid())
                .collection(COLLECTION_CARS)
                .document(nameDoc)
                .delete();
    }

    public void onEditCar(int pos) {
        position = pos;
        Car car = carList.get(pos);
        Intent intent = new Intent(getApplicationContext(), CarEditActivity.class);
        intent.putExtra(EXTRA_CAR, car);
        startActivityForResult(intent, REQUEST_CODE_EDIT_CAR);
    }

    public void onAddCar() {
        Common.changedCar = true;
        Intent intent = new Intent(getApplicationContext(), CarAddActivity.class);
        startActivityForResult(intent, REQUEST_CODE_ADD_CAR);
    }

    public void setCurrentCarsAmount() {
        String total = getString(R.string.total_car) + ": " + adapter.getItemCount();
        tvTotalCars.setText(total);
    }
}
