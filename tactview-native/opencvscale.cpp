#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc.hpp>
#include <iostream>

using namespace cv;
using namespace std;

extern "C"
{

    typedef struct {
        char* input;     
        char* output;    
                              
        int originalWidth;    
        int originalHeight;   
                              
        int newWidth;         
        int newHeight;        
                              
        int interpolationType;
    } OpenCVScaleRequest;

    void scaleImage(OpenCVScaleRequest* request)  {
        Mat inputMat(request->originalHeight, request->originalWidth, CV_8UC4, (void*)request->input);
        Mat outputMat(request->newHeight, request->newWidth, CV_8UC4, (void*)request->output);

        cv::resize(inputMat, outputMat, cv::Size(request->newWidth, request->newHeight));
    }

}

