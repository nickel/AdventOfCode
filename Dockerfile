FROM hseeberger/scala-sbt

ENV CODE_PATH "/root/code"

ADD . $CODE_PATH
RUN mkdir $CODE_PATH/out
WORKDIR $CODE_PATH
VOLUME $CODE_PATH

CMD ["sbt"]
