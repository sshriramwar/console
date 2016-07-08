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
package org.jboss.hal.client.configuration.subsystem.mail;

import com.google.common.base.Joiner;
import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

/**
 * @author Claudio Miranda
 */
class MailSessionPreview extends PreviewContent<MailSession> {

    MailSessionPreview(final MailSession mailSession, final Resources resources) {
        super(mailSession.getName(), mailSession.getServers().isEmpty()
                ? resources.constants().noConfiguredMailServers()
                : resources.messages().configuredMailServer(Joiner.on(", ").join(mailSession.getServers())).asString());

        PreviewAttributes<MailSession> attributes = new PreviewAttributes<>(mailSession);
        attributes.append(ModelDescriptionConstants.JNDI_NAME);
        if (mailSession.hasServer(ModelDescriptionConstants.SMTP)) {
            attributes.append(model -> {
                return new String[]{ModelDescriptionConstants.SMTP.toUpperCase() + " " + Names.SOCKET_BINDING,
                        model.getServerSocketBinding(ModelDescriptionConstants.SMTP)};
            });
        }
        if (mailSession.hasServer(ModelDescriptionConstants.IMAP)) {
            attributes.append(model -> {
                return new String[]{ModelDescriptionConstants.IMAP.toUpperCase() + " " + Names.SOCKET_BINDING,
                        model.getServerSocketBinding(ModelDescriptionConstants.IMAP)};
            });
        }
        if (mailSession.hasServer(ModelDescriptionConstants.POP3)) {
            attributes.append(model -> {
                return new String[]{ModelDescriptionConstants.POP3.toUpperCase() + " " + Names.SOCKET_BINDING,
                        model.getServerSocketBinding(ModelDescriptionConstants.POP3)};
            });
        }
        attributes.end();
        previewBuilder().addAll(attributes);
    }
}
