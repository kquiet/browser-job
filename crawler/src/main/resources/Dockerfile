FROM kquiet/browser-scheduler:${browserscheduler.version}
USER root:root
RUN echo '@edgetesting http://dl-cdn.alpinelinux.org/alpine/edge/testing' >> /etc/apk/repositories \
 && apk --no-cache add pptpclient@edgetesting \
 && rm -f lib/*

USER kquiet:kquiet

COPY --chown=kquiet:kquiet ["application.yml", "logback-spring.xml", "${project.build.finalName}.jar", "lib", "./ext/"]

COPY --chown=kquiet:kquiet ["browserscheduler.sh", "./"]

RUN chmod +x ./browserscheduler.sh