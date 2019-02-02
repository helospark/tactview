#include <stdlib.h>

int main() {
  system("LD_LIBRARY_PATH=libs:$LD_LIBRARY_PATH java-runtime/bin/java --add-exports=javafx.base/com.sun.javafx.runtime=controlsfx --add-exports=javafx.controls/com.sun.javafx.scene.control.behavior=controlsfx --add-exports=javafx.controls/com.sun.javafx.scene.control.inputmap=controlsfx --add-exports=javafx.graphics/com.sun.javafx.scene.traversal=controlsfx -jar tactview.jar");
}
