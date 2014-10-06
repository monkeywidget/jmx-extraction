package monkeywidget.jmxextraction.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Records the JMX MBean-level info for the desired metrics to be monitored.
 * Holds a list of each property to be monitored
 *
 * @author monkeywidget
 */
public class MonitoredMBeanDesiredInfo {

    /*** WHICH BEANS TO QUERY ?  ***/

    private boolean queryAllBeansForMetricValuesP = false;

    // only used if queryAllBeansForMetricValuesP == false:
    private String keyValueMatch; // may be null
                                  // Note: it's easier to specify a key to match
                                  // rather than an explicit property


    /*** WHICH METRICS TO QUERY AND PRINT ?  ***/

    private boolean queryAllMetricsNotOnlySubsetP = true;

    // only used if queryAllMetricsNotOnlySubsetP == false
    private List<String> monitoredAttributes = new  ArrayList<>();

    public MonitoredMBeanDesiredInfo(String keyMatch) {
        this.keyValueMatch = keyMatch;
        this.queryAllBeansForMetricValuesP = (keyMatch == null);
    }

    // public String getDomain() { return this.domain; }

    public boolean queryAllBeansForMetricValuesP() { return queryAllBeansForMetricValuesP; }
    public String getKeyValueMatch() { return this.keyValueMatch; }

    public boolean queryAllMetricsNotOnlySubsetP() { return queryAllMetricsNotOnlySubsetP; }

    public void setMonitoredAttributesCsvList(String monitoredAttributesCsvList) {
        if (monitoredAttributesCsvList == null) {
            this.queryAllMetricsNotOnlySubsetP = true;
        }
        else {
            this.queryAllMetricsNotOnlySubsetP = false;
            this.setMonitoredAttributes(Arrays.asList(monitoredAttributesCsvList.split("\\s*,\\s*")));
        }
    }

    public String getMonitoredAttributesAsCsv() {
        if (monitoredAttributes.size() <1) {
            return "";
        }

        // there is at least one element

        // since this is Java <8 we don't have a "join"
        StringBuffer csvOfMonitoredAttributes = new StringBuffer();
        for (String listItem : monitoredAttributes) {
            csvOfMonitoredAttributes.append(listItem + ",");
        }
        String csvWithoutLastComma = csvOfMonitoredAttributes.toString();
        csvWithoutLastComma = csvWithoutLastComma.substring(0,csvWithoutLastComma.length()-1);
        return csvWithoutLastComma;
    }

    public List<String> getMonitoredAttributes() { return monitoredAttributes; }
    public void setMonitoredAttributes(List<String> newAttr) { this.monitoredAttributes = newAttr; }

    public String toString() {
        return "key_to_match:" + getKeyValueMatch() +
                ", monitored_attributes_csv:" + getMonitoredAttributesAsCsv();
    }

}
