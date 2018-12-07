package cn.v1.framework.base;

import java.io.Serializable;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by wr on 2017/12/28.
 */
public class SysConfig implements Serializable{

    private static final long serialVersionUID = -5005819177032814835L;
    /**
     * 是/否
     */
    static {
        init();
    }

    public static final String STR_TRUE = "true";

    public static final String STR_FALSE = "false";

    public static final Boolean TRUE = true;

    public static final Boolean FALSE = false;

    public static final String YES = "1";

    public static final String NO = "0";

    private static String SITE_LOCAL_IP;

    private static String SITE_PUBLIC_IP;

    public static String getSiteLocalAddress() {
        return SITE_LOCAL_IP;
    }

    private static void init() {
        loadServerIp();
    }

    private static void loadServerIp() {
        System.out.println("loadServerIp()-start");
        try {
            Enumeration<NetworkInterface> netInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (netInterfaces.hasMoreElements()) {
                NetworkInterface ni = netInterfaces.nextElement();
                Enumeration<InetAddress> ips = ni.getInetAddresses();
                while (ips.hasMoreElements()) {
                    InetAddress ip = ips.nextElement();
					/*System.out.printf(
							"ip:%s, class:%s, localAddress:%b, loopback:%b\n",
							ip.getHostAddress(), ip.getClass().getSimpleName(),
							ip.isSiteLocalAddress(), ip.isLoopbackAddress());*/
                    if (ip instanceof Inet4Address && !ip.isLoopbackAddress()) {
                        boolean isLocal = ip.isSiteLocalAddress();
                        if (isLocal && SITE_LOCAL_IP == null) {
                            SITE_LOCAL_IP = ip.getHostAddress();
                            System.out.println(
                                    "siteLocalAddress:" + SITE_LOCAL_IP);
                        } else if (!isLocal && SITE_PUBLIC_IP == null) {
                            SITE_PUBLIC_IP = ip.getHostAddress();
                            System.out.println(
                                    "sitePublicAddress:" + SITE_PUBLIC_IP);
                        } else {
                            break;
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        System.out.println("loadServerIp()-end");
    }

}
