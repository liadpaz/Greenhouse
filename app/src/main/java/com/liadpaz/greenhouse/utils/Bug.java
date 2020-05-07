package com.liadpaz.greenhouse.utils;

import androidx.annotation.Keep;

import com.google.firebase.database.Exclude;

import java.util.Date;

@Keep
@SuppressWarnings({"unused"})
public class Bug {

    private String greenhouse;
    private String time;
    private double x;
    private double y;

    public Bug(String greenhouse, Date time, double x, double y) {
        this.greenhouse = greenhouse;
        this.time = DateParser.dateFormat.format(time);
        this.x = Math.round(x * 100) / 100.0;
        this.y = Math.round(y * 100) / 100.0;
    }

    public Bug() {}

    public String getGreenhouse() {
        return greenhouse;
    }

    public void setGreenhouse(String greenhouse) {
        this.greenhouse = greenhouse;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    @SuppressWarnings("ConstantConditions")
    @Exclude
    public int getId() {
        try {
            return (int)DateParser.dateFormat.parse(time).getTime() + (int)x + (int)y;
        } catch (Exception ignored) {
            return 0;
        }
    }
}