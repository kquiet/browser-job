cd %~dp0
start "" javaw -Dspring.profiles.active=jsonlog -Dchrome_sandbox=no -Dwebdriver_use_default_user_data_dir=yes -cp "lib/;lib/*;ext/;ext/*" org.kquiet.browserscheduler.Launcher
