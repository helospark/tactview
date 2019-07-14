package com.helospark.tactview.core.util.lut.lutre3d;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.util.lut.cube.AbstractCubeLut;
import com.helospark.tactview.core.util.lut.cube.CubeLut3d;
import com.helospark.tactview.core.util.lut.cube.LutColor;

@Component
public class Lutre3dLoader {

    public AbstractCubeLut readLut(String fileName) {
        try (InputStream inputStream = new FileInputStream(new File(fileName))) {
            return readLut(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public AbstractCubeLut readLut(InputStream inputStream) {
        try (BufferedReader fos = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return readFile(fos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static enum LutType {
        LUSTRE,
        FLAME
    }

    static class Lutre3dBuilder {
        List<Integer> shapingValues;
        LutType type;
        Integer inputBitDepth;
        Integer outputBitDepth;
        Integer size;
    }

    private AbstractCubeLut readFile(BufferedReader fos) throws IOException {
        Lutre3dBuilder builder = new Lutre3dBuilder();
        parseHeader(fos, builder);
        LutColor[][][] values = readRawTable(fos, builder);

        return CubeLut3d.builder()
                .withTitle("Undefined")
                .withLowerBound(new float[]{0, 0, 0})
                .withUpperBound(new float[]{1, 1, 1})
                .withSize(builder.size)
                .withValues(values)
                .build();
    }

    private LutColor[][][] readRawTable(BufferedReader fos, Lutre3dBuilder builder) throws IOException {
        int dimensions = builder.size;
        LutColor[][][] values = new LutColor[dimensions][dimensions][dimensions];
        float maxValue = 0.0f;
        for (int z = 0; z < dimensions; ++z) {
            for (int y = 0; y < dimensions; ++y) {
                for (int x = 0; x < dimensions; ++x) {
                    String[] triplet = readLine(fos).split("\\s+");
                    if (triplet.length != 3) {
                        throw new RuntimeException("RGB color expected");
                    }
                    float redValue = Float.parseFloat(triplet[0]);
                    float greenValue = Float.parseFloat(triplet[1]);
                    float blueValue = Float.parseFloat(triplet[2]);
                    values[x][y][z] = new LutColor(redValue, greenValue, blueValue);

                    maxValue = Math.max(maxValue, Math.max(redValue, Math.max(greenValue, blueValue)));
                }
            }
        }

        Integer outputBitDepth = builder.outputBitDepth;
        if (outputBitDepth == null) {
            outputBitDepth = (int) Math.ceil(Math.log(maxValue) / Math.log(2));
        }

        float intervalMax = (float) Math.pow(2, outputBitDepth);

        for (int x = 0; x < dimensions; ++x) {
            for (int y = 0; y < dimensions; ++y) {
                for (int z = 0; z < dimensions; ++z) {
                    values[z][y][x].r /= intervalMax;
                    values[z][y][x].g /= intervalMax;
                    values[z][y][x].b /= intervalMax;
                }
            }
        }
        return values;
    }

    private void parseHeader(BufferedReader fos, Lutre3dBuilder builder) throws IOException {
        int i = 0;

        while (i++ < 1000) {
            fos.mark(1000);
            String sizeLine = readLine(fos);
            String[] parts = sizeLine.split("\\s+");

            if (parts.length == 0) {
            } else if (parts.length > 3) {
                parseShapers(builder, parts);
            } else if (parts[0].equals("3DMESH")) {
                builder.type = LutType.LUSTRE;
            } else if (parts[0].equals("MESH")) {
                builder.inputBitDepth = Integer.parseInt(parts[1]);
                builder.outputBitDepth = Integer.parseInt(parts[2]);
            } else {
                fos.reset();
                break;
            }
        }

        if (builder.type == null) {
            builder.type = LutType.FLAME;
        }
        if (builder.shapingValues != null) {
            builder.size = builder.shapingValues.size();
        } else if (builder.inputBitDepth != null) {
            builder.size = (int) Math.pow(2, builder.inputBitDepth);
        } else {
            throw new RuntimeException("Unable to determine the 3dl input bit depth");
        }
    }

    private void parseShapers(Lutre3dBuilder builder, String[] parts) {
        builder.shapingValues = Arrays.stream(parts)
                .map(a -> Integer.parseInt(a))
                .collect(Collectors.toList());
        //        int diff = builder.shapingValues.get(1) - builder.shapingValues.get(0);
        //
        //        for (int i = 2; i < builder.shapingValues.size(); ++i) {
        //            if (builder.shapingValues.get(i) - builder.shapingValues.get(i - 1) != diff) {
        //                //throw new RuntimeException("Uniform color cube segmentation expected");
        //            }
        //        }
    }

    private String readLine(BufferedReader fos) throws IOException {
        String line = "";

        do {
            line = fos.readLine();
            if (line == null) {
                throw new IllegalStateException("Premature end of file");
            }
        } while (line.isEmpty() || line.startsWith("#"));

        return line;
    }

}
