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
    struct OpenCVSharpenRequest {
        char* output;
        char* input;
        int width;
        int height;

        int blurRadius;
        double strength;
    };


    EXPORTED void sharpen(OpenCVSharpenRequest* request)  {
        Mat inputMat(request->height, request->width, CV_8UC4, (void*)request->input);
        Mat outputMat(request->height, request->width, CV_8UC4, (void*)request->output);

        cv::Mat gaussian;
        cv::GaussianBlur(inputMat, gaussian, cv::Size(0, 0), request->blurRadius);
        cv::addWeighted(inputMat, 1.0 + request->strength, gaussian, -request->strength, 0, outputMat);
    }

}

