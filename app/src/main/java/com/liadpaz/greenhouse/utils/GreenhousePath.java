package com.liadpaz.greenhouse.utils;

import androidx.annotation.Keep;

import java.io.Serializable;

@Keep
@SuppressWarnings({"unused", "WeakerAccess"})
public class GreenhousePath implements Serializable {
    public Block Path;
    public int X;
    public int Y;
    public int Width;
    public int Height;

    public GreenhousePath(Block path, int x, int y, int width, int height) {
        Path = path;
        X = x;
        Y = y;
        Width = width;
        Height = height;
    }

    public GreenhousePath() {}

    enum Block {
        Entrance, Road
    }
}