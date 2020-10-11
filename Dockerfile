# This Dockerfile configures the build environment in a Docker for Tactview for Linux environment, but does not actually build Tactview.
FROM ubuntu:bionic-20200921

VOLUME /tactview
WORKDIR /tmp

RUN apt-get update && apt-get install -y wget

RUN wget -O maven.tar.gz https://downloads.apache.org/maven/maven-3/3.6.3/binaries/apache-maven-3.6.3-bin.tar.gz && \
    tar -xvzf maven.tar.gz && \
    mv apache-maven-3.6.3 /opt/apache-maven && \
    rm maven.tar.gz


RUN wget -O jdk.tar.gz https://download.java.net/java/GA/jdk15/779bf45e88a44cbd9ea6621d33e33db1/36/GPL/openjdk-15_linux-x64_bin.tar.gz && \
    tar -xvzf jdk.tar.gz && \
    mv jdk-15 /opt/jdk-15 && \
    rm jdk.tar.gz

ENV JAVA_HOME=/opt/jdk-15
ENV PATH="/opt/jdk-15/bin:/opt/apache-maven/bin:${PATH}"

# Separate out this step, so the layer can be cached
ADD tactview-native/dependency_fragments/install_apt_dependencies.sh /tmp/dependency_fragments/install_apt_dependencies.sh
RUN chmod +x /tmp/dependency_fragments/install_apt_dependencies.sh
RUN /tmp/dependency_fragments/install_apt_dependencies.sh

RUN echo $(pwd)
ADD tactview-native/build_dependencies.sh /tmp/build_dependencies.sh
RUN chmod +x /tmp/build_dependencies.sh
RUN ./build_dependencies.sh -r

# Set .m2 directory to tmp, so it can be attached locally for better caching
RUN sed -i "s/<\!-- localRepository/<localRepository>\/tmp\/.m2<\/localRepository>\n<\!--/g" /opt/apache-maven/conf/settings.xml

WORKDIR /tactview

ENTRYPOINT ["./create-release-linux-x64.sh"]
