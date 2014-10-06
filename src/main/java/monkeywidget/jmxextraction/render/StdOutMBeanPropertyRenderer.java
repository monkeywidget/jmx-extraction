package monkeywidget.jmxextraction.render;

import javax.management.ObjectName;
import java.io.PrintStream;

/**
 *
 * Prints metric data to StdOut - err log is still sent to log4j
 * @author  monkeywidget
 */
public class StdOutMBeanPropertyRenderer
        extends AbstractMBeanPropertyRenderer {


    protected PrintStream outputStream;

    public StdOutMBeanPropertyRenderer() {
        outputStream = System.out;
    }


    public void writeMBeanMetric(ObjectName liveMBeanStub,
                                 String mBeanAttributeName,
                                 String mBeanAttributeValue) {

        outputStream.println("(" + liveMBeanStub.getCanonicalName() +
                             ") name=" + mBeanAttributeName +
                             " value=" + mBeanAttributeValue);
    }

}
