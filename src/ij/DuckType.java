package ij;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * DuckType. Implements Duck Typing for Java.  ("If it walks like a duck,
 * quacks like a duck, it...").  Essentially allows programs to treat
 * objects from separate hierarchies as if they were designed with common
 * interfaces as long as they adhere to common naming conventions.
 *
 * This version is the strict DuckType.  All methods present in
 * interfaceToImplement must be present on the target object.
 *
 * @author djo
 */
/* Use:
        public static Object implement(Class interfaceToImplement, Object object) {
            return Proxy.newProxyInstance(interfaceToImplement.getClassLoader(),
                new Class[] {interfaceToImplement}, new DuckType(object));
        }
 *
 */
public class DuckType implements InvocationHandler {

    protected DuckType(Object object) {
        this.object = object;
        this.objectClass = object.getClass();
    }
    protected Object object;
    protected Class objectClass;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Method realMethod = objectClass.getMethod(method.getName(), method.getParameterTypes());
        return realMethod.invoke(object, args);
    }

    /**
     * Causes object to implement the interfaceToImplement and returns
     * an instance of the object implementing interfaceToImplement even
     * if interfaceToImplement was not declared in object.getClass()'s
     * implements declaration.
     *
     * This works as long as all methods declared in interfaceToImplement
     * are present on object.
     *
     * @param interfaceToImplement The Java class of the interface to implement
     * @param object The object to force to implement interfaceToImplement
     * @return object, but now implementing interfaceToImplement
     */
    public static Object implement(Class interfaceToImplement, Object object) {
        return Proxy.newProxyInstance(interfaceToImplement.getClassLoader(),
                new Class[]{interfaceToImplement}, new DuckType(object));
    }

    /**
     * Indicates if object is a (DuckType) instace of intrface.  That is,
     * is every method in intrface present on object.
     *
     * @param intrface The interface to implement
     * @param object The object to test
     * @return true if every method in intrface is present on object.  false otherwise
     */
    public static boolean instanceOf(Class intrface, Object object) {
        final Method[] methods = intrface.getMethods();
        Class candclass = object.getClass();
        for (int methodidx = 0; methodidx < methods.length; methodidx++) {
            Method method = methods[methodidx];
            try {
                candclass.getMethod(method.getName(), method.getParameterTypes());
            } catch (NoSuchMethodException e) {
                return false;
            }
        }
        return true;
    }


}