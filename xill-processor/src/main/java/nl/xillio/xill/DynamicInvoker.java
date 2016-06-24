/**
 * Copyright (C) 2014 Xillio (support@xillio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.xillio.xill;

import me.biesaart.utils.Log;
import org.slf4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


/**
 * This class represents a wrapper that can search for and invoke a named method dynamically
 *
 * @param <I> The argument base type for the method.
 */
public class DynamicInvoker<I> {
    private static final Logger logger = Log.get();
    /**
     * If set to true the invocation process will be logged more verbose.
     */
    private boolean VERBOSE = false;

    private final String methodName;
    private final Object invokeObject;

    public void setVERBOSE(boolean value) {
        VERBOSE = value;
    }

    /**
     * Create a new {@link DynamicInvoker} for a specific method name.
     *
     * @param methodName   The name of the method
     * @param invokeObject The object to invoke the method on
     */
    public DynamicInvoker(final String methodName, final Object invokeObject) {
        this.methodName = methodName;
        this.invokeObject = invokeObject;
    }

    /**
     * Invoke the best method found by name that has one parameter and best parameter type.
     *
     * @param <O>        The argument base type for the method.
     * @param argument   The input argument for the method
     * @param returnType The return type of the method
     * @return The result of the selected method
     * @throws SecurityException         If a security manager, s, is present and any of the following conditions is met:
     *                                   <ul>
     *                                   <li>the caller's class loader is not the same as the class loader of this class and invocation of s.checkPermission method with RuntimePermission("accessDeclaredMembers") denies access to
     *                                   the declared methods within this class
     *                                   <li>the caller's class loader is not the same as or an ancestor of the class loader for the current class and invocation of s.checkPackageAccess() denies access to the package of this
     *                                   class</li>
     *                                   </ul>
     * @throws InvocationTargetException if the underlying method throws an exception or no compatible method could be found.
     * @throws IllegalArgumentException  if the method is an instance method and the specified object argument is not an instance of the class or interface declaring the underlying method (or of a subclass or implementor
     *                                   thereof);
     *                                   if the number of actual and formal parameters differ; if an unwrapping conversion for primitive arguments fails; or if,
     *                                   after possible unwrapping, a parameter value cannot be converted to the corresponding formal parameter type by a method invocation conversion.
     */
    @SuppressWarnings("unchecked")
    public <O> O invoke(final I argument, final Class<O> returnType) throws InvocationTargetException, IllegalArgumentException {
        Method parseMethod = null;
        log("Finding method for: " + argument.getClass());

        // Select the right method
        for (Method currentMethod : invokeObject.getClass().getDeclaredMethods()) {
            log("Checking: " + currentMethod);

            // Only search 'parse' methods
            if (!currentMethod.getName().equals(methodName)) {
                continue;
            }
            log("Name OK!");

            // Don't invoke private methods
            if (Modifier.isPrivate(currentMethod.getModifiers())) {
                continue;
            }
            log("Access OK!");

            // Only 1 parameters
            if (currentMethod.getParameterCount() != 1) {
                continue;
            }
            log("Param Count OK!");

            Class<?> paramType = currentMethod.getParameters()[0].getType();

            // Check if superclass
            if (!paramType.isAssignableFrom(argument.getClass())) {
                continue;
            }
            log("Param Type OK!");

            // Check return type
            if (!returnType.isAssignableFrom(currentMethod.getReturnType())) {
                continue;
            }
            log("InstructionFlow Type OK!");

            // We have a potential match see if it is better than the current one
            if (parseMethod == null || parseMethod.getParameterTypes()[0].isAssignableFrom(paramType)) {
                parseMethod = currentMethod;
                log("\tSELECTED!");
            }
        }

        if (parseMethod == null) {
            throw new InvocationTargetException(null, "No supported method was found.");
        }

        try {
            return (O) parseMethod.invoke(invokeObject, argument);
        } catch (IllegalAccessException e) {
            // This should not be able to happen since it is checked above
            throw new RuntimeException("The disposer tried to access a private method. Please report this to development.", e);
        }
    }

    private void log(final String message) {
        if (VERBOSE) {
            logger.debug(message);
        }
    }

}
