package monkeywidget.jmxextraction.util;

import java.util.HashSet;
import java.util.Set;

/** reimplementing
 *  - org.apache.commons.lang3.ClassUtils.isPrimitiveOrWrapper
 *  - org.apache.commons.lang.exception.ExceptionUtils.getCause
 *  - org.apache.commons.lang3.ClassUtils.isPrimitiveOrWrapper
 * @author monkeywidget
 */
public final class ClassUtils {

    private ClassUtils() { }

    public static boolean isPrimitiveOrWrapperOrStringType(Class<?> clazz) {
        if (clazz.isPrimitive()) { return true; }
        return getWrapperTypesAndString().contains(clazz);
    }

    private static Set<Class<?>> getWrapperTypesAndString() {
        Set<Class<?>> ret = new HashSet<Class<?>>();
        ret.add(Boolean.class);
        ret.add(Character.class);
        ret.add(Byte.class);
        ret.add(Short.class);
        ret.add(Integer.class);
        ret.add(Long.class);
        ret.add(Float.class);
        ret.add(Double.class);
        ret.add(Void.class);
        ret.add(String.class);
        return ret;
    }

    public static Throwable rootCauseOf(Throwable throwable) {
        Throwable throwableWeAreLookingAt = throwable;
        while (throwableWeAreLookingAt.getCause() != null) {
            throwableWeAreLookingAt = throwableWeAreLookingAt.getCause();
        }
        return throwableWeAreLookingAt;
    }


}
