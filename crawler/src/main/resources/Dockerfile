FROM kquiet/job-scheduler:${jobscheduler.version}

RUN echo '@edgetesting http://dl-cdn.alpinelinux.org/alpine/edge/testing' >> /etc/apk/repositories \
 && apk update \
 && apk add tzdata pptpclient@edgetesting \
 && cp /usr/share/zoneinfo/Asia/Taipei /etc/localtime \
 && echo "Asia/Taipei" > /etc/timezone \
 && rm -f lib/*

COPY ["jobscheduler.config", "mybatis-config.xml", "mybatis-mapper.xml", "logback.xml", "${project.build.finalName}.jar", "lib", "./ext/"]

COPY ["jobscheduler.sh", "./"]

RUN chmod +x ./jobscheduler.sh