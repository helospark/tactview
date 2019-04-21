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
    struct OpenCVPencilSketchRequest {
        char* output;
        char* input;
        int width;
        int height;

        double sigmaS;
        double sigmaR;
        double shadeFactor;

        bool color;
    };


    EXPORTED void pencilSketch(OpenCVPencilSketchRequest* request)  {
        Mat inputMat(request->height, request->width, CV_8UC4, (void*)request->input);
        Mat outputMat(request->height, request->width, CV_8UC4, (void*)request->output);

        std::vector<cv::Mat> matChannels;
        cv::split(inputMat, matChannels);
        Mat rgbImage;
        cv::cvtColor(inputMat, rgbImage, cv::COLOR_RGBA2RGB, 0);

        cv::Mat grayPencil, colorPencil;
        cv::pencilSketch(rgbImage, grayPencil, colorPencil, request->sigmaS, request->sigmaR, request->shadeFactor);

        if (request->color) {
          std::vector<Mat> resultChannels;
          cv::split(colorPencil, resultChannels);
          resultChannels.push_back(matChannels[3]);
          merge(resultChannels, outputMat);
        } else {
          std::vector<Mat> resultChannels;
          resultChannels.push_back(grayPencil);
          resultChannels.push_back(grayPencil);
          resultChannels.push_back(grayPencil);
          resultChannels.push_back(matChannels[3]);
          merge(resultChannels, outputMat);
        }

    }

}

