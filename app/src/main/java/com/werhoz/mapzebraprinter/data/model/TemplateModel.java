package com.werhoz.mapzebraprinter.data.model;

public class TemplateModel {
    public int imageResId;
    public String fileName;
    public String name;

    public TemplateModel(int imageResId, String fileName, String name) {
        this.imageResId = imageResId;
        this.fileName = fileName;
        this.name = name;
    }
}
