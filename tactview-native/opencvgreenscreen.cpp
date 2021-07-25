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

      int hueMin, hueMax;
      int saturationMin, saturationMax;
      int valueMin, valueMax;

      int spillRemovalEnabled;

      int spillDeltaHue;
      int spillSaturationThreshold;
      int spillValueThreshold;

      int enableEdgeBlur;
      int edgeBlurRadius;
  };

  EXPORTED void greenScreen(OpenCVGreenScreenRequest* request)  {
      Mat inputMat(request->height, request->width, CV_8UC4, (void*)request->input);
      Mat outputMat(request->height, request->width, CV_8UC4, (void*)request->output);

      cv::Mat hsv;
      cv::Mat rgbMat;
      cv::cvtColor(inputMat , rgbMat , cv::COLOR_RGBA2RGB);
      cvtColor(rgbMat,hsv,cv::COLOR_RGB2HSV);

      cv::Mat filtered;

      cv::inRange(hsv, cv::Scalar(request->hueMin, request->saturationMin, request->valueMin), cv::Scalar(request->hueMax,request->saturationMax, request->valueMax), filtered);

      cv::Mat result;

      std::vector<cv::Mat> matChannels;

// TODO: Causes crash on Apple
#ifndef __APPLE_
      if (request->enableEdgeBlur && request->edgeBlurRadius > 0) {
         int width = request->edgeBlurRadius;
         Mat element = getStructuringElement( MORPH_ELLIPSE,
                           Size( 2*width + 1, 2*width+1 ),
                           Point( width, width ) );
         //cv::dilate(filtered, filtered, element);
         cv::blur(filtered, filtered, cv::Size(width, width));
      }
#endif
      cv::Mat mask = ~filtered;

      if (request->spillRemovalEnabled) {
        for(int i=0; i<rgbMat.rows; i++) {
            for(int j=0; j<rgbMat.cols; j++) {
              cv::Vec3b hsvColor = hsv.at<cv::Vec3b>(i, j);
              cv::Vec3b& rgbColor = rgbMat.at<cv::Vec3b>(i,j);
              double r = rgbColor[0] / 255.0;
              double g = rgbColor[1] / 255.0;
              double b = rgbColor[2] / 255.0;

              if(hsvColor[0] >= request->hueMin - request->spillDeltaHue &&
                 hsvColor[0] <= request->hueMax + request->spillDeltaHue &&
                 hsvColor[1] >= request->spillSaturationThreshold &&
                 hsvColor[2] > request->spillValueThreshold){
                    if((r*b) !=0 && (g*g) / (r*b) >= 1.5){
                      r *= 1.4;
                      g *= 0.9;
                      b *= 1.4;
                    } else{
                      r *= 1.2;
                      g *= 0.9;
                      b *= 1.2;
                    }

                    uint newR = r * 255;
                    uint newG = g * 255;
                    uint newB = b * 255;

                    if (newR > 255) {
                      newR = 255;
                    }
                    if (newG > 255) {
                      newG = 255;
                    }
                    if (newB > 255) {
                      newB = 255;
                    }
                    rgbMat.at<cv::Vec3b>(i,j) = Vec3b(newR, newG, newB);
              }
          }
        }
    }


    cv::split(rgbMat, matChannels);
    matChannels.push_back(mask);
    merge(matChannels, outputMat);

  }
}
