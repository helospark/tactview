#include <cstdlib>
#include <cstring>
#include <iostream>
#include <pwd.h>
#include <sys/types.h>
#include <unistd.h>
#include <boost/filesystem.hpp>
#include <numeric>
#include "../common.h"

inline bool ends_with(std::string const & value, std::string const & ending) {
    if (ending.size() > value.size())
      return false;
    return std::equal(ending.rbegin(), ending.rend(), value.rbegin());
}

void findPathAndNativesFor(std::string path, std::vector<std::string>& jars, std::vector<std::string>& natives) {
    for (const auto & entry : boost::filesystem::directory_iterator(path)) {
      auto path = entry.path().string();
      if (ends_with(path, ".jar")) {
        jars.push_back(path);
      } else if (ends_with(path, "/native")) {
        natives.push_back(path);
      }
    }
}

std::string mergeWithDelimiter(std::vector<std::string>& elements, std::string separator) {
  std::string result = "";
  for (int i = 0; i < elements.size(); ++i) {
    result += elements[i];
    if (i < elements.size() - 1) {
      result += separator;
    }
  }
  return result;
}

std::string getCommandLine(const char* logFile, const char* startFileName) {
  std::vector<std::string> jars;
  std::vector<std::string> natives;
  std::string homedir = "/tmp";

  try {
    struct passwd *pw = getpwuid(getuid());
    homedir = std::string(pw->pw_dir) + "/.tactview/plugins/";
    INFO("Plugin directory: " << homedir);
    for (const auto & entry : boost::filesystem::directory_iterator(homedir)) {
        findPathAndNativesFor(entry.path().string(), jars, natives);        
        INFO("Adding plugin " << entry.path());
    }
  } catch(const std::exception &e) {
    WARN("Cannot load plugins from home " << e.what());
  }

  try {
    char currentPathBuf[1000];
    char* currentPath = getcwd(currentPathBuf, 1000);
    std::string dropinPluginDirectory = std::string(currentPath) + "/dropin/plugins"; // We have already CDd into the directory
    INFO("Dropin plugin directory: " << dropinPluginDirectory);
    for (const auto & entry : boost::filesystem::directory_iterator(dropinPluginDirectory)) {
        findPathAndNativesFor(entry.path().string(), jars, natives);
        INFO("Adding plugin " << entry.path());
    }
  } catch(const std::exception &e) {
    WARN("Cannot load plugins from dropin folder " << e.what());
  }

  jars.push_back("tactview.jar");
  natives.insert(natives.begin(), "libs");
  natives.push_back("$DYLD_LIBRARY_PATH");

  std::string classpathString = mergeWithDelimiter(jars, ":");
  std::string nativesString = mergeWithDelimiter(natives, ":");

  std::string commandLine = "DYLD_LIBRARY_PATH=" + nativesString + " java-runtime/bin/java -classpath " + classpathString + " -Djdk.gtk.version=2 -Xmx8g application.HackyMain -Dtactview.plugindirectory=\"" + homedir + "\" \"" + startFileName + "\" >> " + logFile + " 2>&1";
  INFO("Command line=" << commandLine);

  return commandLine;
}

int main(int argc, char** argv) {
  int statusCode = 0;
  do {

    std::string logFile = "/tmp/tactview.log";
    // If launched from another folder, we have to move into the executable's folder, because of relative paths in command line
    char *dirsep = strrchr( argv[0], '/' );
    if( dirsep != NULL ) *dirsep = 0;
    if (strlen(argv[0]) > 0) {
      INFO("Working directory is " << argv[0]);
      chdir(argv[0]);
    }

    const char* startFileName = "";
    if (argc > 1) {
       startFileName = argv[1];
    }

    std::string commandLine = getCommandLine(logFile.c_str(), startFileName);

    // -Djdk.gtk.version=2 -> https://bugs.java.com/bugdatabase/view_bug.do?bug_id=JDK-8211302
    statusCode = system(commandLine.c_str());
    statusCode = statusCode >> 8;
    INFO("Tactview returned " << statusCode);

    if (statusCode == 1) {
      system((std::string("tail -n 100 ") + logFile).c_str()); 
    }

  } while (statusCode == 3);

  INFO("Exiting tactview, bye!");
}
