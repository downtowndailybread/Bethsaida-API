FROM openjdk:12

RUN curl https://bintray.com/sbt/rpm/rpm | tee /etc/yum.repos.d/bintray-sbt-rpm.repo && yum install -y sbt

COPY . /root/bethsaida
RUN mkdir /root/assembly
VOLUME /root/assembly
WORKDIR /root/bethsaida


RUN sbt assembly

CMD cp /root/bethsaida/target/scala-2.12/ClientMonitorApi-assembly-0.1.jar /root/assembly/docker; chmod 777 /root/assembly/docker/ClientMonitorApi-assembly-0.1.jar
