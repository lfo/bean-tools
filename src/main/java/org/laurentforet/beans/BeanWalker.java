package org.laurentforet.beans;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;


/**
 * Usefull class which apply a function to all objects of type <T> while walking
 * across the graph object (walk(object)) the results of each calls are stored in
 * a TreeMap indexed by their path. The results are of type <U> .
 * 
 * 
 * The implementation uses Google guava Function, and Springframework beans library.
 * 
 * Cycles are avoided in applying only once the function to a same path.
 * 
 * @author lforet
 */
public class BeanWalker<T, U> {

    private Class<T> typeClass;
    private Map<String, U> results = new TreeMap<>();
    private Set<Object> handledObjects = new HashSet<>();
    private Function<T, U> function;

    public BeanWalker(Function<T, U> function) {
        assert function != null;
        this.typeClass = getParameterClass(function, 0);
        this.function = function;
    }

    public BeanWalker walk(Object object) {
        doWalk(object == null ? "null" : object.getClass().getSimpleName(), object);
        return this;
    }

    public Map<String, U> getResults() {
        return results;
    }

    public void clear() {
        results.clear();
    }

    protected void doWalk(String path, Object object) throws BeanException {
        try {
            if (object == null || handledObjects.contains(object)) {
                return;
            }
           
            if (implementsTypeClass(object)) {
                
                add(path, object, function.apply((T) object));
            }
            if (object instanceof Collection) {
                int i = 0;
                for (Object current : (Collection) object) {
                    doWalk(path + "[" + i++ + "]", current);
                }
            }
            for (PropertyDescriptor descr : BeanUtils.getPropertyDescriptors(object.getClass())) {
                Method method = descr.getReadMethod();
                if (method == null) {
                    continue;
                }
                Object propertyValue = method.invoke(object);
                if (propertyValue == null) {
                    continue;
                }
                if (implementsTypeClass(propertyValue)
                        || propertyValue instanceof Collection) {
                    doWalk(path + "." + descr.getName(), method.invoke(object));
                }
            }
        } catch (BeansException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new BeanException(ex);
        }
    }

    private boolean implementsTypeClass(Object object) {
        List<Class<?>> interfaces = Lists.newArrayList(object.getClass().getInterfaces());
        interfaces.add(object.getClass());
        interfaces.add(Object.class);
        return interfaces.contains(typeClass);
    }

    private void add(String path, Object object, U result) {
        if (result != null) {
            results.put(path, result);
            handledObjects.add(object);
        }
    }
    
    private Class getParameterClass(Object parametizedClass, int index) {
        return (Class) ((ParameterizedType) parametizedClass.getClass().getGenericInterfaces()[0]).
                getActualTypeArguments()[index];
    }
}
