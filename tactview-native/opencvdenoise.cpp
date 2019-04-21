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
    struct OpenCVDenoiseRequest {
        char* output;
        char* input;
        int width;
        int height;

        int templateWindowSize;
        int searchWindowSize;
        double strength;
    };


    EXPORTED void denoise(OpenCVDenoiseRequest* request)  {
        Mat inputMat(request->height, request->width, CV_8UC4, (void*)request->input);
        Mat outputMat(request->height, request->width, CV_8UC4, (void*)request->output);

        cv::fastNlMeansDenoisingColored(inputMat, outputMat, request->strength, request->strength, request->templateWindowSize, request->searchWindowSize);
    }

}

