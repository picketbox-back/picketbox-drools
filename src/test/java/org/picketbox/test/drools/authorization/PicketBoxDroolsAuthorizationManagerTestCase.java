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
package org.picketbox.test.drools.authorization;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.security.Principal;

import org.junit.Test;
import org.picketbox.PicketBoxPrincipal;
import org.picketbox.authorization.Resource;
import org.picketbox.core.PicketBoxSubject;
import org.picketbox.drools.authorization.PicketBoxDroolsAuthorizationManager;

/**
 * Unit test the {@link PicketBoxDroolsAuthorizationManager}
 * @author anil saldhana
 * @since Jul 12, 2012
 */
public class PicketBoxDroolsAuthorizationManagerTestCase {

    @Test
    public void testAuthorize() throws Exception {
        PicketBoxDroolsAuthorizationManager am = new PicketBoxDroolsAuthorizationManager();
        am.start();
        
        Principal principal = new PicketBoxPrincipal("anil");
        
        PicketBoxSubject subject = new PicketBoxSubject();
        subject.setUser(principal);
        
        Resource resource = new Resource(){
            private static final long serialVersionUID = 1L;
            boolean aut;
            
            @Override
            public boolean isAuthorized() {
                return aut;
            }

            @Override
            public void setAuthorized(boolean authorize) {
                aut = authorize;
            }   
        };
        assertTrue(am.authorize(resource, subject));
        
        resource.setAuthorized(false);
        
        principal = new PicketBoxPrincipal("Bad Man");
        subject = new PicketBoxSubject();
        subject.setUser(principal);
        assertFalse(am.authorize(resource, subject));
    }
}