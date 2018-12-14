package com.helospark.tactview.core.timeline.effect.transform.service;

import com.helospark.lightdi.annotation.Service;
import com.helospark.tactview.core.timeline.effect.transform.implementation.OpenCVGenericTransformation;
import com.helospark.tactview.core.timeline.effect.transform.implementation.OpenCVTransformRequest;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

@Service
public class GenericMatrixTransformationService {
    private static final int MATRIX_HEIGHT = 4;
    private static final int MATRIX_WIDTH = 4;
    private OpenCVGenericTransformation openCVGenericTransformation;

    public GenericMatrixTransformationService(OpenCVGenericTransformation openCVGenericTransformation) {
        this.openCVGenericTransformation = openCVGenericTransformation;
    }

    public ClipImage transform(TransformationServiceRequest request) {
        ClipImage result = ClipImage.sameSizeAs(request.getImage());

        Pointer matrixPointer = new Memory(MATRIX_WIDTH * MATRIX_HEIGHT * Native.getNativeSize(Float.TYPE));

        for (int y = 0; y < MATRIX_HEIGHT; ++y) {
            for (int x = 0; x < MATRIX_WIDTH; ++x) {
                setPointer(matrixPointer, y == x ? 1.0f : 0.0f, y, x);
            }
        }

        float[][] matrix = request.getConvolutionMatrix();
        int matrixWidth = matrix[0].length;
        int matrixHeight = matrix.length;
        for (int y = 0; y < matrixHeight; ++y) {
            for (int x = 0; x < matrixWidth; ++x) {
                setPointer(matrixPointer, matrix[y][x], y, x);
            }
        }

        if (request.isFlipRedAndBlue()) {
            for (int i = 0; i < MATRIX_WIDTH; ++i) {
                float red = getFloat(matrixPointer, 0, i);
                float blue = getFloat(matrixPointer, 2, i);
                setPointer(matrixPointer, red, 2, i);
                setPointer(matrixPointer, blue, 0, i);
            }
        }

        OpenCVTransformRequest nativeRequest = new OpenCVTransformRequest();
        nativeRequest.input = request.getImage().getBuffer();
        nativeRequest.output = result.getBuffer();
        nativeRequest.width = request.getImage().getWidth();
        nativeRequest.height = request.getImage().getHeight();
        nativeRequest.matrix = matrixPointer;
        nativeRequest.matrixWidth = MATRIX_WIDTH;
        nativeRequest.matrixHeight = MATRIX_HEIGHT;

        openCVGenericTransformation.transform(nativeRequest);

        return result;
    }

    private float getFloat(Pointer matrixPointer, int y, int x) {
        int floatSize = Native.getNativeSize(Float.TYPE);
        return matrixPointer.getFloat(y * floatSize * MATRIX_WIDTH + x * floatSize);
    }

    private void setPointer(Pointer matrixPointer, float value, int y, int x) {
        int floatSize = Native.getNativeSize(Float.TYPE);
        matrixPointer.setFloat(y * floatSize * MATRIX_WIDTH + x * floatSize, value);
    }

}
