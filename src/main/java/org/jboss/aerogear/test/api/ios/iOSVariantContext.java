/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.test.api.ios;

import org.jboss.aerogear.test.Session;
import org.jboss.aerogear.test.api.AbstractUPSContext;
import org.jboss.aerogear.test.model.PushApplication;
import org.jboss.aerogear.test.model.iOSVariant;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

public class iOSVariantContext extends AbstractUPSContext<iOSVariant, String, iOSVariantBlueprint,
        iOSVariantEditor, PushApplication, iOSVariantWorker, iOSVariantContext> {

    public iOSVariantContext(iOSVariantWorker worker, PushApplication parent, Session session) {
        super(worker, parent, session);
    }

    @Override
    public iOSVariantBlueprint create() {
        return new iOSVariantBlueprint(this);
    }

    @Override
    public iOSVariantBlueprint generate() {
        return create()
                .name(randomString())
                .description(randomString())
                .certificate(getWorker().getDefaultCertificate())
                .passphrase(getWorker().getDefaultPassphrase());
    }

    @Override
    protected iOSVariantContext castInstance() {
        return this;
    }

    @Override
    public String getEntityID(iOSVariant variant) {
        return variant.getVariantID();
    }

    public iOSVariantContext mergePatch(iOSVariant variant) {
        return mergePatch(Collections.singleton(variant));
    }

    public iOSVariantContext mergePatch(Collection<? extends iOSVariant> entities) {
        getWorker().updatePatch(this, entities);
        return this;
    }
}
