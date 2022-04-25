package com.example.eruvis.carnotebook.fragments;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;

import com.example.eruvis.carnotebook.Common;
import com.example.eruvis.carnotebook.R;

import static com.example.eruvis.carnotebook.AppConst.PREF_CURRENCY_UNIT;
import static com.example.eruvis.carnotebook.AppConst.PREF_DISTANCE_UNITS;
import static com.example.eruvis.carnotebook.AppConst.PREF_FUEL_UNITS;

public class SettingsFragment extends PreferenceFragment {

    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        preferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                Common.changedSettings = true;

                if (key.equals(PREF_CURRENCY_UNIT)) {
                    Preference currencyPref = findPreference(key);
                    currencyPref.setSummary(sharedPreferences.getString(key, ""));
                }

                if (key.equals(PREF_FUEL_UNITS)) {
                    Preference fuelPref = findPreference(key);
                    fuelPref.setSummary(sharedPreferences.getString(key, ""));
                }

                if (key.equals(PREF_DISTANCE_UNITS)) {
                    Preference distancePref = findPreference(key);
                    distancePref.setSummary(sharedPreferences.getString(key, ""));
                }
            }
        };
    }


    @Override
    public void onResume() {
        super.onResume();

        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(preferenceChangeListener);


        Preference currencyPref = findPreference(PREF_CURRENCY_UNIT);
        currencyPref.setSummary(getPreferenceScreen().getSharedPreferences().getString(PREF_CURRENCY_UNIT, ""));


        Preference fuelPref = findPreference(PREF_FUEL_UNITS);
        fuelPref.setSummary(getPreferenceScreen().getSharedPreferences().getString(PREF_FUEL_UNITS, ""));


        Preference distancePref = findPreference(PREF_DISTANCE_UNITS);
        distancePref.setSummary(getPreferenceScreen().getSharedPreferences().getString(PREF_DISTANCE_UNITS, ""));
    }

    @Override
    public void onPause() {
        super.onPause();

        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(preferenceChangeListener);
    }
}
