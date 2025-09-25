package com.werhoz.mapzebraprinter.data.model;

import com.google.gson.annotations.SerializedName;
import com.werhoz.mapzebraprinter.data.entity.ProductEntity;

import java.util.List;

public class ProductResponse {
    @SerializedName("TotalRecords")
    private int totalRecords;

    @SerializedName("Page")
    private int page;

    @SerializedName("PageSize")
    private int pageSize;

    @SerializedName("Products")
    private List<ProductEntity> products;

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

    public List<ProductEntity> getProducts() {
        return products;
    }

    public void setProducts(List<ProductEntity> products) {
        this.products = products;
    }
}
