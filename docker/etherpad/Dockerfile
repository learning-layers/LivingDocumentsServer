FROM tutum/ubuntu:latest
MAINTAINER Martin Bachl <martin@bachl.pro>

# ENV arguments
# -----------------
# ENV HTTP_USER
# ENV HTTP_PASSWORD
# ENV HTTP_BASE_URL

RUN sudo apt-get update
RUN sudo apt-get install -y wget curl unzip abiword

COPY run.sh /run.sh

ENV ETHERPAD_VERSION 1.5.6

RUN sudo apt-get update
RUN apt-get install -y curl unzip && \
    rm -r /var/lib/apt/lists/*

RUN sudo apt-get update
RUN apt-get install -y nodejs-legacy npm && \
    rm -r /var/lib/apt/lists/*

RUN sudo apt-get update
RUN apt-get install -y mysql-client && \
    rm -r /var/lib/apt/lists/*

WORKDIR /opt/

RUN curl -SL \
    https://github.com/ether/etherpad-lite/archive/${ETHERPAD_VERSION}.zip \
    > etherpad.zip && unzip etherpad && rm etherpad.zip && \
    mv etherpad-lite-${ETHERPAD_VERSION} etherpad-lite

WORKDIR etherpad-lite

EXPOSE 9001
WORKDIR ~/bin
COPY inner-run1.sh /root/bin/inner-run1.sh
COPY inner-run2.sh /root/bin/inner-run2.sh
COPY run.sh run.sh
CMD ["bash", "/run.sh"]