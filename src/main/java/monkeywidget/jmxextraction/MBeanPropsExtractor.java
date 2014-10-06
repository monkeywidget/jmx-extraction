package monkeywidget.jmxextraction;

import monkeywidget.jmxextraction.cli.CommandLineArgsConfigurer;
import monkeywidget.jmxextraction.core.MonitoredMBeanDesiredInfo;
import monkeywidget.jmxextraction.core.MonitoredServiceHostInfo;
import monkeywidget.jmxextraction.fetch.JMXClient;
import monkeywidget.jmxextraction.fetch.RemoteJMXClient;
import monkeywidget.jmxextraction.render.MBeanPropertyRenderer;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Command-line utility that
 * - connects to JMX server specified by arguments
 * - connects to a JMX MBean matching a pattern (another argument)
 * - finds all the attributes matching a list of names (another argument)
 * - prints those values to be picked up by something else (like Sensu)
 *
 * @author monkeywidget
 * Date: 2/21/14
 * Time: 12:10 PM
 */
public final class MBeanPropsExtractor {

    static Logger logger = null;

    static {
        logger = LoggerFactory.getLogger(MBeanPropsExtractor.class);
    }

    private MBeanPropsExtractor() {
        // never called
    }

    public static CommandLineArgsConfigurer parseAndCheckArguments(String[] args) {

        CommandLineArgsConfigurer parser = null;

        try {
            parser = new CommandLineArgsConfigurer(args);
        }
        catch(ParseException exp) {
            // oops, something went wrong
            logger.error("Parsing failed!  Reason: " + exp.getMessage() +
                        " for args (" + Arrays.deepToString(args) + ")");
            System.exit(1);
        }

        return parser;
    }

    public static void main(String[] args) {

        CommandLineArgsConfigurer parser = parseAndCheckArguments(args);

        MonitoredServiceHostInfo monitoredServiceHostInfo = null;
        MonitoredMBeanDesiredInfo monitoredMBeanInfo = null;
        MBeanPropertyRenderer renderer = null;

        try {
            monitoredServiceHostInfo = parser.getMonitoredServiceHostInfo();
            monitoredMBeanInfo = parser.getMonitoredMBeanInfo();
            renderer = parser.getRenderer();
        }
        catch(Exception exp) {
            // oops, something went wrong
            logger.error("Parsing failed!  Reason: " + exp.getMessage());
            System.exit(1);
        }

        try {
            // query the MBeans for all their values and render
            JMXClient client = new RemoteJMXClient(monitoredServiceHostInfo,
                    monitoredMBeanInfo,
                    renderer);

            client.fetchAndRenderMBeanProperties();
        }
        catch(Exception exp) {
            // oops, something went wrong
            logger.error("Error getting JMX metrics for params ( " +
                        monitoredServiceHostInfo.toString() + "; " +
                        monitoredMBeanInfo.toString() + "; " +
                        exp.getMessage());
            System.exit(1);
        }

    } // end main

} // end class