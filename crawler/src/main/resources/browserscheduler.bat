cd %~dp0
start "" javaw -javaagent:opentelemetry-javaagent.jar -Dotel.exporter.otlp.endpoint=${otlp_endpoint:-http://localhost:4317} -Dotel.service.name=${project.parent.artifactId}-${project.artifactId}-${project.version} -Dchrome_sandbox=no -Dwebdriver_use_default_user_data_dir=yes -cp "lib/;lib/*;ext/;ext/*" org.kquiet.browserscheduler.Launcher
