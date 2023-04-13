cd %~dp0
start "" javaw -Dspring.profiles.active=jsonlog -Dchrome_option_args="--no-sandbox,--disable-dev-shm-usage,--remote-allow-origins=*" -cp "lib/;lib/*;ext/;ext/*" org.kquiet.browserscheduler.Launcher
