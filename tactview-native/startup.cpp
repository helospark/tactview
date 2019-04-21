#include <cstdlib>

int main() {
  // -Djdk.gtk.version=2 -> https://bugs.java.com/bugdatabase/view_bug.do?bug_id=JDK-8211302
  system("LD_LIBRARY_PATH=libs:$LD_LIBRARY_PATH java-runtime/bin/java -Djdk.gtk.version=2 -Xmx8g -jar tactview.jar");
}
