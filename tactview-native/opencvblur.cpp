#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc.hpp>
#include <iostream>

using namespace cv;
using namespace std;

extern "C"
{

    typedef struct {
        char* output;
        char* input;
        int width;
        int height;
        int kernelWidth;
        int kernelHeight;
    } OpenCVGaussianBlurRequest;

    void applyGaussianBlur(OpenCVGaussianBlurRequest* request)  {
        Mat inputMat(request->height, request->width, CV_8UC4, (void*)request->input);
        Mat outputMat(request->height, request->width, CV_8UC4, (void*)request->output);

        cv::GaussianBlur(inputMat, outputMat, cv::Size(request->kernelWidth, request->kernelHeight), 0);
    }

}

