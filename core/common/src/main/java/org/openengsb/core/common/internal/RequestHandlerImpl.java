/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.core.common.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.openengsb.core.common.OpenEngSBService;
import org.openengsb.core.common.communication.MethodCall;
import org.openengsb.core.common.communication.MethodReturn;
import org.openengsb.core.common.communication.MethodReturn.ReturnType;
import org.openengsb.core.common.communication.RequestHandler;
import org.openengsb.core.common.util.OsgiServiceUtils;

public class RequestHandlerImpl implements RequestHandler {

    @Override
    public MethodReturn handleCall(MethodCall call) {
        String serviceId = call.getMetaData().get("serviceId");
        if (serviceId == null) {
            throw new IllegalArgumentException("missing definition of serviceid in methodcall");
        }
        OpenEngSBService service;
        service = OsgiServiceUtils.getServiceWithId(OpenEngSBService.class, serviceId);

        Object[] args = call.getArgs();
        Method method = findMethod(service, call.getMethodName(), getArgTypes(args));

        return invokeMethod(service, method, args);
    }

    private MethodReturn invokeMethod(OpenEngSBService service, Method method, Object[] args) {
        MethodReturn resultContainer = new MethodReturn();
        try {
            Object result = method.invoke(service, args);
            if (method.getReturnType().getName().equals("void")) {
                resultContainer.setType(ReturnType.Void);
            } else {
                resultContainer.setType(ReturnType.Object);
                resultContainer.setArg(result);
            }
        } catch (InvocationTargetException e) {
            resultContainer.setType(ReturnType.Exception);
            resultContainer.setArg(e.getCause());
        } catch (IllegalAccessException e) {
            resultContainer.setType(ReturnType.Exception);
            resultContainer.setArg(e);
        }
        return resultContainer;
    }

    private Method findMethod(OpenEngSBService service, String methodName, Class<?>[] argTypes) {
        Method method;
        try {
            method = service.getClass().getMethod(methodName, argTypes);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(e);
        }
        return method;
    }

    private Class<?>[] getArgTypes(Object[] args) {
        Class<?>[] result = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            result[i] = args[i].getClass();
        }
        return result;
    }

}
