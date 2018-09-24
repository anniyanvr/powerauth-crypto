/*
 * Copyright 2018 Wultra s.r.o.
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
package io.getlime.security.powerauth.crypto.lib.encryptor.ecies.model;


import java.nio.charset.StandardCharsets;

/**
 * Enumeration with constants for parameter sharedInfo1.
 */
public enum SharedInfo1Constants {

    ACTIVATION_LAYER_2("/pa/activation"),
    MIGRATION_START("/pa/migration"),
    VAULT_UNLOCK("/pa/vault/unlock"),
    CREATE_TOKEN("/pa/token/create");

    private byte[] value;

    /**
     * Constructor with sharedInfo1 parameter for ECIES.
     * @param sharedInfo1 Parameter sharedInfo1 for ECIES.
     */
    SharedInfo1Constants(String sharedInfo1) {
        this.value = sharedInfo1.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Get byte value of sharedInfo1 parameter for ECIES.
     * @return Byte value of sharedInfo1 parameter for ECIES.
     */
    public byte[] value() {
        return value;
    }

}
