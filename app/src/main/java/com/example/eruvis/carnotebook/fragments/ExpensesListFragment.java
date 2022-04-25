package com.example.eruvis.carnotebook.fragments;

import com.example.eruvis.carnotebook.activities.ExpenseEditActivity;
import com.rey.material.app.Dialog;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.eruvis.carnotebook.Common;
import com.example.eruvis.carnotebook.R;
import com.example.eruvis.carnotebook.activities.ExpenseAddActivity;
import com.example.eruvis.carnotebook.adapters.ExpensesRecyclerAdapter;
import com.example.eruvis.carnotebook.models.Expense;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;
import static com.example.eruvis.carnotebook.AppConst.COLLECTION_CARS;
import static com.example.eruvis.carnotebook.AppConst.COLLECTION_USERS;
import static com.example.eruvis.carnotebook.AppConst.COLLECTION_EXPENSES;
import static com.example.eruvis.carnotebook.AppConst.EXTRA_EXPENSE;
import static com.example.eruvis.carnotebook.AppConst.EXTRA_TYPE_ACTIVITY;
import static com.example.eruvis.carnotebook.AppConst.EXTRA_TYPE_FINE;
import static com.example.eruvis.carnotebook.AppConst.EXTRA_TYPE_OTHER;
import static com.example.eruvis.carnotebook.AppConst.EXTRA_TYPE_PARKING;
import static com.example.eruvis.carnotebook.AppConst.EXTRA_TYPE_REFUELING;
import static com.example.eruvis.carnotebook.AppConst.EXTRA_TYPE_SERVICE;
import static com.example.eruvis.carnotebook.AppConst.EXTRA_TYPE_SPARES;
import static com.example.eruvis.carnotebook.AppConst.EXTRA_TYPE_WASH;
import static com.example.eruvis.carnotebook.AppConst.REQUEST_CODE_ADD_EXPENSE;
import static com.example.eruvis.carnotebook.AppConst.REQUEST_CODE_EDIT_EXPENSE;

public class ExpensesListFragment extends Fragment {
    private static final String LOG_TAG = ExpensesListFragment.class.getSimpleName();

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore db;

    private ProgressBar progressBar;

    private FloatingActionMenu fabMenu;
    private FloatingActionButton fabRefueling, fabCarWash, fabService, fabParking, fabFine,
            fabSpares, fabOther;

    private TextView tvTotalExpenses, tvTapToAddExpense, tvAddCarForWork;

    private RecyclerView recyclerView;
    private ExpensesRecyclerAdapter adapter;
    private List<Expense> expenseList;

    private Dialog delDialog;

    private int currentItemPosition;

    private int mScrollOffset = 4;
    private int position;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_expenses_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        progressBar = view.findViewById(R.id.progress_bar);

        setHasOptionsMenu(true);

        initControls(view);
        initExpensesList(view);
        initFab(view);
        if (Common.selectedCarIdDoc != null) {
            progressBar.setVisibility(View.VISIBLE);
            initDb();
        }
        updateUI();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (Common.changedCar) {
            if (Common.selectedCarIdDoc != null) {
                initDb();
            }
            updateUI();

            Common.changedCar = false;
        }

        if (Common.changedSettings) {
            runLayoutAnimation(recyclerView);

            Common.changedSettings = false;
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        if (getActivity() != null) {
            getActivity().getMenuInflater().inflate(R.menu.menu_item_expense, menu);
        }

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.item_expense_edit:
                onEditExpense(currentItemPosition);
                break;
            case R.id.item_expense_delete:
                initDelDialog(currentItemPosition);
                break;
        }

        return true;
    }

    public void onEditExpense(int pos) {
        position = pos;
        Expense expense = expenseList.get(pos);
        Intent intent = new Intent(getContext(), ExpenseEditActivity.class);
        intent.putExtra(EXTRA_EXPENSE, expense);
        startActivityForResult(intent, REQUEST_CODE_EDIT_EXPENSE);
    }

    public void initDelDialog(final int pos) {
        if (getContext() != null) {
            delDialog = new Dialog(getContext());
            delDialog.contentView(R.layout.dialog_expense_delete)
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
                            onDelExpense(pos);
                            delDialog.dismiss();
                        }
                    }).show();
        }
    }

    public void onDelExpense(int pos) {
        delExpenseFromDb(expenseList.get(pos).getIdDoc());

        expenseList.remove(pos);

        adapter.notifyItemRemoved(pos);
        adapter.notifyItemRangeChanged(pos, adapter.getItemCount());

        setCurrentExpensesAmount();

        if (adapter.getItemCount() == 0) {
            tvTapToAddExpense.setVisibility(View.VISIBLE);
        }
    }

    public void delExpenseFromDb(String nameDoc) {
        db = FirebaseFirestore.getInstance();
        db.collection(COLLECTION_USERS)
                .document(firebaseUser.getUid())
                .collection(COLLECTION_CARS)
                .document(Common.selectedCarIdDoc)
                .collection(COLLECTION_EXPENSES)
                .document(nameDoc)
                .delete();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_EXPENSE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    expenseList.add((Expense) data.getSerializableExtra(EXTRA_EXPENSE));
                    tvTapToAddExpense.setVisibility(View.GONE);
                    setCurrentExpensesAmount();

                    runLayoutAnimation(recyclerView);
                }
            }
        } else if (requestCode == REQUEST_CODE_EDIT_EXPENSE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    Expense resultExpense = (Expense) data.getSerializableExtra(EXTRA_EXPENSE);
                    expenseList.set(position, resultExpense);

                    runLayoutAnimation(recyclerView);
                }
            }
        }
    }

    public void setCurrentExpensesAmount() {
        String total = getString(R.string.total_records) + ": " + adapter.getItemCount();
        tvTotalExpenses.setText(total);
    }

    private void runLayoutAnimation(final RecyclerView recyclerView) {
        final Context context = recyclerView.getContext();
        final LayoutAnimationController controller =
                AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_fall_down);

        recyclerView.setLayoutAnimation(controller);
        sortListByDateAddDescending();
        adapter.notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
    }

    private void initControls(View view) {
        tvTapToAddExpense = view.findViewById(R.id.tv_tap_to_add_expenses);
        tvAddCarForWork = view.findViewById(R.id.tv_add_car_for_work);
        tvTotalExpenses = view.findViewById(R.id.tv_total_expenses);

        fabMenu = view.findViewById(R.id.fab_menu);
    }

    private void initExpensesList(View view) {
        recyclerView = view.findViewById(R.id.rv_expenses_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        expenseList = new ArrayList<>();
        adapter = new ExpensesRecyclerAdapter(getContext(), expenseList);

        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (Math.abs(dy) > mScrollOffset) {
                    if (dy > 0) {
                        tvTotalExpenses.animate().translationY(tvTotalExpenses.getHeight()).start();
                        fabMenu.hideMenuButton(true);
                    } else {
                        tvTotalExpenses.animate().translationY(0).start();
                        fabMenu.showMenuButton(true);
                    }
                }
            }
        });

        registerForContextMenu(recyclerView);
        adapter.setOnLongItemClickListener(new ExpensesRecyclerAdapter.onLongItemClickListener() {
            @Override
            public void ItemLongClicked(View v, int position) {
                currentItemPosition = position;
                v.showContextMenu();
            }
        });
    }

    private void initDb() {
        expenseList.clear();

        db = FirebaseFirestore.getInstance();

        CollectionReference dbExpenses;
        dbExpenses = db
                .collection(COLLECTION_USERS)
                .document(firebaseUser.getUid())
                .collection(COLLECTION_CARS)
                .document(Common.selectedCarIdDoc)
                .collection(COLLECTION_EXPENSES);

        dbExpenses
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                            for (DocumentSnapshot d : list) {
                                Expense e = d.toObject(Expense.class);
                                expenseList.add(e);
                            }
                            setCurrentExpensesAmount();
                            tvTapToAddExpense.setVisibility(View.GONE);

                            sortListByDateAddDescending();

                            adapter.notifyDataSetChanged();
                        } else {
                            tvTapToAddExpense.setVisibility(View.VISIBLE);
                        }
                        progressBar.setVisibility(View.INVISIBLE);
                        runLayoutAnimation(recyclerView);
                    }
                });
    }

    private void initFab(View view) {
        fabRefueling = view.findViewById(R.id.fab_add_refueling_expense);
        fabCarWash = view.findViewById(R.id.fab_add_wash_expense);
        fabService = view.findViewById(R.id.fab_add_services_expense);
        fabParking = view.findViewById(R.id.fab_add_parking_expense);
        fabFine = view.findViewById(R.id.fab_add_fine_expense);
        fabSpares = view.findViewById(R.id.fab_add_spares_expense);
        fabOther = view.findViewById(R.id.fab_add_other_expense);

        fabMenu.setClosedOnTouchOutside(true);

        final Intent intent = new Intent(getContext(), ExpenseAddActivity.class);

        fabRefueling.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.putExtra(EXTRA_TYPE_ACTIVITY, EXTRA_TYPE_REFUELING);
                startActivityForResult(intent, REQUEST_CODE_ADD_EXPENSE);
            }
        });

        fabCarWash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.putExtra(EXTRA_TYPE_ACTIVITY, EXTRA_TYPE_WASH);
                startActivityForResult(intent, REQUEST_CODE_ADD_EXPENSE);
            }
        });

        fabService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.putExtra(EXTRA_TYPE_ACTIVITY, EXTRA_TYPE_SERVICE);
                startActivityForResult(intent, REQUEST_CODE_ADD_EXPENSE);
            }
        });

        fabParking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.putExtra(EXTRA_TYPE_ACTIVITY, EXTRA_TYPE_PARKING);
                startActivityForResult(intent, REQUEST_CODE_ADD_EXPENSE);
            }
        });

        fabFine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.putExtra(EXTRA_TYPE_ACTIVITY, EXTRA_TYPE_FINE);
                startActivityForResult(intent, REQUEST_CODE_ADD_EXPENSE);
            }
        });

        fabSpares.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.putExtra(EXTRA_TYPE_ACTIVITY, EXTRA_TYPE_SPARES);
                startActivityForResult(intent, REQUEST_CODE_ADD_EXPENSE);
            }
        });

        fabOther.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.putExtra(EXTRA_TYPE_ACTIVITY, EXTRA_TYPE_OTHER);
                startActivityForResult(intent, REQUEST_CODE_ADD_EXPENSE);
            }
        });
    }

    private void updateUI() {
        fabMenu.close(true);

        if (Common.selectedCarIdDoc != null) {
            tvAddCarForWork.setVisibility(View.GONE);

            fabRefueling.setEnabled(true);
            fabCarWash.setEnabled(true);
            fabService.setEnabled(true);
            fabParking.setEnabled(true);
            fabFine.setEnabled(true);
            fabSpares.setEnabled(true);
            fabOther.setEnabled(true);
        } else {
            tvTapToAddExpense.setVisibility(View.GONE);
            tvAddCarForWork.setVisibility(View.VISIBLE);

            fabRefueling.setEnabled(false);
            fabCarWash.setEnabled(false);
            fabService.setEnabled(false);
            fabParking.setEnabled(false);
            fabFine.setEnabled(false);
            fabSpares.setEnabled(false);
            fabOther.setEnabled(false);
        }
    }

    private void sortListByDateAddDescending() {
        Collections.sort(expenseList, new Comparator<Expense>() {
            @Override
            public int compare(Expense o1, Expense o2) {
                return o2.getDate().compareTo(o1.getDate());
            }
        });
    }

    private void sortListByDateAddAscending() {
        Collections.sort(expenseList, new Comparator<Expense>() {
            @Override
            public int compare(Expense o1, Expense o2) {
                return o1.getDate().compareTo(o2.getDate());
            }
        });
    }
}
