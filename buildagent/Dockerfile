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

FROM openjdk:8u242-jdk
#FROM amazoncorretto:8u242


# # Install Dependencies
RUN apt-get update && apt-get install -y software-properties-common debconf-utils git maven curl build-essential \
    chrpath libssl-dev libxft-dev libfreetype6 libfreetype6-dev libfontconfig1 libfontconfig1-dev \
    openjfx

# # Set timezone
ENV DEBIAN_FRONTEND=noninteractive
RUN apt-get install -y tzdata && ln -fs /usr/share/zoneinfo/Europe/Amsterdam /etc/localtime && \
    dpkg-reconfigure --frontend noninteractive tzdata

# Do update 
#RUN yum update -y

# Install a lot
# RUN yum install -y software-properties-common debconf-utils git maven curl build-essential \
#     chrpath libssl-dev libxft-dev libfreetype6 libfreetype6-dev libfontconfig1 libfontconfig1-dev

# Install Docker

# RUN yum install -y "https://get.docker.com/rpm/1.7.1/centos-6/RPMS/x86_64/docker-engine-1.7.1-1.el6.x86_64.rpm"

# Install Docker
RUN curl -sSL get.docker.com | bash

