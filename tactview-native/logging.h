#ifndef LOGGING_ASD_H
#define LOGGING_ASD_H

#include <iostream>
#include <ctime>
#include <thread>
#include <string>

std::string getCurrentTime() {
  time_t rawtime;
  struct tm * timeinfo;
  char buffer[80];

  time (&rawtime);
  timeinfo = localtime(&rawtime);

  strftime(buffer,sizeof(buffer),"%H:%M:%S",timeinfo);
  std::string str(buffer);

  return str;
}

// TODO: add runtime check for log level based on startup params (ex. environment variable)
#ifdef DEBUG_LOG
#  define DEBUG(x) std::cout << getCurrentTime() << " [" << std::this_thread::get_id() << "] DEBUG " << __FILE__ << ":" << __LINE__ << " - " << x << std::endl;
#else
#  define DEBUG(x) do {} while (0)
#endif

#define INFO(x) std::cout << getCurrentTime() << " [" << std::this_thread::get_id() << "] INFO " << __FILE__ << ":" << __LINE__ << " - " << x << std::endl
#define WARN(x) std::cout << getCurrentTime() << " [" << std::this_thread::get_id() << "] WARN " << __FILE__ << ":" << __LINE__ << " - " << x << std::endl
#ifndef SHOULD_NOT_DEFINE_ERROR_LOG
#define ERROR(x) std::cout << getCurrentTime() << " [" << std::this_thread::get_id() << "] ERROR " << __FILE__ << ":" << __LINE__ << " - " << x << std::endl
#endif

#endif
