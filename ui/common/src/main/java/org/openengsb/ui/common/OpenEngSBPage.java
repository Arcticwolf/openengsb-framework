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

package org.openengsb.ui.common;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.openengsb.core.api.context.ContextCurrentService;
import org.openengsb.core.api.context.ContextHolder;

/**
 * Baseclass for any page in the OpenEngSB and for client Projects. It initializes a context when started the first
 * time. In order for this page to work, a spring-bean of the class
 *
 * @link{org.openengsb.core.common.context.ContextCurrentService must be available
 */
public class OpenEngSBPage extends WebPage {

    public static final String CONTEXT_PARAM = "context";

    private static Log log = LogFactory.getLog(OpenEngSBPage.class);

    @SpringBean
    protected ContextCurrentService contextService;

    public OpenEngSBPage() {
        initContextForCurrentThread();
    }

    public OpenEngSBPage(PageParameters parameters) {
        super(parameters);
        log.debug("creating new page using parameters: " + parameters);
        Object object = parameters.get(CONTEXT_PARAM);
        if (object != null && object instanceof String[]) {
            final String contextId = ((String[]) object)[0];
            log.debug("setting context-id from pageparameter: " + contextId);
            ContextHolder.get().setCurrentContextId(contextId);
        }
    }

    protected List<String> getAvailableContexts() {
        if (contextService == null) {
            return new ArrayList<String>();
        }
        return contextService.getAvailableContexts();
    }

    protected final void initContextForCurrentThread() {
        String sessionContextId = ContextHolder.get().getCurrentContextId();
        if (sessionContextId == null) {
            sessionContextId = "foo";
        }
        try {
            if (contextService != null) {
                contextService.setThreadLocalContext(sessionContextId);
            }
        } catch (IllegalArgumentException e) {
            log.debug("initialize default-values in contexts");
            contextService.createContext(sessionContextId);
            contextService.createContext(sessionContextId + "2");
            contextService.setThreadLocalContext(sessionContextId);
            contextService.putValue("domain/NotificationDomain/defaultConnector/id", "notification");
            contextService.putValue("domain/IssueDomain/defaultConnector/id", "issue");
            contextService.putValue("domain/ExampleDomain/defaultConnector/id", "example");
            contextService.putValue("domain/AuditingDomain/defaultConnector/id", "auditing");
        }
    }

    /**
     * @return the class name, which should be the index in navigation bar
     *
     */
    public String getHeaderMenuItem() {
        return this.getClass().getSimpleName();
    }
}