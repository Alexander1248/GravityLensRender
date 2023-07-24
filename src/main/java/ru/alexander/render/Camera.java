package ru.alexander.render;

import jcuda.Pointer;
import jcuda.Sizeof;
import jcuda.driver.*;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Picture;

import java.awt.image.BufferedImage;

import static jcuda.driver.JCudaDriver.*;

public class Camera {
    private final Picture image;
    private final Scene scene;
    private final CUdeviceptr imagePtr;
    private final CUdeviceptr dataPtr;

    private final CUcontext context;
    private final CUmodule module;
    private final CUfunction render;

    private int maxSteps = 1000;
    private float maxStepDistance = 1e30f;
    private float gravityConstant = 6.67e-11f;
    private float lightSpeed = 3e8f;

    private float cx = 0;
    private float cy = 0;
    private float cz = 0;

    private float rx = 0;
    private float ry = 0;
    private float fov = 60;
    private float specularity = 10;

    public Camera(Scene scene, int width, int height) {
        this.scene = scene;
        image = Picture.create(width, height, ColorSpace.RGB);

        setExceptionsEnabled(true);

        cuInit(0);
        CUdevice device = new CUdevice();
        cuDeviceGet(device, 0);
        context = new CUcontext();
        cuCtxCreate(context, 0, device);
        module = new CUmodule();
        cuModuleLoad(module, "src/main/resources/render.ptx");

        render = new CUfunction();
        cuModuleGetFunction(render, module, "render");


        imagePtr = new CUdeviceptr();
        cuMemAlloc(imagePtr, (long) image.getPlaneData(0).length * Sizeof.BYTE);

        dataPtr = new CUdeviceptr();
        cuMemAlloc(dataPtr, (long) scene.getData().length * Sizeof.FLOAT);
    }

    private static final int blockSize = 32;
    public void render() {
        if (scene.isExtended()) {
            cuMemFree(dataPtr);
            cuMemAlloc(dataPtr, (long) scene.getData().length * Sizeof.FLOAT);
        }
        cuMemcpyHtoD(dataPtr, Pointer.to(scene.getData()), (long) scene.getData().length * Sizeof.FLOAT);

        cuLaunchKernel(render,
                (int) Math.ceil((double) image.getWidth() / blockSize),  (int) Math.ceil((double) image.getHeight() / blockSize), 1,
                Math.min(image.getWidth(), blockSize), Math.min(image.getHeight(), blockSize), 1,
                0, null,
                Pointer.to(
                        Pointer.to(new int[] { image.getWidth() }),
                        Pointer.to(new int[] { image.getHeight() }),
                        Pointer.to(new int[] { scene.getLength() }),

                        Pointer.to(new int[] { maxSteps }),
                        Pointer.to(new float[] { maxStepDistance }),

                        Pointer.to(new float[] { gravityConstant }),
                        Pointer.to(new float[] { lightSpeed }),

                        Pointer.to(new float[] { cx }),
                        Pointer.to(new float[] { cy }),
                        Pointer.to(new float[] { cz }),

                        Pointer.to(new float[] { rx }),
                        Pointer.to(new float[] { ry }),

                        Pointer.to(new float[] { fov }),
                        Pointer.to(new float[] { specularity }),

                        Pointer.to(dataPtr),
                        Pointer.to(imagePtr)
                ),
                null);
        cuCtxSynchronize();

        cuMemcpyDtoH(Pointer.to(image.getPlaneData(0)), imagePtr, (long) image.getPlaneData(0).length * Sizeof.BYTE);
    }

    public void destroy() {
        cuModuleUnload(module);
        cuCtxDestroy(context);
        cuMemFree(imagePtr);
        cuMemFree(dataPtr);
    }

    public void setGravityConstant(float gravityConstant) {
        this.gravityConstant = gravityConstant;
    }

    public void setLightSpeed(float lightSpeed) {
        this.lightSpeed = lightSpeed;
    }

    public void setPosition(float x, float y, float z) {
        this.cx = x;
        this.cy = y;
        this.cz = z;
    }
    public void setOrientation(float rx, float ry) {
        this.rx = rx;
        this.ry = ry;
    }
    public void setFOV(float fov) {
        this.fov = fov;
    }

    public void setSpecularity(float specularity) {
        this.specularity = specularity;
    }

    public void setMaxSteps(int maxSteps) {
        this.maxSteps = maxSteps;
    }

    public void setMaxStepDistance(float maxStepDistance) {
        this.maxStepDistance = maxStepDistance;
    }

    public Picture getImage() {
        return image;
    }
}
