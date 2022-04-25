package com.example.eruvis.carnotebook.models;

import android.net.Uri;

import java.io.Serializable;
import java.util.Date;

public class Car implements Serializable {

    private String idDoc;
    private String photoUri;
    private String brand;
    private String model;
    private String name;
    private String color;
    private Date yearIssue;
    private int mileage;
    private String fuelType;
    private Date purchaseDate;
    private String number;
    private String vin;
    private Date dateAdd;

    public Car() {

    }

    public Car(String idDoc, String photoUri, String brand, String model, String name, String color,
               Date yearIssue, int mileage, String fuelType, Date purchaseDate, String number,
               String vin, Date dateAdd) {
        this.idDoc = idDoc;
        this.photoUri = photoUri;
        this.brand = brand;
        this.model = model;
        this.name = name;
        this.color = color;
        this.yearIssue = yearIssue;
        this.mileage = mileage;
        this.fuelType = fuelType;
        this.purchaseDate = purchaseDate;
        this.number = number;
        this.vin = vin;
        this.dateAdd = dateAdd;
    }

    public String getIdDoc() {
        return idDoc;
    }

    public String getPhotoUri() {
        return photoUri;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public Date getYearIssue() {
        return yearIssue;
    }

    public int getMileage() {
        return mileage;
    }

    public String getFuelType() {
        return fuelType;
    }

    public Date getPurchaseDate() {
        return purchaseDate;
    }

    public String getNumber() {
        return number;
    }

    public String getVin() {
        return vin;
    }

    public Date getDateAdd() {
        return dateAdd;
    }

}