#include <cstdlib>
#include <iostream>

int main() {
  int statusCode = 0;
  do {
    // -Djdk.gtk.version=2 -> https://bugs.java.com/bugdatabase/view_bug.do?bug_id=JDK-8211302
    statusCode = system("LD_LIBRARY_PATH=libs:$LD_LIBRARY_PATH java-runtime/bin/java -Djdk.gtk.version=2 -Xmx8g -jar tactview.jar");
    statusCode = statusCode >> 8;
    std::cout << "Tactview returned " << statusCode << std::endl;
  } while (statusCode == 3);
  std::cout << "Exiting tactview, bye!" << std::endl;
}
