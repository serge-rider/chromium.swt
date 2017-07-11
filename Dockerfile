# This is produce shared lib and executable linked to older glibc(2.12) and compatible with older linux distros
FROM centos:6.6

RUN yum install -y java-1.8.0-openjdk-devel curl gcc gtk2-devel 
# for running
RUN yum install -y libXScrnSaver GConf2-devel unzip

RUN curl https://sh.rustup.rs -sSf | sh -s -- -y
ENV PATH=$PATH:/root/.cargo/bin
ENV JAVA_HOME=/usr/lib/jvm/java-1.8.0/

COPY cefswt/gradle* /src/cefswt/
COPY cefswt/gradle /src/cefswt/gradle
WORKDIR /src/cefswt

# cause gradlew to download gradle and docker to cache this step
RUN ./gradlew tasks

COPY cefswt/build.gradle /src/cefswt/
COPY cefrust/Cargo* /src/cefrust/
RUN ./gradlew getCefAndUnzip

COPY cefrust /src/cefrust
COPY cefswt /src/cefswt

RUN ./gradlew buildCefRust --stacktrace

RUN ./gradlew buildSampleE4 --stacktrace
 
