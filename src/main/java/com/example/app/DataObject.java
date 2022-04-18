package com.example.app;

import java.util.ArrayList;

public class DataObject {
    private String name;
    private String type;
    private float y;
    private float x;
    private int k;

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public DataObject(String name, String type, float y, float x, int k) {
        this.name = name;
        this.type = type;
        this.y = y;
        this.x = x;
        this.k = k;
    }
    public DataObject(ArrayList<String> params){
        this.name = params.get(0);
        this.type = params.get(1);
        this.y = Float.parseFloat(params.get(2));
        this.x = Float.parseFloat(params.get(3));
        this.k = Integer.parseInt(params.get(4));
    }

    public float getY() {
        return y;
    }

    public float getX() {
        return x;
    }

    @Override
    public String toString() {
        return "DataObject{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", y=" + y +
                ", x=" + x +
                ", k=" + k +
                '}';
    }
}
