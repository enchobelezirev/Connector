/*
 *  Copyright (c) 2023 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - initial API and implementation
 *
 */

package org.eclipse.edc.iam.identitytrust.issuer.configuration;

import com.fasterxml.jackson.core.type.TypeReference;
import org.eclipse.edc.iam.identitytrust.spi.TrustedIssuerRegistry;
import org.eclipse.edc.iam.verifiablecredentials.spi.model.Issuer;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.system.configuration.Config;
import org.eclipse.edc.spi.types.TypeManager;

import java.util.Map;

import static org.eclipse.edc.iam.identitytrust.issuer.configuration.TrustedIssuerConfigurationExtension.NAME;

/**
 * This IATP extension makes it possible to configure a list of trusted issuers, that will be matched against the Verifiable Credential issuers.
 */
@Extension(NAME)
public class TrustedIssuerConfigurationExtension implements ServiceExtension {

    public static final String CONFIG_PREFIX = "edc.iam.trusted-issuer";
    public static final String CONFIG_ALIAS = CONFIG_PREFIX + ".<issuerAlias>.";

    @Setting(context = CONFIG_ALIAS, value = "Additional properties of the issuer.")
    public static final String PROPERTIES_SUFFIX = "properties";
    @Setting(context = CONFIG_ALIAS, value = "ID of the issuer.", required = true)
    public static final String ID_SUFFIX = "id";

    protected static final String NAME = "Trusted Issuers Configuration Extensions";

    @Inject
    private TrustedIssuerRegistry trustedIssuerRegistry;
    @Inject
    private TypeManager typeManager;

    @Override
    public void initialize(ServiceExtensionContext context) {
        var config = context.getConfig(CONFIG_PREFIX);
        var issuers = config.partition().map(this::configureIssuer).toList();
        if (issuers.isEmpty()) {
            throw new EdcException("The list of trusted issuers is empty");
        }
        issuers.forEach(issuer -> trustedIssuerRegistry.addIssuer(issuer));
    }

    private Issuer configureIssuer(Config config) {

        var id = config.getString(ID_SUFFIX);
        var propertiesConfig = config.getString(PROPERTIES_SUFFIX, "{}");
        var properties = typeManager.readValue(propertiesConfig, new TypeReference<Map<String, Object>>() {
        });
        return new Issuer(id, properties);
    }
}
