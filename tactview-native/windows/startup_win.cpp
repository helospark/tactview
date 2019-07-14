#include <cstdlib>
#include <iostream>
#include <windows.h>
#include <experimental/filesystem>
#include <numeric>

inline bool ends_with(std::string const & value, std::string const & ending) {
    if (ending.size() > value.size())
      return false;
    return std::equal(ending.rbegin(), ending.rend(), value.rbegin());
}

void findPathAndNativesFor(std::string path, std::vector<std::string>& jars, std::vector<std::string>& natives) {
    for (const auto & entry : std::experimental::filesystem::directory_iterator(path)) {
      if (ends_with(entry.path().string(), ".jar")) {
        jars.push_back(entry.path().string());
      } else if (ends_with(entry.path().string(), "/native")) {
        natives.push_back(entry.path().string());
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

std::string getCommandLine() {
  std::vector<std::string> jars;
  std::vector<std::string> natives;
  std::string homedir = "/tmp";

  try {
    homedir = std::string(getenv("USERPROFILE")) + "/.tactview/plugins/";

    std::cout << homedir << std::endl;

    for (const auto & entry : std::experimental::filesystem::directory_iterator(homedir)) {
        findPathAndNativesFor(entry.path().string(), jars, natives);        
        std::cout << entry.path() << std::endl;
    }
  } catch(...) {
    std::cout << "Cannot load plugins" << std::endl;
  }
    
  jars.push_back("tactview.jar");
  natives.insert(natives.begin(), "libs");
  natives.push_back("$LD_LIBRARY_PATH");

  std::string classpathString = mergeWithDelimiter(jars, ":");
  std::string nativesString = mergeWithDelimiter(natives, ":");

//  std::cout << classpathString << " " << nativesString << std::endl;

  std::string commandLine = "LD_LIBRARY_PATH=" + nativesString + " java-runtime/bin/java -classpath " + classpathString + " -Djdk.gtk.version=2 -Xmx8g application.HackyMain -Dtactview.plugindirectory=" + homedir;
  std::cout << commandLine << std::endl;

  return commandLine;
}

int main() {
  int statusCode = 0;
  do {
	std::string commandLine = getCommandLine();
    statusCode = WinExec(commandLine.c_str(), SW_HIDE);
    std::cout << "Tactview returned " << statusCode << std::endl;
  } while (statusCode == 3);
  std::cout << "Exiting tactview, bye!" << std::endl;
}
