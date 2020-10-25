#include <opencv2/opencv.hpp>
#include <opencv2/calib3d/calib3d.hpp>
#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <iostream>
#include <sstream>
#include <string>
#include <fcntl.h>
#include <unistd.h>
#include <sys/ioctl.h>
#include <linux/videodev2.h>
#include <string>

using namespace std;
using namespace cv;

extern "C" {


struct ImageToLoopbackRequest {
    int width;
    int height;

    unsigned char* image;
    const char* loopbackDevice;
};

std::string v4l2Cache = "";
int v4l2lo = -1;

  void sendImageToLoopbackCamera(ImageToLoopbackRequest* request) {
        string cacheKey = string(request->loopbackDevice) + "_" + std::to_string(request->width) + "x" +  std::to_string(request->height);

        if (v4l2Cache != cacheKey) {
            if (v4l2lo != -1) {
                std::cout << "Closing v4l2 device" << std::endl;
                close(v4l2lo);
                v4l2lo = -1;
            }
            std::cout << "Init v4l2 device with cache key "<< cacheKey << std::endl;

            int width  = request->width;
            int height = request->height;

            std::cout << width << " x " << height << std::endl;

            v4l2lo = open(request->loopbackDevice, O_WRONLY);
            if(v4l2lo < 0) {
                std::cout << "Error opening v4l2l device: " << strerror(errno);
                return;
            } else {
                v4l2Cache = cacheKey;

                struct v4l2_format v;
                int t;
                v.type = V4L2_BUF_TYPE_VIDEO_OUTPUT;
                t = ioctl(v4l2lo, VIDIOC_G_FMT, &v);
                if( t < 0 ) {
                    exit(t);
                }
                v.fmt.pix_mp.width = width;
                v.fmt.pix_mp.height = height;
                v.fmt.pix.width = width;
                v.fmt.pix.height = height;
                v.fmt.pix.pixelformat = V4L2_PIX_FMT_RGB24;
                v.fmt.pix.sizeimage = width * height * 3;
                t = ioctl(v4l2lo, VIDIOC_S_FMT, &v);
                if( t < 0 ) {
                    std::cout << "Unable to call ioctl" << std::endl;
                    return;
                }
            }
        }
        

        Mat inputFrame(request->height, request->width, CV_8UC4, (void*)request->image);
        Mat rgbMat;
        cvtColor(inputFrame, rgbMat, cv::COLOR_RGBA2RGB);
        
        size_t written = write(v4l2lo, rgbMat.data, rgbMat.cols * rgbMat.rows * rgbMat.elemSize());
        
        if (written < 0) {
            std::cout << "Error writing v4l2l device";
        }
    }

}
