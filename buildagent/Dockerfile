FROM ubuntu:bionic

# Install Dependencies
RUN apt-get update && apt-get install -y software-properties-common debconf-utils

# Accept Oracle TOS
RUN echo "oracle-java8-installer shared/accepted-oracle-license-v1-1 select true" | debconf-set-selections

# Install Oracle JDK 8
RUN add-apt-repository ppa:webupd8team/java && apt-get update && apt-get install -y oracle-java8-installer
