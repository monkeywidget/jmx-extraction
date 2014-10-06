package monkeywidget.jmxextraction.render;

import javax.management.ObjectName;

/**
 * Renders a specific property on an MBean to a log or alert
 *
 * @author monkeywidget
 * Date: 2/21/14
 * Time: 12:08 PM
 */
public interface MBeanPropertyRenderer {

    void writeMBeanMetric(ObjectName liveMBeanStub,
                          String mBeanAttributeName,
                          String mBeanAttributeValue);

}