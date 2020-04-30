cd %~dp0
start "" javaw -Dwebdriver.gecko.driver=geckodriver.exe -Dwebdriver.chrome.driver=chromedriver.exe -Dchrome_sandbox=no -Dwebdriver_headless=no -Dwebdriver_use_default_user_data_dir=yes -cp "lib/;lib/*;ext/;ext/*" org.kquiet.jobscheduler.Launcher
