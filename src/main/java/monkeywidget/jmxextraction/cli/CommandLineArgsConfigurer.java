package monkeywidget.jmxextraction.cli;

import monkeywidget.jmxextraction.core.MonitoredMBeanDesiredInfo;
import monkeywidget.jmxextraction.core.MonitoredServiceHostInfo;
import monkeywidget.jmxextraction.render.MBeanPropertyRenderer;
import monkeywidget.jmxextraction.render.StdOutMBeanPropertyRenderer;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;


/**
 * The CLI parser for MBeanPropsExtractor.
 * - Reads in all the options
 * - retains object instances of the generated configurations
 * - objects are retrievable using getter methods
 *
 *
 * @author monkeywidget
 * Date: 2/21/14
 * Time: 1:30 PM
 */
public class CommandLineArgsConfigurer {

    private MonitoredServiceHostInfo monitoredServiceHostInfo;
    private MonitoredMBeanDesiredInfo monitoredMBeanInfo;
    private MBeanPropertyRenderer renderer;

    /** GETTERS **/
    public MonitoredServiceHostInfo getMonitoredServiceHostInfo() { return monitoredServiceHostInfo; }
    public MonitoredMBeanDesiredInfo getMonitoredMBeanInfo() { return monitoredMBeanInfo; }
    public MBeanPropertyRenderer getRenderer() { return renderer; }

    private Logger logger = LoggerFactory.getLogger(CommandLineArgsConfigurer.class);

    /**
     * Parse out the configurations from arguments fetched from the command line
     *
     * @param args
     * @throws RuntimeException if there are missing or inconsistent arguments
     */
    public CommandLineArgsConfigurer(String[] args)
            throws ParseException {

        Options options = buildOptions();   // defines the options that are required

        CommandLineParser parser = new DefaultParser();
        CommandLine line;

        // if any args are missing, this will throw an exception
        try {
            line = parser.parse(options, args);
        }
        catch (ParseException pex) {
            HelpFormatter formatter = new HelpFormatter();
            StringWriter out = new StringWriter();
            formatter.printUsage(new PrintWriter(out), 160,
                                "jmx-extraction", options);
            logger.error(out.toString());  // just prints the usage to the log
            logger.error("Parse problem: " + pex.getMessage());
            throw pex;
        }

        populateMonitoredServiceHostInfo(line);
        populateMonitoredMBeanDesiredInfo(line);

        // could add here: a way to output to a file instead,
        // the idea being renderer would not be a StdOut etc

        renderer = new StdOutMBeanPropertyRenderer();
    }


    /**
     * Build the specifications of all the command line options
     */
    private Options buildOptions() {
        Options options = new Options();

        /* service host info */

        addRequiredOptionTo("H","jmxHost", "the host serving the JMX interface", options);
        addRequiredOptionTo("P","jmxPort", "the port for the JMX interface", options);
        addRequiredOptionTo("J","jmxJndi", "the JNDI name of the JMX interface", options);

        addNonRequiredOptionTo("U","jmxUser", "OPTIONAL username for a secured JMX interface", options);
        addNonRequiredOptionTo("W","jmxPassword", "OPTIONAL password for a secured JMX interface", options);
        addNonRequiredOptionTo("v","verbose", "OPTIONAL verbose mode", options);

        /* mbean info */


        // these flags each change behavior of the utility:
        addNonRequiredOptionTo("k","keyMatch",
                               "OPTIONAL substring used to find the MBean (queries all beans if missing)",
                               options);
        addNonRequiredOptionTo("c","csvAttributes",
                               "OPTIONAL comma-separated list of attributes to monitor (outputs all if flag missing)",
                               options);

        return options;
    }

    /**
     * helper utility to build an option, and add it to the cli.Options
     * @param flag
     * @param longOptName
     * @param description
     * @param options
     * @param requiredP whether it's required
     */
    private void addOptionTo(String flag, String longOptName,
                                     String description,
                                     Options options,
                                     boolean requiredP) {

        Option optionToAdd = Option.builder(flag)
                .hasArg()
                .longOpt(longOptName)
                .desc(description)
                .required(requiredP)
                .build();
        options.addOption(optionToAdd);
    }

    /**
     * helper utility to build a required option, and add it to the cli.Options
     * @param flag
     * @param longOptName
     * @param description
     * @param options
     */
    private void addRequiredOptionTo(String flag, String longOptName,
                                     String description,
                                     Options options) {
        addOptionTo(flag,longOptName,description,options,true);
    }

    /**
     * helper utility to build a non-required option, and add it to the cli.Options
     * @param flag
     * @param longOptName
     * @param description
     * @param options
     */
    private void addNonRequiredOptionTo(String flag, String longOptName,
                                     String description,
                                     Options options) {
        addOptionTo(flag,longOptName,description,options,false);
    }



    private String getRequiredArgumentForFlag(CommandLine line, String flag) {

        String value = line.getOptionValue(flag);

        if (value == null) {
            logger.error("Missing argument for flag \"" + flag + "\"");
            System.exit(1);
        }

        return value;
    }

    private void populateMonitoredServiceHostInfo(CommandLine line) {

        String jmxHost = getRequiredArgumentForFlag(line, "H");
        String jmxPort = getRequiredArgumentForFlag(line, "P");
        String jmxJndi = getRequiredArgumentForFlag(line, "J");

        // optional:
        String jmxUser = line.getOptionValue("U");
        String jmxPassword = line.getOptionValue("W");

        if (jmxUser != null
                && jmxPassword != null) {
            monitoredServiceHostInfo = new MonitoredServiceHostInfo(jmxHost, jmxPort, jmxJndi,
                                                                    jmxUser, jmxPassword);
        }
        else {
            monitoredServiceHostInfo = new MonitoredServiceHostInfo(jmxHost, jmxPort, jmxJndi);
        }
    }


    private void populateMonitoredMBeanDesiredInfo(CommandLine line) {

        String keymatch = line.getOptionValue("k");
        String csvAttributes = line.getOptionValue("c");

        monitoredMBeanInfo =
                new MonitoredMBeanDesiredInfo(keymatch);
        monitoredMBeanInfo.setMonitoredAttributesCsvList(csvAttributes);

        for (String attribute : monitoredMBeanInfo.getMonitoredAttributes()) {
            logger.debug("debug: monitoring attribute \"" + attribute + "\"");
        }
    }


}
