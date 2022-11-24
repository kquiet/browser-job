package org.kquiet.job.crawler.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import org.apache.http.client.fluent.Request;

/**
 * NetUtility.
 *
 * @author monkey
 *
 */
public final class NetUtility {
  private NetUtility() {}

  /**
   * Get local IP list.
   *
   * @return an IP list
   */
  public static List<String> getIpList() {
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
  public static List<NetworkInterface> getUpNetworkInterfaceList() {
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

  /**
   * Add headers to given {@code Request}.
   *
   * @param r request
   * @param header headers to add
   * @return given {@code Request}
   */
  public static Request addHeader(Request r, Map<String, String> header) {
    if (header != null && !header.isEmpty()) {
      for (Map.Entry<String, String> entry : header.entrySet()) {
        r.addHeader(entry.getKey(), entry.getValue());
      }
    }
    return r;
  }

  /**
   * Create a new http request.
   *
   * @param method http method
   * @param url target url
   * @return given {@code Request}
   */
  public static Request httpRequest(String method, String url) {
    if (method == null) {
      method = "GET";
    }
    method = method.toUpperCase();

    Request r = null;
    switch (method) {
      case "GET":
        r = Request.Get(url);
        break;
      case "POST":
        r = Request.Post(url);
        break;
      case "PUT":
        r = Request.Put(url);
        break;
      case "DELETE":
        r = Request.Delete(url);
        break;
      case "OPTIONS":
        r = Request.Options(url);
        break;
      case "TRACE":
        r = Request.Trace(url);
        break;
      case "HEAD":
        r = Request.Head(url);
        break;
      case "PATCH":
        r = Request.Patch(url);
        break;
      default:
        throw new UnsupportedOperationException("Not supported method:" + method);
    }
    return r;
  }
}
