/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.client.runtime;

import java.util.List;
import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.hal.core.HostSelectionEvent;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.mbui.MbuiPresenter;
import org.jboss.hal.core.mbui.MbuiView;
import org.jboss.hal.core.mvp.HasVerticalNavigation;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Composite;
import org.jboss.hal.dmr.model.CompositeResult;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.Requires;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;

/**
 * @author Harald Pehl
 */
public class HostPresenter extends MbuiPresenter<HostPresenter.MyView, HostPresenter.MyProxy> {

    static final String HOST_ADDRESS = "/{selected.host}";
    static final String INTERFACE_ADDRESS = HOST_ADDRESS + "/interface=*";
    static final String JVM_ADDRESS = HOST_ADDRESS + "/jvm=*";
    static final String PATH_ADDRESS = HOST_ADDRESS + "/path=*";
    static final String SOCKET_BINDING_GROUP_ADDRESS = HOST_ADDRESS + "/socket-binding-group=*";
    static final String SYSTEM_PROPERTY_ADDRESS = HOST_ADDRESS + "/system-property=*";


    // @formatter:off
    @ProxyCodeSplit
    @NameToken(NameTokens.HOST_CONFIGURATION)
    @Requires(value = {HOST_ADDRESS, INTERFACE_ADDRESS, JVM_ADDRESS, PATH_ADDRESS, SOCKET_BINDING_GROUP_ADDRESS,
            SYSTEM_PROPERTY_ADDRESS}, recursive = false)
    public interface MyProxy extends ProxyPlace<HostPresenter> {}

    public interface MyView extends MbuiView<HostPresenter>, HasVerticalNavigation {
        void updateHost(Host host);
        void updateInterfaces(List<NamedNode> interfaces);
        void updateJvms(List<NamedNode> interfaces);
        void updatePaths(List<NamedNode> interfaces);
        void updateSocketBindingGroups(List<NamedNode> interfaces);
        void updateSystemProperties(List<NamedNode> interfaces);
    }
    // @formatter:on


    private final StatementContext statementContext;
    private final Dispatcher dispatcher;

    @Inject
    public HostPresenter(final EventBus eventBus,
            final HostPresenter.MyView view,
            final HostPresenter.MyProxy proxy,
            final Finder finder,
            final StatementContext statementContext,
            final Dispatcher dispatcher) {
        super(eventBus, view, proxy, finder);
        this.statementContext = statementContext;
        this.dispatcher = dispatcher;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public void prepareFromRequest(final PlaceRequest request) {
        super.prepareFromRequest(request);
        String host = request.getParameter(HOST, null);
        if (host != null) {
            getEventBus().fireEvent(new HostSelectionEvent(host));
        }

    }

    @Override
    protected FinderPath finderPath() {
        return new FinderPath()
                .append(HOST, Host.id(statementContext.selectedHost()), Names.HOST, statementContext.selectedHost());
    }

    @Override
    protected void reload() {
        ResourceAddress hostAddress = new ResourceAddress().add(HOST, statementContext.selectedHost());
        Operation hostOp = new Operation.Builder(READ_RESOURCE_OPERATION, hostAddress)
                .param(INCLUDE_RUNTIME, true)
                .build();
        Operation interfacesOp = new Operation.Builder(READ_CHILDREN_RESOURCES_OPERATION, hostAddress)
                .param(CHILD_TYPE, INTERFACE)
                .param(INCLUDE_RUNTIME, true)
                .build();
        Operation jvmsOp = new Operation.Builder(READ_CHILDREN_RESOURCES_OPERATION, hostAddress)
                .param(CHILD_TYPE, JVM)
                .param(INCLUDE_RUNTIME, true)
                .build();
        Operation pathsOp = new Operation.Builder(READ_CHILDREN_RESOURCES_OPERATION, hostAddress)
                .param(CHILD_TYPE, PATH)
                .param(INCLUDE_RUNTIME, true)
                .build();
        Operation socketBindingGroupsOp = new Operation.Builder(READ_CHILDREN_RESOURCES_OPERATION, hostAddress)
                .param(CHILD_TYPE, SOCKET_BINDING_GROUP)
                .param(INCLUDE_RUNTIME, true)
                .build();
        Operation systemPropertiesOp = new Operation.Builder(READ_CHILDREN_RESOURCES_OPERATION, hostAddress)
                .param(CHILD_TYPE, SYSTEM_PROPERTY)
                .param(INCLUDE_RUNTIME, true)
                .build();

        dispatcher.execute(
                new Composite(hostOp, interfacesOp, jvmsOp, pathsOp, socketBindingGroupsOp, systemPropertiesOp),
                (CompositeResult result) -> {
                    getView().updateHost(new Host(result.step(0).get(RESULT)));
                    getView().updateInterfaces(asNamedNodes(result.step(1).get(RESULT).asPropertyList()));
                    getView().updateJvms(asNamedNodes(result.step(2).get(RESULT).asPropertyList()));
                    getView().updatePaths(asNamedNodes(result.step(3).get(RESULT).asPropertyList()));
                    getView().updateSocketBindingGroups(asNamedNodes(result.step(4).get(RESULT).asPropertyList()));
                    getView().updateSystemProperties(asNamedNodes(result.step(5).get(RESULT).asPropertyList()));
                });
    }
}
