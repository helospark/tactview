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
    struct OpenCVThresholdRequest {
        char* output;
        char* input;
        int width;
        int height;

        int blockSize;
        int addedConstant;
    };


    EXPORTED void threshold(OpenCVThresholdRequest* request)  {
        Mat inputMat(request->height, request->width, CV_8UC4, (void*)request->input);
        Mat outputMat(request->height, request->width, CV_8UC4, (void*)request->output);

        std::vector<cv::Mat> matChannels;
        cv::split(inputMat, matChannels);

        Mat grayscaleMat;
        cv::cvtColor(inputMat, grayscaleMat, cv::COLOR_RGBA2GRAY, 0);

        Mat grayscaleOutput;
        cv::adaptiveThreshold(grayscaleMat, grayscaleOutput, 255, ADAPTIVE_THRESH_GAUSSIAN_C, cv::THRESH_BINARY, request->blockSize, request->addedConstant);


       std::vector<Mat> resultChannels;
       resultChannels.push_back(grayscaleOutput);
       resultChannels.push_back(grayscaleOutput);
       resultChannels.push_back(grayscaleOutput);
       resultChannels.push_back(matChannels[3]);
       merge(resultChannels, outputMat);
    }

}

