#include <cstdlib>
#include <iostream>
#include <windows.h>
#include <experimental/filesystem>
#include <numeric>

#include "../common.h"

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

std::string getCommandLine(const char* startupFile) {
  std::vector<std::string> jars;
  std::vector<std::string> natives;
  std::string homedir = "/tmp";

  try {
    homedir = std::string(getenv("USERPROFILE")) + "/.tactview/plugins/";

    INFO(homedir);

    for (const auto & entry : std::experimental::filesystem::directory_iterator(homedir)) {
        findPathAndNativesFor(entry.path().string(), jars, natives);        
        INFO(entry.path());
    }
  } catch(...) {
    WARN("Cannot load plugins");
  }
    
  jars.push_back("tactview.jar");

  std::string classpathString = mergeWithDelimiter(jars, ";");
  std::string nativesString = mergeWithDelimiter(natives, ";");


  std::string commandLine = "\"java-runtime/bin/java\" -classpath " + classpathString + " -Djdk.gtk.version=2 -Xmx8g application.HackyMain -Dtactview.plugindirectory=\"" + homedir + "\" \"" + startupFile + "\"";
  INFO(commandLine);

  if (nativesString.size() > 0) {
    std::string path = std::string(getenv("PATH")) + nativesString;
    _putenv_s("PATH", path.c_str());
    INFO("Set PATH=" << path);
  }
  return commandLine;
}

int execute(std::string commandLine) {
    STARTUPINFO si;
    PROCESS_INFORMATION pi;

    SECURITY_ATTRIBUTES sa;
    sa.nLength = sizeof(sa);
    sa.lpSecurityDescriptor = NULL;
    sa.bInheritHandle = TRUE;   

	INFO("Create file");
    HANDLE h = CreateFile("tactview.log",
        FILE_APPEND_DATA,
        FILE_SHARE_WRITE | FILE_SHARE_READ,
        &sa,
        OPEN_ALWAYS,
        FILE_ATTRIBUTE_NORMAL,
        NULL );

    ZeroMemory( &si, sizeof(si) );
    si.cb = sizeof(si);
    si.dwFlags |= (STARTF_USESTDHANDLES | STARTF_USESHOWWINDOW);
    si.hStdInput = NULL;
    si.hStdError = h;
    si.hStdOutput = h;
    ZeroMemory( &pi, sizeof(pi) );

	LPSTR lpstr = const_cast<LPSTR>(commandLine.c_str());
    // Start the child process. 
    if( !CreateProcess( NULL,   // No module name (use command line)
        lpstr,        // Command line
        NULL,           // Process handle not inheritable
        NULL,           // Thread handle not inheritable
        TRUE,           // Set handle inheritance to FALSE
        0,              // No creation flags
        NULL,           // Use parent's environment block
        NULL,           // Use parent's starting directory 
        &si,            // Pointer to STARTUPINFO structure
        &pi )           // Pointer to PROCESS_INFORMATION structure
    ) 
    {
        printf( "CreateProcess failed (%d).\n", GetLastError() );
        return -1;
    }

    // Wait until child process exits.
    WaitForSingleObject( pi.hProcess, INFINITE );
	
	DWORD returnCode;
	GetExitCodeProcess(pi.hProcess, &returnCode);

    // Close process and thread handles. 
    CloseHandle( pi.hProcess );
    CloseHandle( pi.hThread );
    CloseHandle( h );
	
	return (int)returnCode;
}

int main(int argc, char** argv) {
  int statusCode = 0;
  do {
    const char* startupFile = "";
    if (argc > 1) {
      startupFile = argv[1];
    }

	  std::string commandLine = getCommandLine(startupFile);

    statusCode = execute(commandLine.c_str());
    INFO("Tactview returned " << statusCode);
  } while (statusCode == 3);
  INFO("Exiting tactview, bye!");
}

