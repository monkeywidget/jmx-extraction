package monkeywidget.jmxextraction.fetch;

import monkeywidget.jmxextraction.core.MonitoredMBeanDesiredInfo;
import monkeywidget.jmxextraction.core.MonitoredServiceHostInfo;
import monkeywidget.jmxextraction.render.MBeanPropertyRenderer;
import monkeywidget.jmxextraction.util.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static monkeywidget.jmxextraction.util.ClassUtils.isPrimitiveOrWrapperOrStringType;

/**
 * Responsible for all things JMX:
 * making a connection,
 * searching for MBeans,
 * and passing the attributes to a renderer
 *
 * @author monkeywidget
 * Date: 3/7/14
 * Time: 4:23 PM
 */
public abstract class JMXClient {

    protected MBeanServerConnection mBeanServerConnection = null;

    protected MonitoredServiceHostInfo monitoredServiceHostInfo = null;
    protected MonitoredMBeanDesiredInfo monitoredMBeanDesiredInfo = null;
    protected MBeanPropertyRenderer renderer = null;

    protected Logger logger = LoggerFactory.getLogger(JMXClient.class);

    protected TreeSet<ObjectName> beansMatchingRequestedPattern  = new TreeSet<>();
    protected TreeSet<ObjectName> beansNotMatchingRequestedPattern  = new TreeSet<>();

    /**
     * This could be a remote (RemoteJMXClient) connection or a local (LocalJMXClient) connection
     * */
    protected abstract void openMBeanServerConnection() throws Exception;
    protected abstract void closeMBeanServerConnection() throws Exception;

    public void fetchAndRenderMBeanProperties() throws Exception {

        openMBeanServerConnection();

        fetchAndRenderMBeanPropertiesFromOpenedConnection();

        closeMBeanServerConnection();
    }

    protected void fetchAndRenderMBeanPropertiesFromOpenedConnection() {

        logger.info("fetching beans");

        sortBeansMatchingKeySubstring(); // separates into beans[Not]MatchingRequestedPattern
                                         // filters beans by keyValueMatch (-k)

        if (beansMatchingRequestedPattern.isEmpty()) {
            logger.warn("No MBeans matched pattern \"" +
                    monitoredMBeanDesiredInfo.getKeyValueMatch() + "\"");
        }
        else {
            logger.info("Fetched " + beansMatchingRequestedPattern.size() + " MBeans");
        }

        logAttributesFromTheseMBeans(beansMatchingRequestedPattern);
                                         // filters metrics by (-c)
    }

    /**
     * Retrieves MBeans matching the filter
     * populates both beansMatchingRequestedPattern and beansNotMatchingRequestedPattern
     * called by fetchAndLogMonitoredAttributesFromMBeansMatchingCriteria
     */
    private void sortBeansMatchingKeySubstring() {

        // choose a subset of the beans?
        boolean ignoreBeanFilters = monitoredMBeanDesiredInfo.queryAllBeansForMetricValuesP();

        String keyValueMatch = null;

        if (!ignoreBeanFilters) {
            // to choose beans, what is the pattern we should match in the key/value string?
            keyValueMatch = monitoredMBeanDesiredInfo.getKeyValueMatch();
        }

        logger.debug("fetchBeansMatchingKeySubstring: keyValueMatch is " + keyValueMatch);
        logger.debug("fetchBeansMatchingKeySubstring: ignoreBeanFilters is " + ignoreBeanFilters);
        // example:
        // com.mchange.v2.c3p0:type=PooledDataSource,identityToken=2w6kbm8z1j13au5x2rm45|16b4bba6,...
        // ^^ the domain    ^ : ^  key-property-list
        // note that the "name" is this entire string
        // so the keyValueMatch might be "PooledDataSource" or "type=P"


        try {

            TreeSet<ObjectName> allMBeans =  new TreeSet<ObjectName>(mBeanServerConnection.queryNames(null, null));

            // we are filtering ourselves, because we don't know the format of what we are looking for -
            // it may be a substring, it may specify a type, or name, etc
            // (it's set in a properties file)

            if (allMBeans.size() == 0) {
                logger.error("there were no MBeans retrieved from port " +
                        monitoredServiceHostInfo.getServicePort());
            }
            else {
                logger.debug("there are " + allMBeans.size() + " mbeans total, some may be filtered out");
            }

            for (ObjectName objectName : allMBeans.descendingSet()) {
                if (ignoreBeanFilters || objectName.toString().contains(keyValueMatch)) {
                    beansMatchingRequestedPattern.add(objectName);
                    // TODO: DEBUG
                    logger.info("\tadded bean " + objectName.toString() + " (matched \"" + keyValueMatch + "\")");
                }
                else {
                    beansNotMatchingRequestedPattern.add(objectName);
                    logger.debug("\t" + objectName.toString() + " did not match \"" + keyValueMatch + "\"");
                }
            }

            if (beansMatchingRequestedPattern.isEmpty()) {
                logger.error("No MBeans had key values that matched the substring \"" +
                        monitoredMBeanDesiredInfo.getKeyValueMatch() + "\"");
            }

        }
        catch (IOException ioException) {
            logger.error("could not queryNames from MBean connection: " + ioException.toString());
        }

    }




    /**
     *
     * @param mbeans the actual mbeans we got back, which may or may not have the attributes we want
     */
    private void logAttributesFromTheseMBeans(Set<ObjectName> mbeans) {
        logger.debug("there are " + mbeans.size() + " mbeans which matched the filter");

        for (ObjectName mBean : mbeans) {
            try {
                // logger.debug("examining MBean >>" + mBean.getCanonicalName() + "<<");
                logger.info("examining MBean >>" + mBean.getCanonicalName() + "<<");
                logAttributesFromMBean(mBean);
            }
            catch (IOException | JMException ex) {
                // we need this particular error to make it to the log
                StringWriter errors = new StringWriter();
                ex.printStackTrace(new PrintWriter(errors));

                logger.error("MBean " + mBean + " could not be displayed: " + errors.toString());
                continue;
            }
        } // end for all MBeans responding to this ObjectName
    }


    /**
     * Again, note that we log the actual live MBean we have
     * rather than the list of desired props from the config
     * with which we looked it up
     *
     * @param liveMBeanStub client stub of the live remote bean
     * @throws IOException
     * @throws JMException
     */
    private void logAttributesFromMBean(ObjectName liveMBeanStub)
            throws IOException, JMException                         {

        // filter the metrics on those beans?  Or not?
        boolean ignoreFilters =
                monitoredMBeanDesiredInfo.queryAllMetricsNotOnlySubsetP();

        // logger.debug("logAttributesFromMBean: ignoreFilters is " + ignoreFilters);

        final MBeanAttributeInfo[] allAttributesInLiveBean =
                mBeanServerConnection.getMBeanInfo(liveMBeanStub).getAttributes();

        final List<String> attributesToLog = monitoredMBeanDesiredInfo.getMonitoredAttributes();

        // debug("\t DEBUG: there are " + attributesToLog.size() + " attributes to log");
        // debug("\t DEBUG: there are " + allAttributesInLiveBean.length + " attributes to to look through");


        for (MBeanAttributeInfo attributeInLiveBean : allAttributesInLiveBean) {
            if (ignoreFilters
                || attributesToLog.contains(attributeInLiveBean.getName())) {

                String attributeName = attributeInLiveBean.getName();
                String attributeValue = null;

                try {
                    Object attributeValueObject =
                            mBeanServerConnection.getAttribute(liveMBeanStub,
                                                               attributeName);
                    if (attributeValueObject == null) {
                        logger.debug("\tvalue for \"" +
                                attributeName + "\" is null, not emitting metric");
                        continue;
                    }
                    else if (!isPrimitiveOrWrapperOrStringType(attributeValueObject.getClass())) {
                        logger.debug("\tvalue for \"" +
                                attributeName + "\" is not [wrapped] primitive nor String, not emitting metric");
                        logger.debug("\t\t(value was \"" + attributeValueObject.toString() + "\")");
                        continue;
                    }

                    attributeValue = attributeValueObject.toString();
                }
                catch (UnsupportedOperationException ex) {
                    logger.warn("UnsupportedOperationException: (" + liveMBeanStub.getCanonicalName() +
                            ").(attributeName=\"" + attributeName + "\")");
                    continue;
                }
                catch (JMException ex) {
                    // StringWriter errors = new StringWriter();
                    // ex.printStackTrace(new PrintWriter(errors));
                    logger.error("JMException trying to display (" + liveMBeanStub.getCanonicalName() +
                            ").(attributeName=\"" + attributeName +
                            "\") -- caused by: " + ClassUtils.rootCauseOf(ex).toString());

                    continue;
                }
                catch (Throwable ex) {
                    // getting here means the server threw an exception during the getAttribute call to the stub
                    if (ex instanceof  InvocationTargetException) {
                        // InvocationTargetException itex = (InvocationTargetException) ex;
                        // StringWriter errors = new StringWriter();
                        // itex.printStackTrace(new PrintWriter(errors));
                        logger.error("InvocationTargetException on getAttribute: (" +
                                    liveMBeanStub.getCanonicalName() +
                                    ").(attributeName=\"" + attributeName + "\"): " +
                                    ClassUtils.rootCauseOf(ex).toString());
                        continue;
                    }
                    throw ex;
                }

                // use the metric names we specified on the command line
                renderer.writeMBeanMetric(liveMBeanStub, attributeName , attributeValue);
            } // end if matching the list of attr we are interested in
            else {
                logger.debug("\t => attribute " + attributeInLiveBean.getName() +
                        " did not match any requested metric names");
            }
        }
    }


}
