
// 0 - x;
// 1 - y;
// 2 - z;
// 3 - mass;
// 4 - radius;
// 5 - r;
// 6 - g;
// 7 - b;
// 8 - luminosity;
// 9 - diffusion;
// 10 - specular;

extern "C"
__global__ void render(int width, int height, int len, 
                    int maxSteps, float maxStepDistance,
                    float gravityConstant, float lightSpeed,
                    float cx, float cy, float cz,
                    float rx, float ry, float fov, float specularity,
                    float* data, int8_t* image) {
    int x = threadIdx.x + blockDim.x * blockIdx.x;
    int y = threadIdx.y + blockDim.y * blockIdx.y;

    if (x < width && y < height) {
        int index = x + y * width;
        image[index * 3] = -128;
        image[index * 3 + 1] = -128;
        image[index * 3 + 2] = -128;

        float factor = 0.0174532925 * fov;
        float ax = (2.0 * x / width - 1) * factor;
        float ay = (2.0 * y / height - 1) * factor * height / width;

        float cosComp = cos(ry + ay);
        float nx = cos(rx + ax) * cosComp;
        float ny = sin(rx + ax) * cosComp;
        float nz = sin(ry + ay);

        float rpx = cx, rpy = cy, rpz = cz;

        bool work = true;
        int iterations = 0;
        while (iterations < maxSteps && work) {
            float ax = 0, ay = 0, az = 0;
            float minDst = maxStepDistance;

            int i = 0;
            while (i < len && work) {
                float dx = data[i * 11] - rpx;
                float dy = data[i * 11 + 1] - rpy;
                float dz = data[i * 11 + 2] - rpz;
                float dst = pow(dx, 2) + pow(dy, 2) + pow(dz, 2);
                float a = gravityConstant * data[i * 5 + 3] / dst;
                dst = sqrt(dst);
                a /= dst;
                ax += a * dx;
                ay += a * dy;
                az += a * dz;

                dst -= data[i * 11 + 4];
                if (abs(dst) < 1e-1) {
                    if (data[i * 11 + 5] != 0 || data[i * 11 + 6] != 0 || data[i * 11 + 7] != 0) {

                        float spd = gravityConstant * data[i * 11 + 3] / data[i * 11 + 4];
                        if (spd < lightSpeed) {

                            float norx = rpx - data[i * 11];
                            float nory = rpy - data[i * 11 + 1];
                            float norz = rpz - data[i * 11 + 2];
                            float norsqr = norx * norx + nory * nory + norz * norz;

                            float diffR = 0;
                            float diffG = 0;
                            float diffB = 0;

                            float specR = 0;
                            float specG = 0;
                            float specB = 0;

                            float lum = 0;

                            for (int j = 0; j < len; j++) {
                                if (gravityConstant * data[j * 8 + 3] / data[j * 8 + 4] < lightSpeed) {
                                    float lx = data[j * 11] - rpx;
                                    float ly = data[j * 11 + 1] - rpy;
                                    float lz = data[j * 11 + 2] - rpz;
                                    float lsqr = lx * lx + ly * ly + lz * lz;
                                    float diff = max(0.0, (lx * norx + ly * nory + lz * norz) / sqrt(lsqr * norsqr) * data[j * 11 + 9]);

                                    diffR += diff * data[j * 11 + 5];
                                    diffG += diff * data[j * 11 + 6];
                                    diffB += diff * data[j * 11 + 7];

                                    float coef = (lx * norx + ly * nory + lz * norz) / norsqr;
                                    float refx = lx - 2 * norx * coef;
                                    float refy = ly - 2 * nory * coef;
                                    float refz = lz - 2 * norz * coef;

                                    float spec = max(0.0, -(nx * refx + ny * refy + nz * refz) / sqrt(lsqr) * data[j * 11 + 10]);

                                    specR += spec * data[j * 11 + 5];
                                    specG += spec * data[j * 11 + 6];
                                    specB += spec * data[j * 11 + 7];

                                    lum += data[j * 11 + 8];
                                }
                            }
//                            lum /= 10;

                            diffR *= data[i * 11 + 9] / lum;
                            diffG *= data[i * 11 + 9] / lum;
                            diffB *= data[i * 11 + 9] / lum;

                            specR = pow(specR * data[i * 11 + 10] / lum, specularity);
                            specG = pow(specG * data[i * 11 + 10] / lum, specularity);
                            specB = pow(specB * data[i * 11 + 10] / lum, specularity);

                            image[index * 3] = 255.0 * max(0.0, min(1.0, (data[i * 11 + 5] * (data[i * 11 + 8] + diffR) + specR))) - 128;
                            image[index * 3 + 1] = 255.0 * max(0.0, min(1.0, (data[i * 11 + 6] * (data[i * 11 + 8] + diffG) + specG))) - 128;
                            image[index * 3 + 2] = 255.0 * max(0.0, min(1.0, (data[i * 11 + 7] * (data[i * 11 + 8] + diffB) + specB))) - 128;
                        }
                    }
                    work = false;
                }
                else if (dst < minDst) minDst = dst;
                i++;
            }

            if (work) {
                nx += ax / lightSpeed;
                ny += ay / lightSpeed;
                nz += az / lightSpeed;
                float nor = sqrt(nx * nx + ny * ny + nz * nz);
                nx /= nor;
                ny /= nor;
                nz /= nor;

                rpx += nx * minDst;
                rpy += ny * minDst;
                rpz += nz * minDst;
                iterations++;
            }
        }
    }
}