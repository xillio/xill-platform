#
# Copyright (C) 2014 Xillio (support@xillio.com)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#         http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

FROM ubuntu:bionic

# Install Dependencies
RUN apt-get update && apt-get install -y software-properties-common debconf-utils git maven curl build-essential \
    chrpath libssl-dev libxft-dev libfreetype6 libfreetype6-dev libfontconfig1 libfontconfig1-dev

# Accept Oracle TOS
RUN echo "oracle-java8-installer shared/accepted-oracle-license-v1-1 select true" | debconf-set-selections

# Install Oracle JDK 8
RUN add-apt-repository ppa:webupd8team/java && apt-get update && apt-get install -y oracle-java8-installer

# Set the locale
RUN sed -i -e 's/# en_US.UTF-8 UTF-8/en_US.UTF-8 UTF-8/' /etc/locale.gen && locale-gen
ENV LANG en_US.UTF-8
ENV LANGUAGE en_US:en
ENV LC_ALL en_US.UTF-8

# Set timezone
ENV DEBIAN_FRONTEND=noninteractive
RUN apt-get install -y tzdata && ln -fs /usr/share/zoneinfo/Europe/Amsterdam /etc/localtime && \
    dpkg-reconfigure --frontend noninteractive tzdata


