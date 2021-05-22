# Release process

Run script for the environment you want to release

 - **docker-create-release-linux-x64.sh** : Creates release for Linux 64 bit and Debian (.deb) using Docker. This is the preferred method for Linux release format as it uses fixed version libraries that are optimized
    - To prepare "Docker Linux64 release environment" chapter in [development environment](development-environment.md)
    - Run ./docker-create-release-linux-x64.sh
    - Then the built releases will appear in this folder names tactview_linux64_{date}.tar.gz and tactview_{version}.deb
    - These may be manually deployed to file server
    - Push new version number back to Git
 - **create-release-linux-x64.sh** : Creates release for Linux 64 bit and Debian (.deb) using the system's shared libraries. Used for debug releas purpose.
    - To prepare "Linux 64 development environment" chapter in [development environment](development-environment.md)
    - Run ./create-release-linux-x64.sh
    - Then the built releases will appear in this folder names tactview_win64_{date}.zip
    - These may be manually deployed to file server
 - **create-release-osx.sh** : Creates release for OSX 64 bit
    - To prepare "OSX development environment" chapter in [development environment](development-environment.md)
    - Run ./create-release-osx.sh
    - Then the built releases will appear in this folder names tactview_osx_{date}.dmg
    - These may be manually deployed to file server
 - **create-release-win-x64.sh** : Creates release for Windows 64 bit
    - To prepare "Windows development environment" chapter in [development environment](development-environment.md)
    - Run ./create-release-win-x64.sh from your **Cygwin terminal**
    - Then the built releases will appear in this folder names tactview_win64_{date}.zip
    - These may be manually deployed to file server
