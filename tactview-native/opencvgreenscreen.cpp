#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc.hpp>
#include <iostream>
#include "common.h"

using namespace cv;
using namespace std;


extern "C"
{

  struct OpenCVGreenScreenRequest {
      char* output;
      char* input;
      int width;
      int height;
  };

  EXPORTED void greenScreen(OpenCVGreenScreenRequest* request)  {
      Mat inputMat(request->height, request->width, CV_8UC4, (void*)request->input);
      Mat outputMat(request->height, request->width, CV_8UC4, (void*)request->output);

      cv::Mat hsv;
      cv::Mat rgbMat;
      cv::cvtColor(inputMat , rgbMat , cv::COLOR_RGBA2RGB);
      cvtColor(rgbMat,hsv,cv::COLOR_RGB2HSV);

      cv::Mat filtered;

      cv::inRange(hsv, cv::Scalar(50,30,00), cv::Scalar(80,255,255), filtered);

      cv::Mat result;
      cv::Mat mask = ~filtered;

      std::vector<cv::Mat> matChannels;
      cv::split(rgbMat, matChannels);

      matChannels.push_back(mask);
      merge(matChannels, outputMat);
  }
}
