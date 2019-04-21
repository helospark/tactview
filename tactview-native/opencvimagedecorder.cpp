#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc.hpp>
#include <iostream>
#include "common.h"

using namespace cv;
using namespace std;

extern "C"
{

    typedef struct {
        char* path;
    } ImageMetadataRequest;

    typedef struct {
        int width;
        int height;
    } ImageMetadataResonse;

    EXPORTED ImageMetadataResonse readMetadata(ImageMetadataRequest* request)  {
        Mat mat = imread(request->path, cv::IMREAD_COLOR);
        ImageMetadataResonse response;
        response.width = mat.cols;
        response.height = mat.rows;
        return response;
    }

    typedef struct {
        char* path;
        int width;
        int height;
        char* data;
    } ImageRequest;

    EXPORTED void readImage(ImageRequest* request)
    {
        Mat input = imread(request->path, cv::IMREAD_COLOR);


        if(! input.data ) {
            cout <<  "Could not open or find the image" << std::endl ;
            return;
        }

        if (input.cols != request->width || input.rows != request->height) {
            cv::resize(input,input,Size(request->width,request->height));
        }


        //Mat output = Mat(request->width,request->height,CV_8UC4,request->data);
        cv::cvtColor(input, input, cv::COLOR_BGR2RGBA );
        std::cout << "read2 " << input.cols << " "  << input.rows << std::endl;
        memcpy(request->data, input.data, input.cols * input.rows * 4);
    }
}

