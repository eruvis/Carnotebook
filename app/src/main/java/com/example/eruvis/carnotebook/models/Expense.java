package com.example.eruvis.carnotebook.models;

import java.io.Serializable;
import java.util.Date;

public class Expense implements Serializable {

    private String idDoc;
    private String type;
    private Date date;
    private int mileage;
    private double amount;
    private double volume;
    private double price;
    private String fuelType;
    private String fuelGrade;
    private String note;
    private Date dateAdd;

    public Expense() {

    }

    public Expense(String idDoc, String type, Date date, int mileage, double amount, double volume,
                   double price, String fuelType, String fuelGrade, String note, Date dateAdd) {
        this.idDoc = idDoc;
        this.type = type;
        this.date = date;
        this.mileage = mileage;
        this.amount = amount;
        this.volume = volume;
        this.price = price;
        this.fuelType = fuelType;
        this.fuelGrade = fuelGrade;
        this.note = note;
        this.dateAdd = dateAdd;
    }

    public String getIdDoc() {
        return idDoc;
    }

    public String getType() {
        return type;
    }

    public Date getDate() {
        return date;
    }

    public int getMileage() {
        return mileage;
    }

    public double getAmount() {
        return amount;
    }

    public double getVolume() {
        return volume;
    }

    public double getPrice() {
        return price;
    }

    public String getFuelType() {
        return fuelType;
    }

    public String getFuelGrade() {
        return fuelGrade;
    }

    public String getNote() {
        return note;
    }

    public Date getDateAdd() {
        return dateAdd;
    }
}
