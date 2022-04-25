package com.example.eruvis.carnotebook.activities;

import com.example.eruvis.carnotebook.adapters.ViewPagerAdapter;
import com.example.eruvis.carnotebook.Common;
import com.example.eruvis.carnotebook.fragments.ExpensesListFragment;
import com.example.eruvis.carnotebook.fragments.StatisticsFragment;
import com.example.eruvis.carnotebook.fragments.TotalFragment;
import com.example.eruvis.carnotebook.R;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rey.material.app.Dialog;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.TextView;

import static com.example.eruvis.carnotebook.AppConst.EXTRA_TYPE_ACTIVITY;
import static com.example.eruvis.carnotebook.AppConst.FUEL_TYPE_KEY;
import static com.example.eruvis.carnotebook.AppConst.ID_DOC_KEY;
import static com.example.eruvis.carnotebook.AppConst.LAST_REFUELING_FUEL_GRADE_KEY;
import static com.example.eruvis.carnotebook.AppConst.LAST_REFUELING_FUEL_PRICE_KEY;
import static com.example.eruvis.carnotebook.AppConst.LAST_REFUELING_FUEL_TYPE_KEY;
import static com.example.eruvis.carnotebook.AppConst.PREF_CURRENCY_UNIT;
import static com.example.eruvis.carnotebook.AppConst.PREF_DISTANCE_UNITS;
import static com.example.eruvis.carnotebook.AppConst.PREF_FUEL_UNITS;
import static com.example.eruvis.carnotebook.AppConst.PREF_LAST_REFUELING_FUEL_PRICE;
import static com.example.eruvis.carnotebook.AppConst.PREF_LAST_REFUELING_FUEL_TYPE;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore db;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        initControls();
        checkSettings();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkSettings();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchMenuItem.getActionView();
        searchView.setQueryHint(getString(R.string.search));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                return true;
            }
        });

        MenuItem filterMenuItem = menu.findItem(R.id.action_filter);

        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.drawer_add_auto:
                startActivity(new Intent(this, CarListActivity.class));
                break;
            case R.id.drawer_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.drawer_about_app:
                break;
            case R.id.drawer_sign_out:
                showSignOutDialog();
            default:
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void checkSettings() {
        Common.selectedCarIdDoc = sharedPreferences.getString(ID_DOC_KEY, null);
        Common.selectedCarFuelType = sharedPreferences.getString(FUEL_TYPE_KEY, null);
        Common.prefDistanceUnits = sharedPreferences.getString(PREF_DISTANCE_UNITS, getString(R.string.pref_default_distance_units));
        Common.prefCurrencyUnit = sharedPreferences.getString(PREF_CURRENCY_UNIT, getString(R.string.pref_default_currency));
        Common.prefFuelUnits = sharedPreferences.getString(PREF_FUEL_UNITS, getString(R.string.pref_default_fuel_units));

        Common.prefLastRefuelingFuelType = sharedPreferences.getBoolean(PREF_LAST_REFUELING_FUEL_TYPE, true);
        Common.prefLastRefuelingFuelPrice = sharedPreferences.getBoolean(PREF_LAST_REFUELING_FUEL_PRICE, true);

        Common.lastRefuelingFuelType = sharedPreferences.getString(LAST_REFUELING_FUEL_TYPE_KEY, null);
        Common.lastRefuelingFuelGrade = sharedPreferences.getString(LAST_REFUELING_FUEL_GRADE_KEY, null);
        Common.lastRefuelingFuelPrice = sharedPreferences.getString(LAST_REFUELING_FUEL_PRICE_KEY, null);
    }

    private void initControls() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerLayout = findViewById(R.id.nav_head);
        TextView tvHeaderEmail = headerLayout.findViewById(R.id.tv_header_email);
        TextView tvCarName = headerLayout.findViewById(R.id.tv_car_name);
        if (!firebaseUser.isAnonymous()) {
            tvHeaderEmail.setText(firebaseUser.getEmail());
        } else {
            tvHeaderEmail.setText(getString(R.string.guest));
        }
        /*
        if () {
            tvCarName.setText(R.string.car_no_select);
        } else {
            tvCarName.setText();
        }*/

        ViewPager viewPager = findViewById(R.id.vp_main);
        setupViewPager(viewPager);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
            }

            @Override
            public void onPageSelected(int position) {
                if (filterMenuItem != null) {
                    filterMenuItem.setVisible(position == 1);
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        TabLayout tabLayout = findViewById(R.id.tl_main_tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new TotalFragment(), getString(R.string.total_title));
        adapter.addFragment(new ExpensesListFragment(), getString(R.string.expenses_list_title));
        adapter.addFragment(new StatisticsFragment(), getString(R.string.statistics_title));
        viewPager.setAdapter(adapter);
    }

    private void showSignOutDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.contentView(R.layout.dialog_sign_out)
                .positiveAction(getString(R.string.sign_out))
                .negativeAction(getString(R.string.cancel))
                .negativeActionClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                })
                .positiveActionClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        signOut();
                        dialog.dismiss();
                    }
                });
        dialog.show();
    }

    private void signOut() {
        sharedPreferences.edit().putString(ID_DOC_KEY, null).apply();
        firebaseAuth.signOut();
        startActivity(new Intent(this, SignInActivity.class));
        finish();
    }
}
