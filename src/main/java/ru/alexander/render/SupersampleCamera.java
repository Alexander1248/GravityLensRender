package ru.alexander.render;

import com.aparapi.Range;
import com.aparapi.exception.CompileFailedException;
import com.aparapi.internal.kernel.KernelManager;
import ru.alexander.kernel.CameraKernel;
import ru.alexander.kernel.SSAACameraKernel;

import java.awt.image.BufferedImage;
import java.util.Random;

public class SupersampleCamera {
    private final BufferedImage image;
    private final SSAACameraKernel kernel;
    private final Scene scene;

    public SupersampleCamera(Scene scene, int width, int height) throws CompileFailedException {
        this.scene = scene;
        image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        kernel = new SSAACameraKernel(image);

        kernel.compile(KernelManager.instance().bestDevice());
    }

    public void render() {
        if (scene.isExtended()) {
            kernel.data = scene.getData();
            kernel.color = scene.getColor();
        }
        kernel.seed = new Random().nextLong();
        kernel.len = scene.getLength();

        kernel.put(kernel.color).put(kernel.data);
        kernel.execute(Range.create(image.getWidth() * image.getHeight()));
        kernel.unloadImageFromGPU();
    }

    public void setGravityConstant(double gravityConstant) {
        kernel.gravityConstant = gravityConstant;
    }

    public void setLightSpeed(double lightSpeed) {
        kernel.lightSpeed = lightSpeed;
    }

    public void setCameraPosition(double x, double y, double z) {
        kernel.cx = x;
        kernel.cy = y;
        kernel.cz = z;
    }
    public void setCameraOrientation(double rx, double ry) {
        kernel.rx = rx;
        kernel.ry = ry;
    }
    public void setCameraZoom(double zoom) {
        kernel.zoom = zoom;
    }
    public void setSamplingRate(int samplingRate) {
        kernel.samplingRate = samplingRate * samplingRate;
    }

    public void setSpecularity(double specularity) {
        kernel.specularity = specularity;
    }

    public BufferedImage getImage() {
        return image;
    }
}
