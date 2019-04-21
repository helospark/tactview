#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc.hpp>
#include <opencv2/photo.hpp>
#include <iostream>
#include "common.h"

using namespace cv;
using namespace std;

extern "C"
{
    struct OpenCVTransformRequest {
        char* output;
        char* input;
        int width;
        int height;

        int matrixWidth;
        int matrixHeight;
        float* matrix;
    };


    EXPORTED void transform(OpenCVTransformRequest* request)  {
        Mat inputMat(request->height, request->width, CV_8UC4, (void*)request->input);
        Mat outputMat(request->height, request->width, CV_8UC4, (void*)request->output);
        Mat convolutionMatrix(request->matrixHeight, request->matrixWidth, CV_32FC1, (void*)request->matrix);

        cv::transform(inputMat, outputMat, convolutionMatrix);
    }

}

