#include "common.h"

extern "C"
{
    struct AlphaBlendRequest {
        unsigned char* foreground;
        unsigned char* backgroundAndResult;

        int width;
        int height;

        double alpha;
    };


    inline unsigned char saturateIfNeeded(double input) {
        if (input > 255.0) {
            return 255;
        } else {
            return (unsigned char) input;
        }
    }

    EXPORTED void normalAlphablend(AlphaBlendRequest* request)  {

        for (int y = 0; y < request->height; ++y) {
            for (int x = 0; x < request->width; ++x) {
                int startIndex = y * 4 * request->width + x * 4;
                unsigned char uForegroundAlpha = request->foreground[startIndex + 3];

                if (uForegroundAlpha == 255 && request->alpha == 255) {
                    for (int c = 0; c < 4; ++c) {
                        request->backgroundAndResult[startIndex + c] = request->foreground[startIndex + c];
                    }
                } else {
                    double foregroundAlpha = uForegroundAlpha / 255.0;
                    double alpha = foregroundAlpha * request->alpha;
                    double backgroundAlpha = request->backgroundAndResult[startIndex + 3] / 255.0;

                    for (int c = 0; c < 3; ++c) {
                        double output = (request->foreground[startIndex + c] * alpha + request->backgroundAndResult[startIndex + c] * (1.0 - alpha));
                        request->backgroundAndResult[startIndex + c] = saturateIfNeeded(output);
                    }
                    double newAlpha = ((foregroundAlpha + backgroundAlpha * (1.0 - foregroundAlpha)) * 255.0);

                    request->backgroundAndResult[startIndex + 3] = saturateIfNeeded(newAlpha);
                }
            }
        }
    }

}

