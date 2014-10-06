package monkeywidget.jmxextraction.render;

import javax.management.ObjectName;
// import static monkeywidget.jmxextraction.util.CanonicalNameToMetricNameConverter.convertCanonicalMBeanNameToMetricName;
/**
 * Renderer class intended to be extended to output to a variety
 * of logs, alerts, etc
 *
 * @author monkeywidget
 */
public abstract class AbstractMBeanPropertyRenderer
        implements MBeanPropertyRenderer {


    /**
     * @param mBeanAttributeName
     * @param mBeanAttributeValue
     */
    public abstract void writeMBeanMetric(ObjectName liveMBeanStub,
                                          String mBeanAttributeName,
                                          String mBeanAttributeValue);



}
