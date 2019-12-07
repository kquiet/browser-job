FROM kquiet/job-scheduler:1.0.0

RUN echo '@edgetesting http://dl-cdn.alpinelinux.org/alpine/edge/testing' >> /etc/apk/repositories \
 && apk update \
 && apk add tzdata pptpclient@edgetesting \
 && cp /usr/share/zoneinfo/Asia/Taipei /etc/localtime \
 && echo "Asia/Taipei" > /etc/timezone \
 && rm -f lib/*

COPY ["logback.xml", "${project.build.finalName}.jar", "lib", "./ext/"]

COPY ["jobscheduler.config", "mybatis-config.xml", "mybatis-mapper.xml", "./ext/"]