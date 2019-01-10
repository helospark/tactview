#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc.hpp>
#include <opencv2/photo.hpp>
#include <opencv2/calib3d.hpp>
#include <iostream>

using namespace cv;
using namespace std;

extern "C"
{
    struct OpenCVLensDistortRequest {
        char* output;
        char* input;
        int width;
        int height;

        int opticalCenterX;
        int opticalCenterY;
        double focalLength;

        double k1;
        double k2;
        double k3;
        double p1;
        double p2;
    };


    void lensDistort(OpenCVLensDistortRequest* request)  {
        Mat inputMat(request->height, request->width, CV_8UC4, (void*)request->input);
        Mat outputMat(request->height, request->width, CV_8UC4, (void*)request->output);

        Mat cameraMatrix(3,3,CV_64F);
        cameraMatrix.at<double>(0,0) = request->focalLength;
        cameraMatrix.at<double>(1,1) = request->focalLength;
        cameraMatrix.at<double>(2,2) = 1.0;
        cameraMatrix.at<double>(0,2) = request->opticalCenterX;
        cameraMatrix.at<double>(1,2) = request->opticalCenterY;

        Mat coefficientMatrix(1, 5, CV_64F);
        coefficientMatrix.at<double>(0,0) = request->k1;
        coefficientMatrix.at<double>(0,1) = request->k2;
        coefficientMatrix.at<double>(0,2) = request->p1;
        coefficientMatrix.at<double>(0,3) = request->p2;
        coefficientMatrix.at<double>(0,4) = request->k3;

        cv::undistort(inputMat, outputMat, cameraMatrix, coefficientMatrix);
    }

}

