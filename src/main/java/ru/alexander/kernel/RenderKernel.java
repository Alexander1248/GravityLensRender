package ru.alexander.kernel;

import com.aparapi.Kernel;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public abstract class RenderKernel extends Kernel {
    protected byte[] image;
    protected int width;
    protected int height;

    public RenderKernel(BufferedImage img) {
        setExplicit(true);

        DataBufferByte buffer = (DataBufferByte) img.getRaster().getDataBuffer();
        image = buffer.getData();
        width = img.getWidth();
        height = img.getHeight();
    }

    public void unloadImageFromGPU() {
        get(image);
    }
}
