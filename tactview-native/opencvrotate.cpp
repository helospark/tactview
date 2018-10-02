#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc.hpp>
#include <iostream>

using namespace cv;
using namespace std;

// https://stackoverflow.com/a/33564950

extern "C"
{

    typedef struct {
        char* input;
        int originalWidth;
        int originalHeight;

        char* output;
        int newWidth;
        int newHeight;

        int rotationPointX;
        int rotationPointY;
        double rotationDegrees;
    } OpenCVRotateRequest;

    void rotateImage(OpenCVRotateRequest* request)  {
        Mat inputMat(request->originalHeight, request->originalWidth, CV_8UC4, (void*)request->input);
        Mat outputMat(request->newHeight, request->newWidth, CV_8UC4, (void*)request->output);

        Point2f center(request->rotationPointX, request->rotationPointY);
        Mat rot = getRotationMatrix2D(center, request->rotationDegrees, 1.0);
        rot.at<double>(0, 2) += ((request->newWidth / 2) - request->rotationPointX);
        rot.at<double>(1, 2) += ((request->newHeight / 2) - request->rotationPointY);
        
        warpAffine(inputMat, outputMat, rot, Size(request->newWidth, request->newHeight));
    }

}

