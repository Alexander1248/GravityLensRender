package ru.alexander;

import org.jcodec.api.SequenceEncoder;
import org.jcodec.scale.AWTUtil;
import ru.alexander.render.Camera;
import ru.alexander.render.Scene;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class App {
    //      cd C:\Projects\JavaProjects\GravityLensRender\src\main\resources & nvcc -ptx -m64 -arch=native render.cu -o render.ptx

    public static void main(String[] args) throws IOException {
        int count = 64;

        Scene scene = new Scene();
        scene.addObject(100, 0, 0, 10, 1e20f, Color.white, 0.7f, 0.3f, 0.1f);
        for (int i = 0; i < count - 1; i++) {
            scene.addObject(
                    (float) (500 + (Math.random() * 2 - 1) * 50),
                    (float) ((Math.random() * 2 - 1) * 300),
                    (float) ((Math.random() * 2 - 1) * 150),
                    (float) (1 + Math.random() * 2), 10f, thermo(900 + Math.random() * 20000),
                    (float) (0.3 + Math.random() * 0.4), 0.3f, 0.1f);
        }
        System.out.println("Scene created!");

        Camera camera = new Camera(scene, 1280, 720);
        camera.setFOV(30);
        camera.setMaxSteps(100);
        camera.setMaxStepDistance(100);
        System.out.println("Camera created!");

        camera.render();
        ImageIO.write(AWTUtil.toBufferedImage(camera.getImage()), "png", new File("test.png"));
        System.out.println("Test shot created!");


        SequenceEncoder encoder = SequenceEncoder.createSequenceEncoder(new File("video.mp4"), 30);
        for (int i = -150; i <= 150; i += 5) {
            for (int j = 1; j < count; j++) scene.getData()[j * 11 + 1] -= i;

            camera.render();
            encoder.encodeNativeFrame(camera.getImage());
            System.out.println("Position: " + i);

            for (int j = 1; j < count; j++) scene.getData()[j * 11 + 1] += i;
        }
        encoder.finish();

    }

    public static Color thermo(double temperature) {
        temperature /= 100;
        double r;
        double g;
        double b;

        if (temperature <= 66) {
            g =  Math.max(0, Math.min(255, 99.4708025861 * Math.log(temperature) - 161.1195681661));
            if (temperature <= 19) {
                r = Math.max(0, Math.min(255, Math.PI * temperature * temperature));
                b = 0;
            }
            else {
                r = 255;
                double temp = temperature - 10;
                b = Math.max(0, Math.min(255, 138.5177312231 * Math.log(temp) - 305.0447927307));
            }
        }
        else {
            double temp = temperature - 60;
            r = Math.max(0, Math.min(255, 329.698727446 * Math.pow(temp, -0.1332047592)));
            g = Math.max(0, Math.min(255, 288.1221695283 * Math.pow(temp, -0.0755148492)));
            b = 255;
        }


        return new Color((int) Math.round(r), (int) Math.round(g), (int) Math.round(b));
    }

}
