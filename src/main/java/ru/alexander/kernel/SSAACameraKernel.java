package ru.alexander.kernel;

import java.awt.image.BufferedImage;
import java.util.Random;

public class SSAACameraKernel extends RenderKernel {

    public double[] data;
    public int[] color;
    public int len;

    public double gravityConstant = 6.67e-11;
    public double lightSpeed = 3e8;



    public double cx = 0, cy = 0, cz = 0;
    public double rx = 0, ry = 0;

    public double zoom = 10;

    public double specularity = 3;

    public int samplingRate = 4;
    public long seed = new Random().nextLong();


    public SSAACameraKernel(BufferedImage img) {
        super(img);
    }

    @Override
    public void run() {
        int gid = getGlobalId();
        double zoomedPI = 3.14159265 / zoom;

        double red = 0;
        double green = 0;
        double blue = 0;

        long r = seed % (468742 + gid) + 395L * gid;
        for (int s = 0; s < samplingRate; s++) {
            r = r % 5764763 + 4877564;
            double rv = (double) (r % 1024) / 1024 - 0.5;
            double px = ((double) (gid % width) + rv) / width * 2 - 1;

            r = r % 5764763 + 4877564;
            rv = (double) (r % 1024) / 1024 - 0.5;
            double py = ((double) (gid / width) + rv) / height * 2 - 1;

            double dia = (double) width / height;

            double cosComp = cos(ry + py * zoomedPI);
            double nx = cos(rx + px * zoomedPI * dia) * cosComp;
            double ny = sin(rx + px * zoomedPI * dia) * cosComp;
            double nz = sin(ry + py * zoomedPI);

            double rx = cx, ry = cy, rz = cz;
            boolean work = true;
            int iterations = 0;
            while (iterations < 1e3 && work) {
                double ax = 0, ay = 0, az = 0;
                double minDst = 1e300;

                int i = 0;
                while (i < len && work) {
                    double dx = data[i * 8] - rx;
                    double dy = data[i * 8 + 1] - ry;
                    double dz = data[i * 8 + 2] - rz;
                    double dst = pow(dx, 2) + pow(dy, 2) + pow(dz, 2);
                    double a = gravityConstant * data[i * 5 + 3] / dst;
                    dst = sqrt(dst);
                    a /= dst;
                    ax += a * dx;
                    ay += a * dy;
                    az += a * dz;

                    dst -= data[i * 8 + 4];
                    if (abs(dst) < 1e-1) {
                        if (color[i * 3] != 0 || color[i * 3 + 1] != 0 || color[i * 3 + 2] != 0) {

                            double spd = gravityConstant * data[i * 8 + 3] / data[i * 8 + 4];
                            if (spd < lightSpeed) {

                                double norx = rx - data[i * 8];
                                double nory = ry - data[i * 8 + 1];
                                double norz = rz - data[i * 8 + 2];
                                double norsqr = norx * norx + nory * nory + norz * norz;

                                double diffR = 0;
                                double diffG = 0;
                                double diffB = 0;

                                double specR = 0;
                                double specG = 0;
                                double specB = 0;

                                double lum = 0;

                                for (int j = 0; j < len; j++) {
                                    if (gravityConstant * data[j * 8 + 3] / data[j * 8 + 4] < lightSpeed) {
                                        double lx = data[j * 8] - rx;
                                        double ly = data[j * 8 + 1] - ry;
                                        double lz = data[j * 8 + 2] - rz;
                                        double lsqr = lx * lx + ly * ly + lz * lz;
                                        double diff = max(0, (lx * norx + ly * nory + lz * norz) / sqrt(lsqr * norsqr) * data[j * 8 + 5]);

                                        diffR += diff * color[j * 3];
                                        diffG += diff * color[j * 3 + 1];
                                        diffB += diff * color[j * 3 + 2];

                                        double coef = (lx * norx + ly * nory + lz * norz) / norsqr;
                                        double refx = lx - 2 * norx * coef;
                                        double refy = ly - 2 * nory * coef;
                                        double refz = lz - 2 * norz * coef;

                                        double spec = max(0, -(nx * refx + ny * refy + nz * refz) / sqrt(lsqr) * data[j * 8 + 5]);

                                        specR += spec * color[j * 3];
                                        specG += spec * color[j * 3 + 1];
                                        specB += spec * color[j * 3 + 2];

                                        lum += data[j * 8 + 5];
                                    }
                                }
//                            lum /= 10;

                                diffR *= data[i * 8 + 6] / lum / 255.0;
                                diffG *= data[i * 8 + 6] / lum / 255.0;
                                diffB *= data[i * 8 + 6] / lum / 255.0;

                                specR = pow(specR * data[i * 8 + 7] / lum / 255.0, specularity) * 255.0;
                                specG = pow(specG * data[i * 8 + 7] / lum / 255.0, specularity) * 255.0;
                                specB = pow(specB * data[i * 8 + 7] / lum / 255.0, specularity) * 255.0;

                                red += max(0, min(255, ((double) color[i * 3] * (data[i * 8 + 5] + diffR) + specR)));
                                green += max(0, min(255, ((double) color[i * 3 + 1] * (data[i * 8 + 5] + diffG) + specG)));
                                blue += max(0, min(255, ((double) color[i * 3 + 2] * (data[i * 8 + 5] + diffB) + specB)));
                            }
                        }
                        work = false;
                    } else if (dst < minDst) minDst = dst;
                    i++;
                }

                if (work) {
                    nx += ax / lightSpeed;
                    ny += ay / lightSpeed;
                    nz += az / lightSpeed;
                    double nor = sqrt(nx * nx + ny * ny + nz * nz);
                    nx /= nor;
                    ny /= nor;
                    nz /= nor;

                    rx += nx * minDst;
                    ry += ny * minDst;
                    rz += nz * minDst;
                    iterations++;
                }
            }
        }

        image[gid * 3] = (byte) round(red / samplingRate);
        image[gid * 3 + 1] = (byte) round(green / samplingRate);
        image[gid * 3 + 2] = (byte) round(blue / samplingRate);
    }
}
