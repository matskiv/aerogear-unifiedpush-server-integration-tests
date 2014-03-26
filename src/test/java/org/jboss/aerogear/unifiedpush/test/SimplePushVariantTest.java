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
package org.jboss.aerogear.unifiedpush.test;


import com.jayway.restassured.RestAssured;
import com.jayway.restassured.config.DecoderConfig;
import com.jayway.restassured.config.EncoderConfig;
import com.jayway.restassured.config.RestAssuredConfig;
import org.apache.http.HttpStatus;
import org.jboss.aerogear.test.Session;
import org.jboss.aerogear.test.api.ModelAsserts;
import org.jboss.aerogear.test.api.application.PushApplicationWorker;
import org.jboss.aerogear.test.api.simplepush.SimplePushVariantContext;
import org.jboss.aerogear.test.api.simplepush.SimplePushVariantWorker;
import org.jboss.aerogear.test.model.PushApplication;
import org.jboss.aerogear.test.model.SimplePushVariant;
import org.jboss.aerogear.unifiedpush.utils.CheckingExpectedException;
import org.jboss.aerogear.unifiedpush.utils.Constants;
import org.jboss.aerogear.unifiedpush.utils.ContentTypes;
import org.jboss.aerogear.unifiedpush.utils.SimplePushVariantUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.ArquillianRule;
import org.jboss.arquillian.junit.ArquillianRules;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(ArquillianRules.class)
public class SimplePushVariantTest {

    @ArquillianRule
    public static UnifiedPushServer ups = new UnifiedPushServer() {
        @Override
        protected UnifiedPushServer setup() {

            PushApplication application = with(PushApplicationWorker.worker()).generate().persist().detachEntity();

            return this;
        }
    };

    @Rule
    public CheckingExpectedException exception = new CheckingExpectedException() {
        @Override
        protected void afterExceptionAssert() {
            List<SimplePushVariant> variants = ups.with(SimplePushVariantWorker.worker(), getRegisteredApplication())
                    .findAll()
                    .detachEntities();

            assertThat(variants.size(), is(0));
        }
    };

    @BeforeClass
    public static void setup() {
        RestAssured.config = RestAssuredConfig.newConfig()
                .decoderConfig(DecoderConfig.decoderConfig().defaultContentCharset("UTF-8"))
                .encoderConfig(EncoderConfig.encoderConfig().defaultContentCharset("UTF-8"));

        RestAssured.keystore(Constants.KEYSTORE_PATH, Constants.KEYSTORE_PASSWORD);
    }

    @AfterClass
    public static void cleanup() {
        RestAssured.config = RestAssuredConfig.newConfig()
                .decoderConfig(DecoderConfig.decoderConfig().defaultContentCharset("ISO-8859-1"))
                .encoderConfig(EncoderConfig.encoderConfig().defaultContentCharset("ISO-8859-1"));
    }

    @Deployment(testable = false)
    @TargetsContainer("main-server-group")
    public static WebArchive createDeployment() {
        return Deployments.unifiedPushServer();
    }

    private PushApplication getRegisteredApplication() {
        return ups.with(PushApplicationWorker.worker()).findAll().detachEntity();
    }

    @Test
    public void registerWithoutAuthorization() {
        exception.expectUnexpectedResponseException(HttpStatus.SC_UNAUTHORIZED);

        Session invalidSession = Session.newSession(ups.getSession().getBaseUrl().toExternalForm());

        SimplePushVariantWorker.worker().createContext(invalidSession, getRegisteredApplication()).generate().persist();
    }

    @Test
    public void findVariantWithInvalidID() {
        exception.expectUnexpectedResponseException(HttpStatus.SC_NOT_FOUND);

        ups.with(SimplePushVariantWorker.worker(), getRegisteredApplication()).find(UUID.randomUUID().toString());
    }

    @Test
    public void updateVariantWithInvalidID() {
        SimplePushVariant variant = ups.with(SimplePushVariantWorker.worker(), getRegisteredApplication()).generate();
        variant.setVariantID(UUID.randomUUID().toString());

        exception.expectUnexpectedResponseException(HttpStatus.SC_NOT_FOUND);

        ups.with(SimplePushVariantWorker.worker(), getRegisteredApplication()).merge(variant);
    }

    @Test
    public void removeVariantWithInvalidID() {
        SimplePushVariant variant = ups.with(SimplePushVariantWorker.worker(), getRegisteredApplication()).generate();
        variant.setVariantID(UUID.randomUUID().toString());

        exception.expectUnexpectedResponseException(HttpStatus.SC_NOT_FOUND);
        ups.with(SimplePushVariantWorker.worker(), getRegisteredApplication()).remove(variant);
    }

    @Test
    public void testCRUD() {
        performCRUD(SimplePushVariantWorker.worker());
    }

    @Test
    public void testCRUDUTF8() {
        performCRUD(SimplePushVariantWorker.worker().contentType(ContentTypes.jsonUTF8()));
    }

    private void performCRUD(SimplePushVariantWorker worker) {
        PushApplication application = getRegisteredApplication();

        // CREATE
        List<SimplePushVariant> persistedVariants = ups.with(worker, application)
                .generate().name("AwesomeAppěščřžýáíéňľ").persist()
                .generate().name("AwesomeAppவான்வழிe").persist()
                .detachEntities();

        assertThat(persistedVariants, is(notNullValue()));
        assertThat(persistedVariants.size(), is(2));

        SimplePushVariant persistedVariant = persistedVariants.get(0);
        SimplePushVariant persistedVariant1 = persistedVariants.get(1);

        // READ
        SimplePushVariantContext context = ups.with(worker, application).findAll();
        List<SimplePushVariant> readVariants = context.detachEntities();
        assertThat(readVariants, is(notNullValue()));
        assertThat(readVariants.size(), is(2));

        ModelAsserts.assertModelsEqual(persistedVariant,
                context.detachEntity(persistedVariant.getVariantID()));
        ModelAsserts.assertModelsEqual(persistedVariant1,
                context.detachEntity(persistedVariant1.getVariantID()));

        // UPDATE
        ups.with(worker, application)
                .edit(persistedVariant.getVariantID()).name("newname").description("newdescription").merge();
        SimplePushVariant readVariant = ups.with(worker, application)
                .find(persistedVariant.getVariantID()).detachEntity();

        assertThat(readVariant.getName(), is("newname"));
        assertThat(readVariant.getDescription(), is("newdescription"));

        // DELETE
        readVariants = ups.with(worker, application)
                .removeById(persistedVariant.getVariantID())
                .removeById(persistedVariant1.getVariantID())
                .findAll()
                .detachEntities();
        assertThat(readVariants.size(), is(0));
    }



}
