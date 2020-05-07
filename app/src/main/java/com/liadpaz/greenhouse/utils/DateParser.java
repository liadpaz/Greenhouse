package com.liadpaz.greenhouse.utils;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;

class DateParser {
    @SuppressLint("SimpleDateFormat")
    static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd'T'HH:mm:ss.SSSSSSS");
}
