#include <cstdlib>
#include <iostream>
#include <windows.h>

int main() {
  int statusCode = 0;
  do {
    statusCode = WinExec("\"java-runtime\\bin\\java\" -Xmx8g -jar tactview.jar", SW_HIDE);
    std::cout << "Tactview returned " << statusCode << std::endl;
  } while (statusCode == 3);
  std::cout << "Exiting tactview, bye!" << std::endl;
}

