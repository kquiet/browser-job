FROM kquiet/job-scheduler:${jobscheduler.version}
USER root:root
RUN echo '@edgetesting http://dl-cdn.alpinelinux.org/alpine/edge/testing' >> /etc/apk/repositories \
 && apk update \
 && apk add tzdata pptpclient@edgetesting \
 && cp /usr/share/zoneinfo/Asia/Taipei /etc/localtime \
 && echo "Asia/Taipei" > /etc/timezone \
 && rm -f lib/*

USER kquiet:kquiet

COPY --chown=kquiet:kquiet ["application.yml", "mybatis-config.xml", "mybatis-mapper.xml", "logback.xml", "${project.build.finalName}.jar", "lib", "./ext/"]

COPY --chown=kquiet:kquiet ["jobscheduler.sh", "./"]

RUN chmod +x ./jobscheduler.sh