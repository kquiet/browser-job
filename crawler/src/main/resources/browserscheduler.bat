cd %~dp0
start "" javaw -Dspring.profiles.active=jsonlog -Dfirefox_option_args="--width=1024;--height=768" -Dchrome_option_args="--no-sandbox;--disable-dev-shm-usage;--remote-allow-origins=*;--window-size=1024,768" -cp "lib/;lib/*;ext/;ext/*" org.kquiet.browserscheduler.Launcher
