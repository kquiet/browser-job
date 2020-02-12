#!/bin/sh

if [ -n "${pptp_address}" ]; then

cat > /etc/ppp/peers/pptp_provider <<_EOF_
pty "pptp ${pptp_address} --nolaunchpppd"
name `cat "${pptp_username_file}"`
password `cat "${pptp_password_file}"`
remotename PPTP
maxfail 0
persist
file /etc/ppp/options.pptp
ipparam "pptp_provider"
_EOF_

cat > /etc/ppp/ip-up <<_EOF_
#!/bin/sh
ip route add ${pptp_local_cidr} dev `ip route | grep -m 1 "default via" | awk '{printf "%s via %s",$5,$3}'`
ip route add 0.0.0.0/1 dev \$1
ip route add 128.0.0.0/1 dev \$1
_EOF_

cat > /etc/ppp/ip-down <<_EOF_
#!/bin/sh
ip route del ${pptp_local_cidr}
ip route del 0.0.0.0/1 dev \$1
ip route del 128.0.0.0/1 dev \$1
_EOF_

  pkill pppd
  pppd call pptp_provider
  sleep 10s
fi

exec java -Dwebdriver.gecko.driver=geckodriver -Dwebdriver.chrome.driver=chromedriver -Dchrome_sandbox=no -Dwebdriver_headless=yes -cp "lib/:lib/*:ext/:ext/*" org.kquiet.jobscheduler.Launcher
