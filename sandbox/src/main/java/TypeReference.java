import java.lang.reflect.Type;
import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Courtesy of Neil Gafter's blog.
 * Thanks to Curt Cox for the pointer.
 */
public abstract class TypeReference<T> {

 private final Type type;
 private volatile Constructor<?> constructor;

 protected TypeReference() {
     Type superclass = getClass().getGenericSuperclass();
     if (superclass instanceof Class) {
         throw new RuntimeException("Missing type parameter.");
     }
     this.type = ((ParameterizedType) superclass).getActualTypeArguments()[0];
 }

 /**
  * @return a new instance of {@code T} using the default, no-arg
  * constructor.
  * @throws IllegalAccessException on security reflection issues
  * @throws NoSuchMethodException there's not getRawType on the type
  * @throws java.lang.reflect.InvocationTargetException if a reflective call causes an exception in the underlying instance
  * @throws InstantiationException if the instance cannot be instantiated
  */
 @SuppressWarnings("unchecked")
 public T newInstance()
         throws NoSuchMethodException, IllegalAccessException,
         InvocationTargetException, InstantiationException {
     if (constructor == null) {
         Class<?> rawType = type instanceof Class<?>
             ? (Class<?>) type
             : (Class<?>) ((ParameterizedType) type).getRawType();
         constructor = rawType.getConstructor();
     }
     return (T) constructor.newInstance();
 }

 /**
  * @return the referenced type.
  */
 public Type getType() {
     return this.type;
 }
 
     public static void main(String[] args) throws Exception {
        List<String> l1 = new TypeReference<ArrayList<String>>() {}.newInstance();
        List l2 = new TypeReference<ArrayList>() {}.newInstance();
    }
}
