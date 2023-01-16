package ru.alexander.render;

import java.awt.*;
import java.lang.reflect.Array;

public class Scene {
    private double[] data;
    private int[] color;
    private int length;
    private boolean extended;

    public Scene() {
        int base = 8;
        data = new double[base * 8];
        color = new int[base * 3];
        extended = true;

        length = 0;
    }

    public void addObject(double x, double y, double z, double radius, double mass, Color color, double luminosity, double diffusion, double specular) {
        if (data.length / 8 == length) {
            double[] dataBuff = new double[data.length * 2];
            System.arraycopy(data, 0, dataBuff, 0, data.length);
            data = dataBuff;

            int[] colBuff = new int[this.color.length * 2];
            System.arraycopy(this.color, 0, colBuff, 0, this.color.length);
            this.color = colBuff;

            extended = true;
        }
        data[length * 8] = x;
        data[length * 8 + 1] = y;
        data[length * 8 + 2] = z;
        data[length * 8 + 3] = mass;
        data[length * 8 + 4] = radius;
        data[length * 8 + 5] = luminosity;
        data[length * 8 + 6] = diffusion;
        data[length * 8 + 7] = specular;

        this.color[length * 3] = color.getBlue();
        this.color[length * 3 + 1] = color.getGreen();
        this.color[length * 3 + 2] = color.getRed();

        length++;
    }
    public void removeObject(int index) {
        if (index < length) {
            length--;
            swapData(index * 8, length * 8);
            swapData(index * 8 + 1, length * 8 + 1);
            swapData(index * 8 + 2, length * 8 + 2);
            swapData(index * 8 + 3, length * 8 + 3);
            swapData(index * 8 + 4, length * 8 + 4);
            swapData(index * 8 + 5, length * 8 + 5);
            swapData(index * 8 + 6, length * 8 + 6);
            swapData(index * 8 + 7, length * 8 + 7);


            swapColor(index * 3, length * 3);
            swapColor(index * 3 + 1, length * 3 + 1);
            swapColor(index * 3 + 2, length * 3 + 2);
        }
    }

    private void swapData(int i1, int i2) {
        double buff = data[i1];
        data[i1] = data[i2];
        data[i2] = buff;
    }

    private void swapColor(int i1, int i2) {
        int buff = color[i1];
        color[i1] = color[i2];
        color[i2] = buff;
    }

    public double[] getData() {
        return data;
    }

    public int[] getColor() {
        return color;
    }

    public int getLength() {
        return length;
    }

    public boolean isExtended() {
        return extended;
    }
}
