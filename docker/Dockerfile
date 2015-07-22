FROM learninglayers/base
MAINTAINER Martin Bachl <martin@bachl.pro>

# Install Java.
RUN sudo apt-get purge openjdk*
RUN apt-get update

# Environment variables
ENV LDS_VERSION v0.0.1
# ===> BEGIN: Replace this part with the learninglayers/java image later on <===
ENV JAVA_HOME "/usr/lib/jvm/java-8-oracle" \
ENV PATH $JAVA_HOME/bin:$PATH

# Install Java 8
RUN sudo apt-get purge openjdk*
RUN apt-get update -yq \

	# add Java 8 repository
	&& apt-get install -yq software-properties-common \
	&& add-apt-repository -y ppa:webupd8team/java \
	&& echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | sudo /usr/bin/debconf-set-selections \

	# install Java 8
	&& apt-get update -yq \
	&& apt-get install -yq oracle-java8-installer wget \
	&& update-java-alternatives -s java-8-oracle
	
# ===> END: Replace this part with the ll/java image later on <===

RUN mkdir /opt/ldserver
RUN mkdir /opt/ldserver/search
WORKDIR /opt/ldserver
RUN apt-get install -yq curl
RUN curl -SL \
    https://github.com/learning-layers/LivingDocumentsServer/releases/download/${LDS_VERSION}/ld-boot-1.0.0.jar \
    > ld-boot-1.0.0.jar


RUN curl -SL \
    https://github.com/learning-layers/LivingDocumentsServer/releases/download/${LDS_VERSION}/application.properties \
    > application.properties

EXPOSE 9000
CMD ["java", "-jar", "ld-boot-1.0.0.jar", "--spring.config.location", "application.properties"]