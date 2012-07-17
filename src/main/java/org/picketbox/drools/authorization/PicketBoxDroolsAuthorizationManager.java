/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.picketbox.drools.authorization;

import java.io.InputStream;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.picketbox.core.authorization.AuthorizationManager;
import org.picketbox.core.authorization.Resource;
import org.picketbox.core.PicketBoxSubject;
import org.picketbox.drools.PicketBoxDroolsMessages;

/**
 * An implementation of {@link AuthorizationManager} using Drools
 *
 * @author anil saldhana
 * @since Jul 12, 2012
 */
public class PicketBoxDroolsAuthorizationManager implements AuthorizationManager {

    protected boolean stopped = false, started = false;

    protected String droolsFile = "authorization.drl";
    private KnowledgeBuilder builder;

    protected KnowledgeBase knowledgeBase = null;

    public PicketBoxDroolsAuthorizationManager() {
        this.builder = KnowledgeBuilderFactory.newKnowledgeBuilder();
    }

    public String getDroolsFile() {
        return droolsFile;
    }

    public void setDroolsFile(String droolsFile) {
        this.droolsFile = droolsFile;
    }

    @Override
    public boolean authorize(Resource resource, PicketBoxSubject subject) {
        if (started == false) {
            throw PicketBoxDroolsMessages.MESSAGES.authorizationManagerNotStarted(getClass().getName());
        }
        StatefulKnowledgeSession session = knowledgeBase.newStatefulKnowledgeSession();

        // Insert the facts
        session.insert(resource);
        session.insert(subject.getUser());
        session.insert(subject);

        // Fire the rules. At the end, the resource.isAuthorized() call can tell us if the resource is authorized
        session.fireAllRules();

        // call dispose to release used resources
        session.dispose();

        return resource.isAuthorized();
    }

    @Override
    public boolean started() {
        return started;
    }

    @Override
    public void start() {
        if (knowledgeBase == null) {
            InputStream drl = getClass().getClassLoader().getResourceAsStream(droolsFile);
            if (drl == null)
                throw PicketBoxDroolsMessages.MESSAGES.drlNotAvailable(droolsFile);

            builder.add(ResourceFactory.newInputStreamResource(drl), ResourceType.DRL);
            if (builder.hasErrors()) {
                throw new RuntimeException(builder.getErrors().toString());
            }

            knowledgeBase = KnowledgeBaseFactory.newKnowledgeBase();
            // Add to Knowledge Base packages from the builder which are actually the rules from the drl file.
            knowledgeBase.addKnowledgePackages(builder.getKnowledgePackages());
        }
        this.started = true;
    }

    @Override
    public boolean stopped() {
        return stopped;
    }

    @Override
    public void stop() {
        knowledgeBase = null;
        this.builder = null;
    }
}