
extern "C" {
#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libswscale/swscale.h>

#include <stdio.h>

void copyFrameData(AVFrame *pFrame, int width, int height, int iFrame, char* frames) {
  for(int y=0; y<height; y++) {
    for (int i = 0; i < width; ++i) {
        int id = y*pFrame->linesize[0] + i * 4;
        frames[y * width * 4 + i * 4 + 0] = *(pFrame->data[0] + id + 2);
        frames[y * width * 4 + i * 4 + 1] = *(pFrame->data[0] + id + 1);
        frames[y * width * 4 + i * 4 + 2] = *(pFrame->data[0] + id + 0);
        frames[y * width * 4 + i * 4 + 3] = *(pFrame->data[0] + id + 3);
    }
  }

}


struct MediaMetadata {
    double fps;
    int width;
    int height;
    long long lengthInMicroseconds;

    MediaMetadata() {
      fps = -1;
      width = -1;
      height = -1;
      lengthInMicroseconds = -1;
    }
};

MediaMetadata readMediaMetadata(const char* path) {
  // Initalizing these to NULL prevents segfaults!
  AVFormatContext   *pFormatCtx = NULL;
  int               i, videoStream;
  AVCodecContext    *pCodecCtxOrig = NULL;
  AVCodecContext    *pCodecCtx = NULL;
  AVCodec           *pCodec = NULL;
  AVFrame           *pFrame = NULL;
  AVFrame           *pFrameRGB = NULL;
  AVPacket          packet;
  int               frameFinished;
  int               numBytes;
  uint8_t           *buffer = NULL;
  struct SwsContext *sws_ctx = NULL;
  MediaMetadata mediaMetadata;

  av_register_all();
  
  if(avformat_open_input(&pFormatCtx, path, NULL, NULL)!=0)
    return mediaMetadata;
  
  if(avformat_find_stream_info(pFormatCtx, NULL)<0)
    return mediaMetadata;
  
  av_dump_format(pFormatCtx, 0, path, 0);
  
  videoStream=-1;
  for(i=0; i<pFormatCtx->nb_streams; i++)
    if(pFormatCtx->streams[i]->codec->codec_type==AVMEDIA_TYPE_VIDEO) {
      videoStream=i;
      break;
    }
  if(videoStream==-1)
    return mediaMetadata;

  pCodecCtxOrig=pFormatCtx->streams[videoStream]->codec;

  pCodec=avcodec_find_decoder(pCodecCtxOrig->codec_id);
  if(pCodec==NULL) {
    fprintf(stderr, "Unsupported codec!\n");
    return mediaMetadata;
  }

  pCodecCtx = avcodec_alloc_context3(pCodec);

  if(avcodec_copy_context(pCodecCtx, pCodecCtxOrig) != 0) {
    fprintf(stderr, "Couldn't copy codec context");
    return mediaMetadata;
  }

  if(avcodec_open2(pCodecCtx, pCodec, NULL)<0)
    return mediaMetadata;

  pFrame=av_frame_alloc();

  pFrameRGB=av_frame_alloc();
  if(pFrameRGB==NULL)
    return mediaMetadata;

  AVStream* st = pFormatCtx->streams[videoStream];

  mediaMetadata.width = pCodecCtx->width;
  mediaMetadata.height = pCodecCtx->height;
  mediaMetadata.lengthInMicroseconds = pFormatCtx->duration / (AV_TIME_BASE / 1000000);

  AVRational framerate;

  if (st->avg_frame_rate.den == 0) {
    framerate = st->r_frame_rate;
  } else {
    framerate = st->avg_frame_rate;
  }

  mediaMetadata.fps = framerate.num / (double)framerate.den;

    fprintf(stderr, "Length 1=%ldd 2=%ldd!\n", mediaMetadata.lengthInMicroseconds, (st->duration / (AV_TIME_BASE / 1000000)));

  av_frame_free(&pFrameRGB);
  
  av_frame_free(&pFrame);
  
  avcodec_close(pCodecCtx);
  avcodec_close(pCodecCtxOrig);

  avformat_close_input(&pFormatCtx);

 // printf("%d %d %f\n", mediaMetadata.width, mediaMetadata.height, mediaMetadata.fps);

  return mediaMetadata;
}

typedef struct {
    char* data;
} FFMpegFrame;

typedef struct {
    int width;
    int height;
    int numberOfFrames;
    long long startMicroseconds;
    char* path;
    FFMpegFrame* frames;
} FFmpegImageRequest;

void readFrames(FFmpegImageRequest* request) {
    fprintf(stderr, "Start!2 %s %d %d %d %llu\n", request->path, request->width, request->height, request->numberOfFrames, request->startMicroseconds);
    fflush(stderr);
  // Initalizing these to NULL prevents segfaults!
  AVFormatContext   *pFormatCtx = NULL;
  int               i, videoStream;
  AVCodecContext    *pCodecCtxOrig = NULL;
  AVCodecContext    *pCodecCtx = NULL;
  AVCodec           *pCodec = NULL;
  AVFrame           *pFrame = NULL;
  AVFrame           *pFrameRGB = NULL;
  AVPacket          packet;
  int               frameFinished;
  int               numBytes;
  uint8_t           *buffer = NULL;
  struct SwsContext *sws_ctx = NULL;

  av_register_all();
  
  if(avformat_open_input(&pFormatCtx, request->path, NULL, NULL)!=0)
    return;
  
  if(avformat_find_stream_info(pFormatCtx, NULL)<0)
    return;
  
 // av_dump_format(pFormatCtx, 0, request->path, 0);
  
  videoStream=-1;
  for(i=0; i<pFormatCtx->nb_streams; i++)
    if(pFormatCtx->streams[i]->codec->codec_type==AVMEDIA_TYPE_VIDEO) {
      videoStream=i;
      break;
    }
  if(videoStream==-1)
    return;
  
  pCodecCtxOrig=pFormatCtx->streams[videoStream]->codec;
  pCodec=avcodec_find_decoder(pCodecCtxOrig->codec_id);
  if(pCodec==NULL) {
    fprintf(stderr, "Unsupported codec!\n");
    return;
  }
  printf("Using codec %s\n", pCodec->name);

  pCodecCtx = avcodec_alloc_context3(pCodec);

  if(avcodec_copy_context(pCodecCtx, pCodecCtxOrig) != 0) {
    fprintf(stderr, "Couldn't copy codec context");
    return;
  }

  if(avcodec_open2(pCodecCtx, pCodec, NULL)<0)
    return;
  
  pFrame=av_frame_alloc();
  
  pFrameRGB=av_frame_alloc();
  if(pFrameRGB==NULL)
    return;

  numBytes=avpicture_get_size(AV_PIX_FMT_RGBA, pCodecCtx->width,
			      pCodecCtx->height);
  buffer=(uint8_t *)av_malloc(numBytes*sizeof(uint8_t));
  
  avpicture_fill((AVPicture *)pFrameRGB, buffer, AV_PIX_FMT_BGRA,
		 pCodecCtx->width, pCodecCtx->height);
  
  sws_ctx = sws_getContext(pCodecCtx->width,
			   pCodecCtx->height,
			   pCodecCtx->pix_fmt,
			   request->width,
			   request->height,
			   AV_PIX_FMT_BGRA,
			   SWS_BILINEAR,
			   NULL,
			   NULL,
			   NULL
			   );

  i=0;

  fprintf(stderr, "Number of frames %d\n", numBytes);


  int64_t seek_target = request->startMicroseconds * (AV_TIME_BASE / 1000000); // rethink
	seek_target= av_rescale_q(seek_target, AV_TIME_BASE_Q, pFormatCtx->streams[videoStream]->time_base);
  av_seek_frame(pFormatCtx, videoStream, seek_target, AVSEEK_FLAG_BACKWARD);

  while(av_read_frame(pFormatCtx, &packet)>=0 && i < request->numberOfFrames) {
    if(packet.stream_index==videoStream) {
      avcodec_decode_video2(pCodecCtx, pFrame, &frameFinished, &packet);
        //fprintf(stderr, "Seeking %llu -> %llu %d\n", packet.pts, seek_target, i);
      if(frameFinished && packet.pts >= seek_target ) {
	        sws_scale(sws_ctx, (uint8_t const * const *)pFrame->data,
		         pFrame->linesize, 0, pCodecCtx->height,
		         pFrameRGB->data, pFrameRGB->linesize);

	        copyFrameData(pFrameRGB, request->width, request->height, i, request->frames[i].data);
          ++i;
	    }
    }
    // Free the packet that was allocated by av_read_frame
    av_free_packet(&packet);
  }
  
  // Free the RGB image
  av_free(buffer);
  av_frame_free(&pFrameRGB);
  
  // Free the YUV frame
  av_frame_free(&pFrame);
  
  // Close the codecs
  avcodec_close(pCodecCtx);
  avcodec_close(pCodecCtxOrig);

  // Close the video file
  avformat_close_input(&pFormatCtx);
}
}
