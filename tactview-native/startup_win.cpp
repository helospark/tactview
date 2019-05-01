#include <cstdlib>
#include <iostream>

int main() {
  int statusCode = 0;
  do {
    statusCode = system("\"java-runtime\\bin\\java\" -Xmx8g -jar tactview.jar");
    std::cout << "Tactview returned " << statusCode << std::endl;
  } while (statusCode == 3);
  std::cout << "Exiting tactview, bye!" << std::endl;
}

