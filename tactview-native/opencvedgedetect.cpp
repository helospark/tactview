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
    struct OpenCVEdgeDetectRequest {
        char* output;
        char* input;
        int width;
        int height;

        double lowThreshold;
        double highThresholdMultiplier;
        int apertureSize;
    };


    EXPORTED void edgeDetect(OpenCVEdgeDetectRequest* request)  {
        Mat inputMat(request->height, request->width, CV_8UC4, (void*)request->input);
        Mat outputMat(request->height, request->width, CV_8UC4, (void*)request->output);

        std::vector<cv::Mat> matChannels;
        cv::split(inputMat, matChannels);

        Mat singleChannelInputMat;
        cv::cvtColor(inputMat, singleChannelInputMat ,cv::COLOR_BGRA2GRAY);

        Mat singleChannelOutputMat;
        cv::Canny(singleChannelInputMat, singleChannelOutputMat, request->lowThreshold, request->lowThreshold * request->highThresholdMultiplier, request->apertureSize);

        std::vector<Mat> resultChannels;
        resultChannels.push_back(singleChannelOutputMat);
        resultChannels.push_back(singleChannelOutputMat);
        resultChannels.push_back(singleChannelOutputMat);
        resultChannels.push_back(matChannels[3]);
        merge(resultChannels, outputMat);
    }

}

