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
    struct OpenCVCartoonRequest {
        char* output;
        char* input;
        int width;
        int height;
    };


    EXPORTED void cartoon(OpenCVCartoonRequest* request)  {
        Mat inputMat(request->height, request->width, CV_8UC4, (void*)request->input);
        Mat outputMat(request->height, request->width, CV_8UC4, (void*)request->output);

        std::vector<cv::Mat> matChannels;
        cv::split(inputMat, matChannels);

        Mat rgbImage;
        cv::cvtColor(inputMat, rgbImage, cv::COLOR_RGBA2RGB, 0);

        stylization(rgbImage,rgbImage);

        std::vector<Mat> resultChannels;
        cv::split(rgbImage, resultChannels);
        resultChannels.push_back(matChannels[3]);
        merge(resultChannels, outputMat);
    }

}

