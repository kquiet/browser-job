package org.kquiet.browserjob.crawler.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Net helper.
 *
 * @author monkey
 *
 */
public class NetHelper {

  /**
   * Get local IP list.
   *
   * @return an IP list
   */
  public List<String> getIpList() {
    List<String> resultList = new ArrayList<>();
    try {
      for (Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces(); ifaces
          .hasMoreElements();) {
        NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
        for (Enumeration<InetAddress> inetAddrs = iface.getInetAddresses(); inetAddrs
            .hasMoreElements();) {
          InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
          if (inetAddr.isSiteLocalAddress()) {
            resultList.add(inetAddr.getHostAddress());
          }
        }
      }
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
    return resultList;
  }

  /**
   * Get network interfaces which are in up state.
   *
   * @return list of network interfaces
   */
  public List<NetworkInterface> getUpNetworkInterfaceList() {
    List<NetworkInterface> resultList = new ArrayList<>();
    try {
      for (Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces(); ifaces
          .hasMoreElements();) {
        NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
        if (iface.isUp()) {
          resultList.add(iface);
        }
      }
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
    return resultList;
  }
}
