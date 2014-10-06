package monkeywidget.jmxextraction.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Records the service-level info for the desired metrics to be monitored.
 *
 * @author monkeywidget
 * Date: 2/21/14
 * Time: 11:54 AM
 *
 */
public class MonitoredServiceHostInfo {

    /**************************** ALL PROPERTIES ARE CONFIGS */


    /* JMX connection configs */
    private String jmxServiceHost;
    private String jmxServicePort;
    private String jmxServiceJndiPath;
    private String jmxServiceUserid = "";
    private String jmxServicePassword = "";

    /* configs for the mbeans/props to be monitored */
    private List<MonitoredMBeanDesiredInfo> monitoredMBeanList = new ArrayList<>();


    public MonitoredServiceHostInfo(String jmxServiceHost, String jmxServicePort, String jmxServiceJndiPath) {
        this.jmxServiceHost = jmxServiceHost;
        this.jmxServicePort = jmxServicePort;
        this.jmxServiceJndiPath = jmxServiceJndiPath;
    }

    public MonitoredServiceHostInfo(String jmxServiceHost, String jmxServicePort, String jmxServiceJndiPath,
                                    String jmxServiceUserid, String jmxServicePassword) {
        this(jmxServiceHost,jmxServicePort,jmxServiceJndiPath);
        this.jmxServiceUserid = jmxServiceUserid;
        this.jmxServicePassword = jmxServicePassword;
    }


    /**************************** ACCESSOR METHODS */

    public String getServiceHost() { return this.jmxServiceHost; }
    public String getServicePort() { return this.jmxServicePort; }
    public String getServiceJndiPath() { return this.jmxServiceJndiPath; }
    public String getServiceUserid() { return this.jmxServiceUserid; }
    public String getServicePassword() { return this.jmxServicePassword; }

    public String toString() {
        return "jmx_host:" + getServiceHost() +
               ", jmxPort:" + getServicePort() +
                ", jmxJndi:" + getServiceJndiPath() +
                ", jmxUser:" + getServiceUserid() +
                ", jmxPassword:" + getServicePassword();
    }

}
