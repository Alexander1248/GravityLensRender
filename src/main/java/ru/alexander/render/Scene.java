package ru.alexander.render;

import java.awt.*;
import java.lang.reflect.Array;

public class Scene {
    private float[] data;
    private int length;
    private boolean extended;

    public Scene() {
        int base = 8;
        data = new float[base * 11];
        extended = true;

        length = 0;
    }

    public void addObject(float x, float y, float z, float radius, float mass, Color color, float luminosity, float diffusion, float specular) {
        if (data.length / 11 == length) {
            float[] dataBuff = new float[data.length * 2];
            System.arraycopy(data, 0, dataBuff, 0, data.length);
            data = dataBuff;

            extended = true;
        }
        data[length * 11] = x;
        data[length * 11 + 1] = y;
        data[length * 11 + 2] = z;
        data[length * 11 + 3] = mass;
        data[length * 11 + 4] = radius;

        data[length * 11 + 5] = (float) color.getRed() / 255;
        data[length * 11 + 6] = (float) color.getGreen() / 255;
        data[length * 11 + 7] = (float) color.getBlue() / 255;
        data[length * 11 + 8] = luminosity;
        data[length * 11 + 9] = diffusion;
        data[length * 11 + 10] = specular;

        length++;
    }
    public void removeObject(int index) {
        if (index < length) {
            length--;
            swapData(index * 11, length * 11);
            swapData(index * 11 + 1, length * 11 + 1);
            swapData(index * 11 + 2, length * 11 + 2);
            swapData(index * 11 + 3, length * 11 + 3);
            swapData(index * 11 + 4, length * 11 + 4);
            swapData(index * 11 + 5, length * 11 + 5);
            swapData(index * 11 + 6, length * 11 + 6);
            swapData(index * 11 + 7, length * 11 + 7);
            swapData(index * 11 + 8, length * 11 + 8);
            swapData(index * 11 + 9, length * 11 + 9);
            swapData(index * 11 + 10, length * 11 + 10);
        }
    }

    private void swapData(int i1, int i2) {
        float buff = data[i1];
        data[i1] = data[i2];
        data[i2] = buff;
    }

    public float[] getData() {
        return data;
    }

    public int getLength() {
        return length;
    }

    public boolean isExtended() {
        return extended;
    }
}
