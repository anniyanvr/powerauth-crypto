/*
 * PowerAuth Crypto Library
 * Copyright 2019 Wultra s.r.o.
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
package io.getlime.security.powerauth.crypto.lib.util;

import com.google.common.io.BaseEncoding;
import io.getlime.security.powerauth.crypto.lib.model.Argon2Hash;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Verify parsing of Argon2 hashes in Modular Crypt Format.
 */
public class Argon2Test {

    @Test
    public void testArgon2HashParser() throws IOException {
        // Version 16 test
        Argon2Hash hash16 = Argon2Hash.parse("$argon2i$m=65536,t=2,p=1$c29tZXNhbHQ$9sTbSlTio3Biev89thdrlKKiCaYsjjYVJxGAL3swxpQ");
        assertEquals("argon2i", hash16.getAlgorithm());
        assertNull(hash16.getVersion());
        assertEquals(65536, (int) hash16.getMemory());
        assertEquals(2, (int) hash16.getIterations());
        assertEquals(1, (int) hash16.getParallelism());
        assertArrayEquals(BaseEncoding.base64().decode("c29tZXNhbHQ"), hash16.getSalt());
        assertArrayEquals(BaseEncoding.base64().decode("9sTbSlTio3Biev89thdrlKKiCaYsjjYVJxGAL3swxpQ"), hash16.getDigest());
        // Version 19 test
        Argon2Hash hash19 = Argon2Hash.parse("$argon2i$v=19$m=65536,t=2,p=1$c29tZXNhbHQ$wWKIMhR9lyDFvRz9YTZweHKfbftvj+qf+YFY4NeBbtA");
        assertEquals("argon2i", hash19.getAlgorithm());
        assertEquals(19, (int) hash19.getVersion());
        assertEquals(65536, (int) hash19.getMemory());
        assertEquals(2, (int) hash19.getIterations());
        assertEquals(1, (int) hash19.getParallelism());
        assertArrayEquals(BaseEncoding.base64().decode("c29tZXNhbHQ"), hash19.getSalt());
        assertArrayEquals(BaseEncoding.base64().decode("wWKIMhR9lyDFvRz9YTZweHKfbftvj+qf+YFY4NeBbtA"), hash19.getDigest());
    }

    @Test(expected = IOException.class)
    public void testInvalidHash1() throws IOException {
        Argon2Hash.parse("argon2i$v=19$m=65536,t=2,p=1$c29tZXNhbHQ$wWKIMhR9lyDFvRz9YTZweHKfbftvj+qf+YFY4NeBbtA");
    }

    @Test(expected = IOException.class)
    public void testInvalidHash2() throws IOException {
        Argon2Hash.parse("$argon2i$v=19$c29tZXNhbHQ$wWKIMhR9lyDFvRz9YTZweHKfbftvj+qf+YFY4NeBbtA");
    }

    @Test(expected = IOException.class)
    public void testInvalidHash3() throws IOException {
        Argon2Hash.parse("argon2i$v=19$m=65536,t=2,p=1$$wWKIMhR9lyDFvRz9YTZweHKfbftvj+qf+YFY4NeBbtA");
    }

    @Test(expected = IOException.class)
    public void testInvalidHash4() throws IOException {
        Argon2Hash.parse("$argon2i$v=19$m=65536,t=2,p=1$c29tZXNhbHQ$");
    }

    @Test
    public void testArgon2HashGenerator() {
        Argon2Hash hash19 = new Argon2Hash("argon2i");
        hash19.setVersion(19);
        hash19.setMemory(32768);
        hash19.setParallelism(16);
        hash19.setIterations(5);
        hash19.setSalt(BaseEncoding.base64().decode("c29tZXNhbHQ"));
        hash19.setDigest(BaseEncoding.base64().decode("iekCn0Y3spW+sCcFanM2xBT63UP2sghkUoHLIUpWRS8"));
        assertEquals("$argon2i$v=19$m=32768,t=5,p=16$c29tZXNhbHQ$iekCn0Y3spW+sCcFanM2xBT63UP2sghkUoHLIUpWRS8", hash19.toString());
        Argon2Hash hash16 = new Argon2Hash("argon2id");
        hash16.setMemory(256);
        hash16.setParallelism(2);
        hash16.setIterations(2);
        hash16.setSalt(BaseEncoding.base64().decode("c29tZXNhbHQ"));
        hash16.setDigest(BaseEncoding.base64().decode("bQk8UB/VmZZF4Oo79iDXuL5/0ttZwg2f/5U52iv1cDc"));
        assertEquals("$argon2id$m=256,t=2,p=2$c29tZXNhbHQ$bQk8UB/VmZZF4Oo79iDXuL5/0ttZwg2f/5U52iv1cDc", hash16.toString());
    }
}
