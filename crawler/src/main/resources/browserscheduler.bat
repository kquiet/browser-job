cd %~dp0
start "" javaw -javaagent:opentelemetry-javaagent.jar -Dspring.profiles.active=jsonlog -Dotel.exporter.otlp.endpoint=${otlp_endpoint:-http://localhost:4317} -Dotel.resource.attributes=service.name=${project.artifactId},service.version=${project.version} -Dchrome_sandbox=no -Dwebdriver_use_default_user_data_dir=yes -cp "lib/;lib/*;ext/;ext/*" org.kquiet.browserscheduler.Launcher
