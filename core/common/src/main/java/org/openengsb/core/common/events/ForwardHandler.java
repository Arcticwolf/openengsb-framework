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

package org.openengsb.core.common.events;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openengsb.core.api.Event;
import org.openengsb.core.api.workflow.WorkflowException;
import org.openengsb.core.api.workflow.WorkflowService;
import org.openengsb.core.common.AbstractOpenEngSBInvocationHandler;

public class ForwardHandler extends AbstractOpenEngSBInvocationHandler {

    private Log log = LogFactory.getLog(ForwardHandler.class);
    private WorkflowService workflowService;

    public ForwardHandler() {
        super(true);
    }

    @Override
    public Object handleInvoke(Object proxy, Method method, Object[] args) throws IllegalAccessException,
        InvocationTargetException {
        checkMethod(method);
        log.info("Forwarding event to workflow service");
        try {
            workflowService.processEvent((Event) args[0]);
        } catch (WorkflowException e) {
            throw new InvocationTargetException(e);
        }
        return null;
    }

    private void checkMethod(Method method) {
        if (!method.getName().equals("raiseEvent")) {
            throw new EventProxyException(
                "Event proxy can only handle methods named raiseEvent, but encountered method named: '"
                        + method.getName() + "'.");
        } else if (method.getParameterTypes().length == 0) {
            throw new EventProxyException(
                "Event proxy can only handle methods named raiseEvent where the first parameter is of type Event, "
                        + "but encountered invocation of method raiseEvent without parameter. Method: " + method);
        } else if (!Event.class.isAssignableFrom(method.getParameterTypes()[0])) {
            throw new EventProxyException(
                "Event proxy can only handle methods named raiseEvent where the first parameter is of type Event, "
                        + "but encountered invocation of method raiseEvent where first parameter is no Event. Method: "
                        + method);
        }
    }

    public void setWorkflowService(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

}
