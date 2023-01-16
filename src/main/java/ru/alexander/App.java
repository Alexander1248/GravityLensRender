package ru.alexander;

import com.aparapi.exception.CompileFailedException;
import org.jcodec.api.SequenceEncoder;
import org.jcodec.scale.AWTUtil;
import ru.alexander.render.Camera;
import ru.alexander.render.Scene;
import ru.alexander.render.SupersampleCamera;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class App {
    public static void main(String[] args) throws IOException, CompileFailedException {
        int count = 64;

        Scene scene = new Scene();
        scene.addObject(100, 0, 0, 1, 1e20, Color.white, 0.7, 0.3, 0.1);
        for (int i = 0; i < count - 1; i++) {
            scene.addObject(
                    500 + (Math.random() * 2 - 1) * 50,
                    (Math.random() * 2 - 1) * 300,
                    (Math.random() * 2 - 1) * 150,
                    1 + Math.random() * 2, 10, thermo(900 + Math.random() * 20000),
                    0.3 + Math.random() * 0.4, 0.3, 0.1);
        }
        System.out.println("Scene created!");

        SupersampleCamera camera = new SupersampleCamera(scene, 640, 360);
        System.out.println("Camera created!");

        camera.render();
        ImageIO.write(camera.getImage(), "png", new File("test.png"));
        System.out.println("Test shot created!");


        SequenceEncoder encoder = SequenceEncoder.createSequenceEncoder(new File("video.mp4"), 30);
        for (int i = -500; i <= 500; i += 5) {
            for (int j = 1; j < count; j++)  scene.getData()[j * 5 + 1] -= i;

            camera.render();
            encoder.encodeNativeFrame(AWTUtil.fromBufferedImageRGB(camera.getImage()));
            System.out.println("Position: " + i);

            for (int j = 1; j < count; j++)  scene.getData()[j * 5 + 1] += i;
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
