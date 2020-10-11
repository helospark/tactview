#include <cstdlib>
#include <cstring>
#include <iostream>
#include <pwd.h>
#include <sys/types.h>
#include <unistd.h>
#include <experimental/filesystem>
#include <numeric>

inline bool ends_with(std::string const & value, std::string const & ending) {
    if (ending.size() > value.size())
      return false;
    return std::equal(ending.rbegin(), ending.rend(), value.rbegin());
}

void findPathAndNativesFor(std::string path, std::vector<std::string>& jars, std::vector<std::string>& natives) {
    for (const auto & entry : std::experimental::filesystem::directory_iterator(path)) {
      if (ends_with(entry.path(), ".jar")) {
        jars.push_back(entry.path());
      } else if (ends_with(entry.path(), "/native")) {
        natives.push_back(entry.path());
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
    std::cout << "Plugin directory: " << homedir << std::endl;
    for (const auto & entry : std::experimental::filesystem::directory_iterator(homedir)) {
        findPathAndNativesFor(entry.path(), jars, natives);        
        std::cout << "Adding plugin " << entry.path() << std::endl;
    }
  } catch(const std::exception &e) {
    std::cout << "[ERROR] Cannot load plugins from home " << e.what() << std::endl;
  }

  try {
    std::string dropinPluginDirectory = std::string(get_current_dir_name()) + "/dropin/plugins"; // We have already CDd into the directory
    std::cout << "Dropin plugin directory: " << dropinPluginDirectory << std::endl;
    for (const auto & entry : std::experimental::filesystem::directory_iterator(dropinPluginDirectory)) {
        findPathAndNativesFor(entry.path(), jars, natives);
        std::cout << "Adding plugin " << entry.path() << std::endl;
    }
  } catch(const std::exception &e) {
    std::cout << "[ERROR] Cannot load plugins from dropin folder " << e.what() << std::endl;
  }

  jars.push_back("tactview.jar");
  natives.insert(natives.begin(), "libs");
  natives.push_back("$LD_LIBRARY_PATH");

  std::string classpathString = mergeWithDelimiter(jars, ":");
  std::string nativesString = mergeWithDelimiter(natives, ":");

//  std::cout << classpathString << " " << nativesString << std::endl;

  std::string commandLine = "LD_LIBRARY_PATH=" + nativesString + " java-runtime/bin/java -classpath " + classpathString + " -Djdk.gtk.version=2 -Dprism.order=sw -Xmx8g application.HackyMain -Dtactview.plugindirectory=\"" + homedir + "\" \"" + startFileName + "\" >> " + logFile + " 2>&1";
  std::cout << commandLine << std::endl;

  return commandLine;
}

int main(int argc, char** argv) {
  int statusCode = 0;
  do {

    std::string logFile = std::string(argv[0]) + ".log";
    // If launched from another folder, we have to move into the executable's folder, because of relative paths in command line
    char *dirsep = strrchr( argv[0], '/' );
    if( dirsep != NULL ) *dirsep = 0;
    if (strlen(argv[0]) > 0) {
      std::cout << "Working directory is " << argv[0] << std::endl;
      chdir(argv[0]);
    }

    const char* startFileName = "";
    if (argc > 1) {
       startFileName = argv[1];
    }

    std::string commandLine = getCommandLine(logFile.c_str(), startFileName);

    // -Djdk.gtk.version=2 -> https://bugs.java.com/bugdatabase/view_bug.do?bug_id=JDK-8211302
    // -Dprism.order=sw -> Avoid exception due to too wide texture (occures in com.sun.prism.es2.ES2Texture "Requested texture dimensions...")
    statusCode = system(commandLine.c_str());
    statusCode = statusCode >> 8;
    std::cout << "Tactview returned " << statusCode << std::endl;

    if (statusCode == 1) {
      system((std::string("tail -n 100 ") + logFile).c_str()); 
    }

  } while (statusCode == 3);

  std::cout << "Exiting tactview, bye!" << std::endl;
}
