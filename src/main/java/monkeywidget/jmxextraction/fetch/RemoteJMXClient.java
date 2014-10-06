package monkeywidget.jmxextraction.fetch;

import monkeywidget.jmxextraction.core.MonitoredMBeanDesiredInfo;
import monkeywidget.jmxextraction.core.MonitoredServiceHostInfo;
import monkeywidget.jmxextraction.render.MBeanPropertyRenderer;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * JMXClient used for running in one JVM,
 * but polling MBeans from another
 *
 * @author monkeywidget
 * Date: 3/13/14
 * Time: 1:32 PM
 */
public class RemoteJMXClient extends JMXClient {

    private JMXConnector jmxConnector = null;

    public RemoteJMXClient(MonitoredServiceHostInfo monitoredServiceHostInfo,
                           MonitoredMBeanDesiredInfo monitoredMBeanInfo,
                           MBeanPropertyRenderer renderer) {
        this.monitoredServiceHostInfo = monitoredServiceHostInfo;
        this.monitoredMBeanDesiredInfo = monitoredMBeanInfo;
        this.renderer = renderer;
    }

    /**
     * Just makes the connection to the specified JMX service...
     * does not make any other invocations
     *
     * @throws Exception
     */
    protected void openMBeanServerConnection() throws Exception {
        final String serviceUrlString = "service:jmx:rmi://" + monitoredServiceHostInfo.getServiceHost() +
                "/jndi/rmi://" + monitoredServiceHostInfo.getServiceHost() +
                ":" + monitoredServiceHostInfo.getServicePort() +
                monitoredServiceHostInfo.getServiceJndiPath();

        JMXServiceURL serviceURL = new JMXServiceURL(serviceUrlString);

        Map<String,String[]> credentialsHash  = new HashMap<>();

        // if there are credentials, use them

        if ((monitoredServiceHostInfo.getServiceUserid().length() != 0)
                && (monitoredServiceHostInfo.getServicePassword().length() != 0)) {

            String[] credentialsList = new String[] {   monitoredServiceHostInfo.getServiceUserid() ,
                    monitoredServiceHostInfo.getServicePassword() };
            credentialsHash.put("jmx.remote.credentials", credentialsList);
        }

        //Establish the JMX connection.
        jmxConnector = JMXConnectorFactory.connect(serviceURL, credentialsHash);

        //Get the MBean server connection instance.
        mBeanServerConnection = jmxConnector.getMBeanServerConnection();
    }

    protected void closeMBeanServerConnection() throws Exception {
        jmxConnector.close();
        mBeanServerConnection = null;
    }


}
