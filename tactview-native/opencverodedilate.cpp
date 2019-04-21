#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc.hpp>
#include <iostream>
#include "common.h"

using namespace cv;
using namespace std;

extern "C"
{

    struct OpenCVErodeDilateRequest {
        char* output;
        char* input;
        int width;
        int height;
        int kernelWidth;
        int kernelHeight;
        bool erode;
        int shape;
    };

    EXPORTED void erodeDilate(OpenCVErodeDilateRequest* request)  {
        Mat inputMat(request->height, request->width, CV_8UC4, (void*)request->input);
        Mat outputMat(request->height, request->width, CV_8UC4, (void*)request->output);

        int erosion_type = 0;
        if( request->shape == 0 ){
              erosion_type = MORPH_RECT;
        }
        else if( request->shape == 1 ){
          erosion_type = MORPH_CROSS;
        }
        else if( request->shape == 2) {
          erosion_type = MORPH_ELLIPSE;
        }
        Mat element = getStructuringElement( erosion_type,
                           Size( 2*request->kernelWidth + 1, 2*request->kernelHeight+1 ),
                           Point( request->kernelWidth, request->kernelHeight ) );

        if (request->erode) {
            erode( inputMat, outputMat, element );
        } else {
            dilate( inputMat, outputMat, element );
        }
    }

}

