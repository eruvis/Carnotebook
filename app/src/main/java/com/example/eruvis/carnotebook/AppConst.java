package com.example.eruvis.carnotebook;

public class AppConst {
    // ACTIVITY REQUESTS
    public static final int REQUEST_CODE_ADD_CAR = 1;
    public static final int REQUEST_CODE_EDIT_CAR = 2;

    public static final int REQUEST_CODE_ADD_EXPENSE = 3;
    public static final int REQUEST_CODE_EDIT_EXPENSE = 4;

    //COLLECTIONS
    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_CARS = "cars";
    public static final String COLLECTION_EXPENSES = "expenses";

    // EXTRAS
    public static final String EXTRA_CAR = "car";
    public static final String EXTRA_EXPENSE = "expense";
    public static final String EXTRA_EMPTY = "empty";

    public static final String EXTRA_KM = "KM";
    public static final String EXTRA_MIL = "MIL";
    public static final String EXTRA_LIT = "LIT";
    public static final String EXTRA_GAL = "GAL";
    public static final String EXTRA_RUB = "RUB";
    public static final String EXTRA_USD = "USD";
    public static final String EXTRA_UAH = "UAH";

    public static final String EXTRA_TYPE_ACTIVITY = "type_activity";
    public static final String EXTRA_TYPE_REFUELING = "refueling";
    public static final String EXTRA_TYPE_WASH = "wash";
    public static final String EXTRA_TYPE_SERVICE = "service";
    public static final String EXTRA_TYPE_PARKING = "parking";
    public static final String EXTRA_TYPE_FINE = "fine";
    public static final String EXTRA_TYPE_SPARES = "spares";
    public static final String EXTRA_TYPE_OTHER = "other";

    //PREFERENCE
    public static final String PREF_CURRENCY_UNIT = "key_currency_unit";
    public static final String PREF_FUEL_UNITS = "key_fuel_units";
    public static final String PREF_DISTANCE_UNITS = "key_distance_units";
    public static final String PREF_LAST_REFUELING_FUEL_TYPE = "key_last_refueling_fuel_type";
    public static final String PREF_LAST_REFUELING_FUEL_PRICE = "key_last_refueling_fuel_price";

    //FILES
    public static final String ID_DOC_KEY = "car_id_doc";
    public static final String FUEL_TYPE_KEY = "car_fuel_type";
    public static final String LAST_REFUELING_FUEL_TYPE_KEY = "last_refueling_fuel_type";
    public static final String LAST_REFUELING_FUEL_GRADE_KEY = "last_refueling_fuel_grade";
    public static final String LAST_REFUELING_FUEL_PRICE_KEY = "last_refueling_fuel_price";
}
