package com.helospark.tactview.core.util.lut.cube;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.function.BiConsumer;

import com.helospark.lightdi.annotation.Component;

@Component
public class CubeLutLoader {

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

    enum CubeType {
        ONE_DIMENSION,
        THREE_DIMENSION
    }

    class CubeLutHeaderBuilder {
        float[] lowerBound = new float[]{0.0f, 0.0f, 0.0f};
        float[] upperBound = new float[]{1.0f, 1.0f, 1.0f};

        String title = "Undefined";
        CubeType type;
        Integer size;

        @Override
        public String toString() {
            return "CubeLutHeaderBuilder [upperBound=" + Arrays.toString(upperBound) + ", lowerBound=" + Arrays.toString(lowerBound) + ", title=" + title + ", type=" + type + ", size=" + size + "]";
        }

    }

    Map<String, BiConsumer<String, CubeLutHeaderBuilder>> operations = Map.of(
            "TITLE", (values, builder) -> {
                if (values.charAt(0) != '"' || values.charAt(values.length() - 1) != '"') {
                    throw new RuntimeException("Title should start and end with quote");
                }
                builder.title = values.substring(1, values.length() - 1);
            },
            "DOMAIN_MIN", (values, builder) -> {
                builder.lowerBound = parseTriplet(values);
            },
            "DOMAIN_MAX", (values, builder) -> {
                builder.upperBound = parseTriplet(values);
            },
            "LUT_1D_SIZE", (values, builder) -> {
                int size = Integer.parseInt(values);
                if (size < 2 || size > 65536) {
                    throw new RuntimeException("Invalid range");
                }
                builder.size = size;
                builder.type = CubeType.ONE_DIMENSION;
            },
            "LUT_3D_SIZE", (values, builder) -> {
                int size = Integer.parseInt(values);
                if (size < 2 || size > 256) {
                    throw new RuntimeException("Invalid range");
                }
                builder.size = size;
                builder.type = CubeType.THREE_DIMENSION;
            });

    private float[] parseTriplet(String values) {
        String[] elements = values.split("\\s+");
        if (elements.length != 3) {
            throw new RuntimeException("3 numbers expected for bounds");
        }
        float[] triplet = new float[3];
        for (int i = 0; i < elements.length; ++i) {
            triplet[i] = Float.parseFloat(elements[i]);
        }
        return triplet;
    }

    static final int MAX_HEADER_SIZE = 1000;

    private AbstractCubeLut readFile(BufferedReader fos) throws IOException {
        CubeLutHeaderBuilder headerBuilder = readHeader(fos);
        System.out.println("Read header " + headerBuilder);
        return readBody(fos, headerBuilder);
    }

    private AbstractCubeLut readBody(BufferedReader fos, CubeLutHeaderBuilder headerBuilder) throws IOException {
        if (headerBuilder.type.equals(CubeType.ONE_DIMENSION)) {
            return readOneDimensionalTable(fos, headerBuilder);
        } else {
            return readThreeDimensionalTable(fos, headerBuilder);
        }
    }

    private AbstractCubeLut readThreeDimensionalTable(BufferedReader fos, CubeLutHeaderBuilder headerBuilder) throws IOException {
        rgbvec[][][] values = new rgbvec[headerBuilder.size][headerBuilder.size][headerBuilder.size];
        for (int b = 0; b < headerBuilder.size; ++b) {
            values[b] = new rgbvec[headerBuilder.size][headerBuilder.size];
            for (int g = 0; g < headerBuilder.size; ++g) {
                values[b][g] = new rgbvec[headerBuilder.size];
                for (int r = 0; r < headerBuilder.size; ++r) {
                    String line = readLine(fos);
                    float[] triplet = parseTriplet(line);
                    values[b][g][r] = new rgbvec(triplet[0], triplet[1], triplet[2]);
                }
            }
        }
        return CubeLut3d.builder()
                .withLowerBound(headerBuilder.lowerBound)
                .withUpperBound(headerBuilder.upperBound)
                .withTitle(headerBuilder.title)
                .withValues(values)
                .withSize(headerBuilder.size)
                .build();
    }

    private AbstractCubeLut readOneDimensionalTable(BufferedReader fos, CubeLutHeaderBuilder headerBuilder) throws IOException {
        rgbvec[] values = new rgbvec[headerBuilder.size];
        for (int i = 0; i < headerBuilder.size; ++i) {
            String line = readLine(fos);
            float[] triplet = parseTriplet(line);
            values[i] = new rgbvec(triplet[0], triplet[1], triplet[2]);
        }
        return CubeLut1d.builder()
                .withLowerBound(headerBuilder.lowerBound)
                .withUpperBound(headerBuilder.upperBound)
                .withTitle(headerBuilder.title)
                .withValues(values)
                .withSize(headerBuilder.size)
                .build();
    }

    private CubeLutHeaderBuilder readHeader(BufferedReader fos) throws IOException {
        CubeLutHeaderBuilder builder = new CubeLutHeaderBuilder();

        int i = 0;
        while (i++ < MAX_HEADER_SIZE) {
            String line = readLine(fos);
            int spaceIndex = line.indexOf(" ");
            String keyword = line.substring(0, spaceIndex).trim();

            if (keyword.charAt(0) > '+' && keyword.charAt(0) < ':') {
                fos.reset();
                break;
            }

            String restOfLine = line.substring(spaceIndex + 1).trim();

            BiConsumer<String, CubeLutHeaderBuilder> operation = operations.get(keyword);
            if (operation == null) {
                throw new RuntimeException("Illegal keyword " + keyword);
            }
            operation.accept(restOfLine, builder);
            fos.mark(1000);
        }
        if (i >= MAX_HEADER_SIZE) {
            throw new RuntimeException("Header too large");
        }
        return builder;
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
