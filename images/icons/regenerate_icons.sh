# Linux desktop icons generated during release process

# Windows desktop ico file 
rm ../../tactview-native/tactview.ico
convert icon_full.png -resize 32x32 ../../tactview-native/tactview.ico

# Runtime icon
rm ../../tactview-ui/src/main/resources/icons/tactview_icon.png
convert icon_full.png -resize 32x32 ../../tactview-ui/src/main/resources/icons/tactview_icon.png
