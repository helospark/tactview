 #include <opencv2/highgui/highgui.hpp>
 #include <opencv2/opencv.hpp>
 #include <iostream>
 #include <stdio.h>
 #include "common.h"

using namespace cv;
using namespace std;

extern "C" {


  struct OpenCVHistogramEquizationRequest {
      char* output;
      char* input;
      int width;
      int height;
    
      int adaptiveClipLimit;
      int adaptiveKernelWidth;
      int adaptiveKernelHeight;

      int grayscale;
      int adaptive;
  };

  void process(Mat image, OpenCVHistogramEquizationRequest* request, Mat& outputMat);

  EXPORTED void equizeHistogram(OpenCVHistogramEquizationRequest* request)  {
    Mat inputMat(request->height, request->width, CV_8UC4, (void*)request->input);
    Mat outputMat(request->height, request->width, CV_8UC4, (void*)request->output);

    std::vector<cv::Mat> matChannels;
    cv::split(inputMat, matChannels);

    if (request->grayscale) {
      Mat grayImage;
      cv::cvtColor(inputMat, grayImage, cv::COLOR_RGBA2GRAY);
      
      process(grayImage, request, grayImage);

      std::vector<cv::Mat> outputImageChannels;
      outputImageChannels.push_back(grayImage);
      outputImageChannels.push_back(grayImage);
      outputImageChannels.push_back(grayImage);
      outputImageChannels.push_back(matChannels[3]);
      cv::merge(outputImageChannels, outputMat);
    } else {
      Mat lab_image;
      Mat rgbImage;
      cv::cvtColor(inputMat, rgbImage, cv::COLOR_RGBA2RGB);
      cv::cvtColor(rgbImage, lab_image, cv::COLOR_RGB2Lab);
      std::vector<cv::Mat> lab_planes(3);
      cv::split(lab_image, lab_planes);

      process(lab_planes[0], request, lab_planes[0]);

      Mat image_clahe;
      cv::merge(lab_planes, lab_image);
      cv::cvtColor(lab_image, image_clahe, cv::COLOR_Lab2RGB);
      
      std::vector<cv::Mat> outputImageChannels;
      cv::split(image_clahe, outputImageChannels);
      outputImageChannels.push_back(matChannels[3]);
      cv::merge(outputImageChannels, outputMat);
    }

  }

  void process(Mat image, OpenCVHistogramEquizationRequest* request, Mat& outputMat) {
    if (request->adaptive) {
      cv::Ptr<cv::CLAHE> clahe = cv::createCLAHE(request->adaptiveClipLimit, cv::Size(request->adaptiveKernelWidth, request->adaptiveKernelHeight));
      clahe->setClipLimit(request->adaptiveClipLimit);

      clahe->apply(image, outputMat);
    } else {
      equalizeHist(image, outputMat);
    }
  }
}
