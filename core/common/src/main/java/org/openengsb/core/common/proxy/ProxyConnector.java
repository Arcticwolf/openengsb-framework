/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.core.common.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.openengsb.core.common.communication.CallRouter;
import org.openengsb.core.common.communication.MethodCall;
import org.openengsb.core.common.communication.MethodReturn;

public class ProxyConnector implements InvocationHandler {

    private String portId;
    private String destination;
    private final Map<String, String> metadata = new HashMap<String, String>();

    private CallRouter callRouter;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        MethodReturn callSync =
            callRouter.callSync(portId, destination, new MethodCall(method.getName(), args, this.metadata));
        switch (callSync.getType()) {
            case Object:
                return callSync.getArg();
            case Void:
                return null;
            case Exception:
                throw new RuntimeException(callSync.getArg().toString());
            default:
                throw new IllegalStateException("Return Type has to be either Object or Exception");
        }
    }

    public final void setPortId(String id) {
        this.portId = id;
    }

    public final void setDestination(String destination) {
        this.destination = destination;
    }

    public void addMetadata(String key, String value) {
        this.metadata.put(key, value);
    }

    public final void setCallRouter(CallRouter callRouter) {
        this.callRouter = callRouter;
    }

    public final String getPortId() {
        return portId;
    }

    public final String getDestination() {
        return destination;
    }

    public final CallRouter getCallRouter() {
        return callRouter;
    }
}
