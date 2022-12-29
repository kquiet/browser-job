FROM kquiet/browser-scheduler:${browserscheduler.version}
USER root:root
RUN echo '@edgetesting http://dl-cdn.alpinelinux.org/alpine/edge/testing' >> /etc/apk/repositories \
 && apk update \
 && apk add tzdata pptpclient@edgetesting \
 && cp /usr/share/zoneinfo/Asia/Taipei /etc/localtime \
 && echo "Asia/Taipei" > /etc/timezone \
 && rm -f lib/*

USER kquiet:kquiet

COPY --chown=kquiet:kquiet ["application.yml", "logback.xml", "${project.build.finalName}.jar", "lib", "./ext/"]

COPY --chown=kquiet:kquiet ["browserscheduler.sh", "./"]

RUN chmod +x ./browserscheduler.sh