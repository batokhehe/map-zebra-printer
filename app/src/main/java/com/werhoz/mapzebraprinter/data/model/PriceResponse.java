package com.werhoz.mapzebraprinter.data.model;

import com.google.gson.annotations.SerializedName;
import com.werhoz.mapzebraprinter.data.entity.PriceEntity;

import java.util.List;

public class PriceResponse {
    @SerializedName("TotalRecords")
    private int totalRecords;

    @SerializedName("Page")
    private int page;

    @SerializedName("PageSize")
    private int pageSize;

    @SerializedName("Products")
    private List<PriceEntity> products;

    public boolean isLastPage() {
        return (page * pageSize) >= totalRecords;
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public List<PriceEntity> getProducts() {
        return products;
    }

    public void setProducts(List<PriceEntity> products) {
        this.products = products;
    }
}
