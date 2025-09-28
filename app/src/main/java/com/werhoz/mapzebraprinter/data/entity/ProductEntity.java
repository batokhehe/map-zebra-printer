package com.werhoz.mapzebraprinter.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(tableName = "products")
public class ProductEntity {
    @PrimaryKey
    @NonNull
    @SerializedName("ItemNumber")
    private String itemNumber;

    @SerializedName("Description")
    private String description;

    @SerializedName("Name")
    private String name;

    @SerializedName("AliasNumber")
    private String aliasNumber;

    @SerializedName("StyleNo")
    private String styleNo;

    @SerializedName("ConfigurationCode")
    private int configurationCode;

    @SerializedName("QRCode")
    private String qrCode;

    @SerializedName("Currency")
    private String currency;

    @SerializedName("ItemGroup")
    private String itemGroup;

    @SerializedName("Size")
    private String size;

    @SerializedName("Color")
    private String color;

    @SerializedName("WasPrice")
    private String wasPrice;

    @SerializedName("SalesPrice")
    private String salesPrice;

    @NonNull
    public String getItemNumber() {
        return itemNumber;
    }

    public void setItemNumber(@NonNull String itemNumber) {
        this.itemNumber = itemNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAliasNumber() {
        return aliasNumber;
    }

    public void setAliasNumber(String aliasNumber) {
        this.aliasNumber = aliasNumber;
    }

    public String getStyleNo() {
        return styleNo;
    }

    public void setStyleNo(String styleNo) {
        this.styleNo = styleNo;
    }

    public int getConfigurationCode() {
        return configurationCode;
    }

    public void setConfigurationCode(int configurationCode) {
        this.configurationCode = configurationCode;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getItemGroup() {
        return itemGroup;
    }

    public void setItemGroup(String itemGroup) {
        this.itemGroup = itemGroup;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getWasPrice() {
        return wasPrice;
    }

    public void setWasPrice(String wasPrice) {
        this.wasPrice = wasPrice;
    }

    public String getSalesPrice() {
        return salesPrice;
    }

    public void setSalesPrice(String salesPrice) {
        this.salesPrice = salesPrice;
    }
}
