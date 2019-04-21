#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc.hpp>
#include <iostream>
#include "common.h"

using namespace cv;
using namespace std;

extern "C"
{
    struct OpenCVRegion {
        int x;
        int y;
        int width;
        int height;
    };

    struct OpenCVGaussianBlurRequest {
        char* output;
        char* input;
        int width;
        int height;
        int kernelWidth;
        int kernelHeight;
        OpenCVRegion* blurRegion;
    };


    EXPORTED void applyGaussianBlur(OpenCVGaussianBlurRequest* request)  {
        Mat inputMat(request->height, request->width, CV_8UC4, (void*)request->input);
        Mat outputMat(request->height, request->width, CV_8UC4, (void*)request->output);
        inputMat.copyTo(outputMat);

        OpenCVRegion* blurRegion = request->blurRegion;

        cv::Rect region(blurRegion->x, blurRegion->y, blurRegion->width, blurRegion->height);
        cv::GaussianBlur(inputMat(region), outputMat(region), cv::Size(request->kernelWidth, request->kernelHeight), 0);
    }

}

