package org.kquiet.job.crawler.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;

public final class NetUtility {
  private NetUtility(){}

  /**
   * Get local IP list.
   * 
   * @return an IP list
   */
  public static List<String> getIpList() {
    List<String> resultList = new ArrayList<>();
    try {
      for  (Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
          ifaces.hasMoreElements();) {
        NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
        for  (Enumeration<InetAddress> inetAddrs = iface.getInetAddresses();
            inetAddrs.hasMoreElements();) {
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
      for  (Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
          ifaces.hasMoreElements();) {
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

  public static Response httpGet(String url, Map<String, String> header) {
    return httpMethod("GET", url, header, null, 0);
  }

  public static Response httpPost(String url, Map<String, String> header, byte[] body) {
    return httpMethod("POST", url, header, body, 0);
  }

  public static Response httpPut(String url, Map<String, String> header, byte[] body) {
    return httpMethod("PUT", url, header, body, 0);
  }

  public static Response httpDelete(String url, Map<String, String> header) {
    return httpMethod("DELETE", url, header, null, 0);
  }

  public static Response httpOptions(String url, Map<String, String> header) {
    return httpMethod("OPTIONS", url, header, null, 0);
  }

  public static Response httpTrace(String url, Map<String, String> header) {
    return httpMethod("TRACE", url, header, null, 0);
  }

  public static Response httpHead(String url, Map<String, String> header) {
    return httpMethod("HEAD", url, header, null, 0);
  }

  public static Response httpPatch(String url, Map<String, String> header, byte[] body) {
    return httpMethod("PATCH", url, header, body, 0);
  }

  /**
   * Simple HTTP method with header.
   */
  public static Response httpMethod(String method, String url,
      Map<String, String> header, byte[] body, int connectTimeout) {
    try {
      if (connectTimeout == 0) {
        connectTimeout = 30000;
      }
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
          if (body != null) {
            r.bodyByteArray(body);
          }
          break;
        case "PUT":
          r = Request.Put(url);
          if (body != null) {
            r.bodyByteArray(body);
          }
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
          if (body != null) {
            r.bodyByteArray(body);
          }
          break;
        default:
          throw new Exception("Not supported method:" + method);
      }
      r.connectTimeout(connectTimeout);
      if (header != null && !header.isEmpty()) {
        for (Map.Entry<String,String> entry:header.entrySet()) {
          r.addHeader(entry.getKey(), entry.getValue());
        }
      }

      return r.execute();
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }
}
