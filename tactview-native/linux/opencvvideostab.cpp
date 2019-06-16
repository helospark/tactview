#include <string>
#include <iostream>
#include <fstream>
#include <sstream>
#include <stdexcept>
#include <map>
#include <opencv2/core.hpp>
#include <opencv2/core/utility.hpp>
#include <opencv2/video.hpp>
#include <opencv2/imgproc.hpp>
#include <opencv2/videoio.hpp>
#include <opencv2/highgui.hpp>
#include <opencv2/videostab.hpp>
#include <opencv2/opencv_modules.hpp>
#include <opencv2/videostab/frame_source.hpp>


#define arg(name) cmd.get<string>(name)
#define argb(name) cmd.get<bool>(name)
#define argi(name) cmd.get<int>(name)
#define argf(name) cmd.get<float>(name)
#define argd(name) cmd.get<double>(name)
#define EXPORTED

using namespace std;
using namespace cv;
using namespace cv::videostab;

// TO map

void saveMotionsIfNecessary();

MotionModel motionModel(const string &str)
{
    if (str == "transl")
        return MM_TRANSLATION;
    if (str == "transl_and_scale")
        return MM_TRANSLATION_AND_SCALE;
    if (str == "rigid")
        return MM_RIGID;
    if (str == "similarity")
        return MM_SIMILARITY;
    if (str == "affine")
        return MM_AFFINE;
    if (str == "homography")
        return MM_HOMOGRAPHY;
    throw runtime_error("unknown motion model: " + str);
}


class AddStabilizeFrameRequest {
public:
    int index;
    unsigned char* input;
    int width;
    int height;
};

class CustomTwoPassStabilizer : public TwoPassStabilizer {
    bool ok = true, ok2 = true;
    Mat prevFrame, frame;

    public:



    void prepass(int width, int height) {
            WobbleSuppressorBase *wobble = wobbleSuppressor_.get();
            doWobbleSuppression_ = dynamic_cast<NullWobbleSuppressor*>(wobble) == 0;
            frameSize_ = cv::Size(width, height);
            frameMask_.create(frameSize_, CV_8U);
            frameMask_.setTo(255);
    }


    void addFrame(Mat frame) {
           // std::cout << "Stabilize frame " << frameCount_ << std::endl;
            if (frameCount_ > 0)
            {
                //imwrite("/tmp/frame_bad_" + std::to_string(frameCount_) + ".png", frame);
                //imwrite("/tmp/motion_" + std::to_string(frameCount_) + ".jpg", frame);
                if (maskSource_)
                    motionEstimator_->setFrameMask(maskSource_->nextFrame());

                motions_.push_back(motionEstimator_->estimate(prevFrame, frame, &ok));

                if (doWobbleSuppression_)
                {
                    Mat M = wobbleSuppressor_->motionEstimator()->estimate(prevFrame, frame, &ok2);
                    if (ok2)
                        motions2_.push_back(M);
                    else
                        motions2_.push_back(motions_.back());
                }

                if (ok)
                {
                    if (ok2) log_->print("#");
                    else log_->print("?");
                }
                else log_->print("x");

            }
            else
            {
                frameSize_ = frame.size();
                frameMask_.create(frameSize_, CV_8U);
                frameMask_.setTo(255);
            }

            prevFrame = frame;
            frameCount_++;
        }

        void postPass() {
            for (int i = 0; i < radius_; ++i)
                motions_.push_back(Mat::eye(3, 3, CV_32F));

            stabilizationMotions_.resize(frameCount_);
            motionStabilizer_->stabilize(
                frameCount_, motions_, std::make_pair(0, frameCount_ - 1), &stabilizationMotions_[0]);

            if (mustEstTrimRatio_)
            {
                trimRatio_ = 0;
                for (int i = 0; i < frameCount_; ++i)
                {
                    Mat S = stabilizationMotions_[i];
                    trimRatio_ = std::max(trimRatio_, estimateOptimalTrimRatio(S, frameSize_));
                }
            }

            isPrePassDone_ = true;
        }

        Mat getStabilizedFrame(Mat inputFrame, int frameIndex) {
            Mat result = stabilizeFrameCustom(inputFrame, frameIndex);

            //std::cout << "Iteration done " << frameIndex << std::endl;
            //std::cout << result.size() << std::endl;

            Mat result2 = postProcessFrame(result);
            std::cout << result2.size() << std::endl;



            return result2;
        }

        void resetBeforePass() {
            WobbleSuppressorBase *wobble = wobbleSuppressor_.get();
            doWobbleSuppression_ = dynamic_cast<NullWobbleSuppressor*>(wobble) == 0;
            if (doWobbleSuppression_)
            {
                wobbleSuppressor_->setFrameCount(frameCount_);
                wobbleSuppressor_->setMotions(motions_);
                wobbleSuppressor_->setMotions2(motions2_);
                wobbleSuppressor_->setStabilizationMotions(stabilizationMotions_);
            }
        }

        Mat estimateStabilizationMotionCustom(int index)
        {
            return stabilizationMotions_[index].clone();
        }

        Mat stabilizeFrameCustom(Mat frame, int index)
        {
            std::cout << "Custom stabilizing " << index << std::endl;
            Mat result = Mat();
            Mat stabilizationMotion = estimateStabilizationMotionCustom(index);
            if (doCorrectionForInclusion_)
                stabilizationMotion = ensureInclusionConstraint(stabilizationMotion, frameSize_, trimRatio_);

           // at(curStabilizedPos_, stabilizationMotions_) = stabilizationMotion;

            Mat preprocessedFrame;
            if (doDeblurring_)
            {
                frame.copyTo(preprocessedFrame); // TODO: local variable
                deblurer_->deblur(index, preprocessedFrame);
            }
            else
                preprocessedFrame = frame;

            // apply stabilization transformation


            if (motionEstimator_->motionModel() != MM_HOMOGRAPHY) {
                std::cout << frameSize_ << " " << preprocessedFrame.size() << " " << stabilizationMotion(Rect(0,0,3,2)) << std::endl;
                warpAffine(
                        preprocessedFrame, result,
                        stabilizationMotion(Rect(0,0,3,2)), frameSize_, INTER_LINEAR, borderMode_);
            } else
                warpPerspective(
                        preprocessedFrame, result,
                        stabilizationMotion, frameSize_, INTER_LINEAR, borderMode_);

            if (doInpainting_)
            {
                if (motionEstimator_->motionModel() != MM_HOMOGRAPHY)
                    warpAffine(
                            frameMask_, result,
                            stabilizationMotion(Rect(0,0,3,2)), frameSize_, INTER_NEAREST);
                else
                    warpPerspective(
                            frameMask_, result,
                            stabilizationMotion, frameSize_, INTER_NEAREST);

                erode(at(index, stabilizedMasks_), at(index, stabilizedMasks_),
                      Mat());

                at(index, stabilizedMasks_).copyTo(inpaintingMask_);

                inpainter_->inpaint(
                    index, result, inpaintingMask_);
            }
            return result;
        }

    };

// TODO map
class StabilizationContext {
public:
  CustomTwoPassStabilizer *twoPassStabilizer;
  int expectedWidth, expectedHeight;
};

int indexCounter = 0;

std::map<int, StabilizationContext*> contexts;

class IMotionEstimatorBuilder
{
public:
    virtual ~IMotionEstimatorBuilder() {}
    virtual Ptr<ImageMotionEstimatorBase> build() = 0;
protected:
    IMotionEstimatorBuilder(CommandLineParser &command) : cmd(command) {}
    CommandLineParser cmd;
};


class MotionEstimatorRansacL2Builder : public IMotionEstimatorBuilder
{
public:
    MotionEstimatorRansacL2Builder(CommandLineParser &command, bool use_gpu, const string &_prefix = "")
        : IMotionEstimatorBuilder(command), gpu(use_gpu), prefix(_prefix) {}

    virtual Ptr<ImageMotionEstimatorBase> build() CV_OVERRIDE
    {
        Ptr<MotionEstimatorRansacL2> est = makePtr<MotionEstimatorRansacL2>(motionModel(arg(prefix + "model")));

        RansacParams ransac = est->ransacParams();
        if (arg(prefix + "subset") != "auto")
            ransac.size = argi(prefix + "subset");
        if (arg(prefix + "thresh") != "auto")
            ransac.thresh = argf(prefix + "thresh");
        ransac.eps = argf(prefix + "outlier-ratio");
        est->setRansacParams(ransac);

        est->setMinInlierRatio(argf(prefix + "min-inlier-ratio"));

        Ptr<IOutlierRejector> outlierRejector = makePtr<NullOutlierRejector>();
        if (arg(prefix + "local-outlier-rejection") == "yes")
        {
            Ptr<TranslationBasedLocalOutlierRejector> tblor = makePtr<TranslationBasedLocalOutlierRejector>();
            RansacParams ransacParams = tblor->ransacParams();
            if (arg(prefix + "thresh") != "auto")
                ransacParams.thresh = argf(prefix + "thresh");
            tblor->setRansacParams(ransacParams);
            outlierRejector = tblor;
        }

#if defined(HAVE_OPENCV_CUDAIMGPROC) && defined(HAVE_OPENCV_CUDAOPTFLOW)
        if (gpu)
        {
            Ptr<KeypointBasedMotionEstimatorGpu> kbest = makePtr<KeypointBasedMotionEstimatorGpu>(est);
            kbest->setOutlierRejector(outlierRejector);
            return kbest;
        }
#else
        CV_Assert(gpu == false && "CUDA modules are not available");
#endif

        Ptr<KeypointBasedMotionEstimator> kbest = makePtr<KeypointBasedMotionEstimator>(est);
        kbest->setDetector(GFTTDetector::create(argi(prefix + "nkps")));
        kbest->setOutlierRejector(outlierRejector);
        return kbest;
    }
private:
    bool gpu;
    string prefix;
};


class MotionEstimatorL1Builder : public IMotionEstimatorBuilder
{
public:
    MotionEstimatorL1Builder(CommandLineParser &command, bool use_gpu, const string &_prefix = "")
        : IMotionEstimatorBuilder(command), gpu(use_gpu), prefix(_prefix) {}

    virtual Ptr<ImageMotionEstimatorBase> build() CV_OVERRIDE
    {
        Ptr<MotionEstimatorL1> est = makePtr<MotionEstimatorL1>(motionModel(arg(prefix + "model")));

        Ptr<IOutlierRejector> outlierRejector = makePtr<NullOutlierRejector>();
        if (arg(prefix + "local-outlier-rejection") == "yes")
        {
            Ptr<TranslationBasedLocalOutlierRejector> tblor = makePtr<TranslationBasedLocalOutlierRejector>();
            RansacParams ransacParams = tblor->ransacParams();
            if (arg(prefix + "thresh") != "auto")
                ransacParams.thresh = argf(prefix + "thresh");
            tblor->setRansacParams(ransacParams);
            outlierRejector = tblor;
        }

#if defined(HAVE_OPENCV_CUDAIMGPROC) && defined(HAVE_OPENCV_CUDAOPTFLOW)
        if (gpu)
        {
            Ptr<KeypointBasedMotionEstimatorGpu> kbest = makePtr<KeypointBasedMotionEstimatorGpu>(est);
            kbest->setOutlierRejector(outlierRejector);
            return kbest;
        }
#else
        CV_Assert(gpu == false && "CUDA modules are not available");
#endif

        Ptr<KeypointBasedMotionEstimator> kbest = makePtr<KeypointBasedMotionEstimator>(est);
        kbest->setDetector(GFTTDetector::create(argi(prefix + "nkps")));
        kbest->setOutlierRejector(outlierRejector);
        return kbest;
    }
private:
    bool gpu;
    string prefix;
};


class StabilizationInitRequest {
public:
    int radius;

    int width, height;

    const char* motionFile;
    const char* motion2File;
};

class StabilizeFrameRequest {
public:
    int index;
    unsigned char* input;
    unsigned char* output;
    int width;
    int height;
    int frameIndex;
};

bool isFileExist(const char *fileName)
{
    std::ifstream infile(fileName);
    return infile.good();
}

extern "C" {
  EXPORTED int initializeStabilizer(StabilizationInitRequest* request)
  {
      std::cout << "prepare to initialize video stabilizer " << request->width << " " << request->height << " " << request->motionFile << " " << request->motion2File << " " << request->radius << std::endl;
      
      StabilizationContext* newContext = new StabilizationContext();

      newContext->expectedWidth = request->width;
      newContext->expectedHeight = request->height;
      try
      {
          const char *keys =
                  "{ @1                       |           | }"
                  "{ m  model                 | affine    | }"
                  "{ lp lin-prog-motion-est   | no        | }"
                  "{  subset                  | auto      | }"
                  "{  thresh                  | auto | }"
                  "{  outlier-ratio           | 0.5 | }"
                  "{  min-inlier-ratio        | 0.1 | }"
                  "{  nkps                    | 1000 | }"
                  "{  extra-kps               | 0 | }"
                  "{  local-outlier-rejection | no | }"
                  "{  feature-masks           | no | }"
                  "{ sm  save-motions         | no | }"
                  "{ lm  load-motions         | no | }"
                  "{ r  radius                | 15 | }"
                  "{  stdev                   | auto | }"
                  "{ lps  lin-prog-stab       | no | }"
                  "{  lps-trim-ratio          | auto | }"
                  "{  lps-w1                  | 1 | }"
                  "{  lps-w2                  | 10 | }"
                  "{  lps-w3                  | 100 | }"
                  "{  lps-w4                  | 100 | }"
                  "{  deblur                  | no | }"
                  "{  deblur-sens             | 0.1 | }"
                  "{ et  est-trim             | yes | }"
                  "{ t  trim-ratio            | 0.1 | }"
                  "{ ic  incl-constr          | no | }"
                  "{ bm  border-mode          | replicate | }"
                  "{  mosaic                  | no | }"
                  "{ ms  mosaic-stdev         | 10.0 | }"
                  "{ mi  motion-inpaint       | no | }"
                  "{  mi-dist-thresh          | 5.0 | }"
                  "{ ci color-inpaint         | no | }"
                  "{  ci-radius               | 2 | }"
                  "{ ws  wobble-suppress      | no | }"
                  "{  ws-period               | 30 | }"
                  "{  ws-model                | homography | }"
                  "{  ws-subset               | auto | }"
                  "{  ws-thresh               | auto | }"
                  "{  ws-outlier-ratio        | 0.5 | }"
                  "{  ws-min-inlier-ratio     | 0.1 | }"
                  "{  ws-nkps                 | 1000 | }"
                  "{  ws-extra-kps            | 0 | }"
                  "{  ws-local-outlier-rejection | no | }"
                  "{  ws-lp                   | no | }"
                  "{ sm2 save-motions2        | no | }"
                  "{ lm2 load-motions2        | no | }"
                  "{ gpu                      | no | }"
                  "{ o  output                | stabilized.avi | }"
                  "{ fps                      | auto | }"
                  "{ q quiet                  |  | }"
                  "{ h help                   |  | }";

          const char** argv = (const char**)new char*[1];
          argv[0] = "temp";

          CommandLineParser cmd(0, argv, keys);

          // parse command arguments

          if (arg("gpu") == "yes")
          {
              cout << "initializing GPU..."; cout.flush();
              Mat hostTmp = Mat::zeros(1, 1, CV_32F);
              cuda::GpuMat deviceTmp;
              deviceTmp.upload(hostTmp);
              cout << endl;
          }

          StabilizerBase *stabilizer = 0;


          Ptr<IMotionEstimatorBuilder> motionEstBuilder;
          if (false)
              motionEstBuilder.reset(new MotionEstimatorL1Builder(cmd, arg("gpu") == "yes"));
          else
              motionEstBuilder.reset(new MotionEstimatorRansacL2Builder(cmd, arg("gpu") == "yes"));

          Ptr<IMotionEstimatorBuilder> wsMotionEstBuilder;
          if (arg("ws-lp") == "yes")
              wsMotionEstBuilder.reset(new MotionEstimatorL1Builder(cmd, arg("gpu") == "yes", "ws-"));
          else
              wsMotionEstBuilder.reset(new MotionEstimatorRansacL2Builder(cmd, arg("gpu") == "yes", "ws-"));

          CustomTwoPassStabilizer* twoPassStabilizer = new CustomTwoPassStabilizer();
          stabilizer = twoPassStabilizer;
          twoPassStabilizer->setEstimateTrimRatio(arg("est-trim") == "yes");

          // determine stabilization technique

          if (arg("lin-prog-stab") == "yes")
          {
              Ptr<LpMotionStabilizer> stab = makePtr<LpMotionStabilizer>();
              stab->setFrameSize(Size(request->width, request->height));
              stab->setTrimRatio(arg("lps-trim-ratio") == "auto" ? argf("trim-ratio") : argf("lps-trim-ratio"));
              stab->setWeight1(argf("lps-w1"));
              stab->setWeight2(argf("lps-w2"));
              stab->setWeight3(argf("lps-w3"));
              stab->setWeight4(argf("lps-w4"));
              twoPassStabilizer->setMotionStabilizer(stab);
          }
          else if (arg("stdev") == "auto")
              twoPassStabilizer->setMotionStabilizer(makePtr<GaussianMotionFilter>(request->radius));
          else
              twoPassStabilizer->setMotionStabilizer(makePtr<GaussianMotionFilter>(request->radius, argf("stdev")));

          // init wobble suppressor if necessary

          if (arg("wobble-suppress") == "yes")
          {
              Ptr<MoreAccurateMotionWobbleSuppressorBase> ws = makePtr<MoreAccurateMotionWobbleSuppressor>();
              if (arg("gpu") == "yes")
  #ifdef HAVE_OPENCV_CUDAWARPING
                  ws = makePtr<MoreAccurateMotionWobbleSuppressorGpu>();
  #else
                  throw runtime_error("OpenCV is built without CUDA support");
  #endif

              ws->setMotionEstimator(wsMotionEstBuilder->build());
              ws->setPeriod(argi("ws-period"));
              twoPassStabilizer->setWobbleSuppressor(ws);

              MotionModel model = ws->motionEstimator()->motionModel();
              if (isFileExist(request->motionFile))
              {
                  ws->setMotionEstimator(makePtr<FromFileMotionReader>(request->motion2File));
                  ws->motionEstimator()->setMotionModel(model);
              } else {
                  ws->setMotionEstimator(makePtr<ToFileMotionWriter>(request->motion2File, ws->motionEstimator()));
                  ws->motionEstimator()->setMotionModel(model);
              }
          }

          //stabilizer->setFrameSource(source);
          stabilizer->setMotionEstimator(motionEstBuilder->build());

          if (arg("feature-masks") != "no")
          {
              Ptr<VideoFileSource> maskSource = makePtr<VideoFileSource>(arg("feature-masks"));
              std::function<void(Mat&)> maskCallback = [](Mat & inputFrame)
              {
                  cv::cvtColor(inputFrame, inputFrame, cv::COLOR_BGR2GRAY);
                  threshold(inputFrame, inputFrame, 127, 255, THRESH_BINARY);
              };
             // maskSource->setFrameCallback(maskCallback);
              stabilizer->setFrameSource(maskSource);
          }

          // cast stabilizer to simple frame source interface to read stabilized frames
          //stabilizedFrames.reset(dynamic_cast<IFrameSource*>(stabilizer));

          MotionModel model = stabilizer->motionEstimator()->motionModel();
          if (isFileExist(request->motionFile))
          {
              stabilizer->setMotionEstimator(makePtr<FromFileMotionReader>(request->motionFile));
              stabilizer->motionEstimator()->setMotionModel(model);
          } else  {
              stabilizer->setMotionEstimator(makePtr<ToFileMotionWriter>(request->motionFile, stabilizer->motionEstimator()));
              stabilizer->motionEstimator()->setMotionModel(model);
          }

          stabilizer->setRadius(request->radius);

          // init deblurer
          if (arg("deblur") == "yes")
          {
              Ptr<WeightingDeblurer> deblurer = makePtr<WeightingDeblurer>();
              deblurer->setRadius(request->radius);
              deblurer->setSensitivity(argf("deblur-sens"));
              stabilizer->setDeblurer(deblurer);
          }

          // set up trimming parameters
          stabilizer->setTrimRatio(argf("trim-ratio"));
          stabilizer->setCorrectionForInclusion(arg("incl-constr") == "yes");

          if (arg("border-mode") == "reflect")
              stabilizer->setBorderMode(BORDER_REFLECT);
          else if (arg("border-mode") == "replicate")
              stabilizer->setBorderMode(BORDER_REPLICATE);
          else if (arg("border-mode") == "const")
              stabilizer->setBorderMode(BORDER_CONSTANT);
          else
              throw runtime_error("unknown border extrapolation mode: "
                                   + cmd.get<string>("border-mode"));

          // init inpainter
          InpaintingPipeline *inpainters = new InpaintingPipeline();
          Ptr<InpainterBase> inpainters_(inpainters);
          if (arg("mosaic") == "yes")
          {
              Ptr<ConsistentMosaicInpainter> inp = makePtr<ConsistentMosaicInpainter>();
              inp->setStdevThresh(argf("mosaic-stdev"));
              inpainters->pushBack(inp);
          }
          if (arg("motion-inpaint") == "yes")
          {
              Ptr<MotionInpainter> inp = makePtr<MotionInpainter>();
              inp->setDistThreshold(argf("mi-dist-thresh"));
              inpainters->pushBack(inp);
          }
          if (arg("color-inpaint") == "average")
              inpainters->pushBack(makePtr<ColorAverageInpainter>());
          else if (arg("color-inpaint") == "ns")
              inpainters->pushBack(makePtr<ColorInpainter>(int(INPAINT_NS), argd("ci-radius")));
          else if (arg("color-inpaint") == "telea")
              inpainters->pushBack(makePtr<ColorInpainter>(int(INPAINT_TELEA), argd("ci-radius")));
          else if (arg("color-inpaint") != "no")
              throw runtime_error("unknown color inpainting method: " + arg("color-inpaint"));
          if (!inpainters->empty())
          {
              inpainters->setRadius(request->radius);
              stabilizer->setInpainter(inpainters_);
          }


          twoPassStabilizer->prepass(request->width, request->height);
          newContext->twoPassStabilizer = twoPassStabilizer;
          int index = indexCounter++;
          contexts.insert(std::make_pair(index, newContext));

          return index;
      }
      catch (const exception &e)
      {
          cout << "error: " << e.what() << endl;
          return -1;
      }
      return 0;
  }

  EXPORTED void finishedAddingFrames(int index) {
     try {
        StabilizationContext* context = contexts.at(index);
        context->twoPassStabilizer->postPass();
        context->twoPassStabilizer->resetBeforePass();
      } catch (const std::out_of_range& oor) {
          std::cout << index << " is not found in stabilizationContext" << std::endl;
      }
  }


  EXPORTED void createStabilizedFrame(StabilizeFrameRequest* request) {
     try {
          StabilizationContext* context = contexts.at(request->index);
          std::cout << "stabilize frame index " << request->frameIndex << std::endl;
          Mat inputFrame(request->height, request->width, CV_8UC4, (void*)request->input);
          Mat outputMat(request->height, request->width, CV_8UC4, (void*)request->output);

          if (request->width != context->expectedWidth || request->height != context->expectedHeight) {
            Mat scaledFrame;
            cv::resize(inputFrame, scaledFrame, cv::Size(context->expectedWidth, context->expectedHeight));
            inputFrame = scaledFrame;
          }

          Mat stabilizedFrame = context->twoPassStabilizer->getStabilizedFrame(inputFrame, request->frameIndex);

          cv::resize(stabilizedFrame, outputMat, cv::Size(request->width, request->height));
    } catch (const std::out_of_range& oor) {
          std::cout << request->index << " is not found in stabilizationContext" << std::endl;
    }
  }

  EXPORTED void addFrame(AddStabilizeFrameRequest* frameRequest) {
     try {
          StabilizationContext* context = contexts.at(frameRequest->index);
          Mat frame(frameRequest->height, frameRequest->width, CV_8UC4, (void*)frameRequest->input);
          Mat rgb;
          cv::cvtColor(frame, rgb, cv::COLOR_RGBA2BGR, 3);
          context->twoPassStabilizer->addFrame(rgb);
    } catch (const std::out_of_range& oor) {
          std::cout << frameRequest->index << " is not found in stabilizationContext" << std::endl;
    }
  }

  EXPORTED void deallocate(int index) {
    try {
      contexts.erase(index);
    } catch (const std::out_of_range& oor) {
          std::cout << std::to_string(index) << " is not found in stabilizationContext" << std::endl;
    }
  }

}

/*
int main(int argc, char** args) {
    int width=1280;
    int height=720;
    StabilizationInitRequest* initRequest = new StabilizationInitRequest();
    initRequest->motionFile="/tmp/wrong4";
    initRequest->motion2File="/tmp/wrong4_2";
    initRequest->radius = 300;
    initRequest->width = width;
    initRequest->height = height;
    initializeStabilizer(initRequest);


    VideoCapture cap("/home/black/shaky.mp4");

    while (1) {
        Mat frame;
        // Capture frame-by-frame
        cap >> frame;

        // If the frame is empty, break immediately
        if (frame.empty())
          break;

        Mat rgba;
        cv::cvtColor(frame, rgba, cv::COLOR_BGR2BGRA, 4);

        AddStabilizeFrameRequest* afr = new AddStabilizeFrameRequest();
        afr->input = rgba.data;
        afr->width = rgba.cols;
        afr->height = rgba.rows;

        addFrame(afr);

        delete afr;
    }
    finishedAddingFrames();

    VideoWriter writer;
    Mat stabilizedFrame;
    int nframes = 0;

    // for each stabilized frame
    std::cout << "Before while" << std::endl;


    VideoCapture cap2("/home/black/shaky.mp4");
    while(1){
        Mat inputFrame;
        // Capture frame-by-frame
        cap2 >> inputFrame;

        // If the frame is empty, break immediately
        if (inputFrame.empty())
          break;

        std::cout << "Frame " << nframes << std::endl;

        Mat rgba;
        cv::cvtColor(inputFrame, rgba, cv::COLOR_BGR2BGRA, 4);
        StabilizeFrameRequest* sfr = new StabilizeFrameRequest();
        sfr->input = rgba.data;
        sfr->output = new unsigned char[width*height*4];
        sfr->width = width;
        sfr->height = height;
        sfr->frameIndex = nframes;

        createStabilizedFrame(sfr);

        Mat outputMat(sfr->height, sfr->width, CV_8UC4, (void*)sfr->output);
        std::cout << "Have stabilized frame " << outputMat.size() << std::endl;

        // init writer (once) and save stabilized frame
            if (!writer.isOpened())
                writer.open("/home/black/shaky_out13.mp4", VideoWriter::fourcc('X','V','I','D'),
                            30, cv::Size(width, height));
            Mat rgb;
            cv::cvtColor(outputMat, rgb, cv::COLOR_BGRA2BGR, 3);
            std::cout << "Writing frame " << rgb.type() << " " << inputFrame.type() << std::endl;
            writer << rgb;
        nframes++;
        delete[] sfr->output;
        delete sfr;
      }
}*/

