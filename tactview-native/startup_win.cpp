#include <cstdlib>

int main() {
  int statusCode = 0;
  do {
    system("\"java-runtime\\bin\\java\" -Xmx8g -jar tactview.jar");
    std::cout << "Tactview returned " << statusCode << std::endl;
  } while (statusCode == 3);
  std::cout << "Exiting tactview, bye!" << std::endl;
}
