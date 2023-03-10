/*
 * Copyright (c) 2021-present KuFlow S.L.
 *
 * All rights reserved.
 */

package com.kuflow.engine.samples.worker;

import com.kuflow.spring.boot.autoconfigure.validation.NotFillMeAttributesValues;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties {

    @Valid
    private final TemporalProperties temporal = new TemporalProperties();

    public TemporalProperties getTemporal() {
        return this.temporal;
    }

    @NotFillMeAttributesValues
    public static final class TemporalProperties {

        @NotBlank
        private String namespace;

        @NotBlank
        private String kuflowQueue;

        @NotBlank
        private String target;

        private MutualTlsProperties mutualTls = new MutualTlsProperties();

        public MutualTlsProperties getMutualTls() {
            return this.mutualTls;
        }

        public String getNamespace() {
            return this.namespace;
        }

        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }

        public String getTarget() {
            return this.target;
        }

        public void setTarget(String target) {
            this.target = target;
        }

        public String getKuflowQueue() {
            return this.kuflowQueue;
        }

        public void setKuflowQueue(String kuflowQueue) {
            this.kuflowQueue = kuflowQueue;
        }

        @NotFillMeAttributesValues
        public static final class MutualTlsProperties {

            private String ca;

            private String caData;

            private String cert;

            private String certData;

            private String key;

            private String keyData;

            public String getCa() {
                return this.ca;
            }

            public void setCa(String ca) {
                this.ca = ca;
            }

            public String getCaData() {
                return this.caData;
            }

            public void setCaData(String caData) {
                this.caData = caData;
            }

            public String getCert() {
                return this.cert;
            }

            public void setCert(String cert) {
                this.cert = cert;
            }

            public String getCertData() {
                return this.certData;
            }

            public void setCertData(String certData) {
                this.certData = certData;
            }

            public String getKey() {
                return this.key;
            }

            public void setKey(String key) {
                this.key = key;
            }

            public String getKeyData() {
                return this.keyData;
            }

            public void setKeyData(String keyData) {
                this.keyData = keyData;
            }
        }
    }
}
