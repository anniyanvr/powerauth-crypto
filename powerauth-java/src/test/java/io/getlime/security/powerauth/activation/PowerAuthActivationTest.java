/**
 * Copyright 2015 Lime - HighTech Solutions s.r.o.
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
package io.getlime.security.powerauth.activation;

import com.google.common.io.BaseEncoding;
import io.getlime.security.powerauth.client.activation.PowerAuthClientActivation;
import io.getlime.security.powerauth.lib.config.PowerAuthConfiguration;
import io.getlime.security.powerauth.lib.generator.KeyGenerator;
import io.getlime.security.powerauth.lib.provider.CryptoProviderUtil;
import io.getlime.security.powerauth.lib.provider.impl.CryptoProviderUtilBouncyCastle;
import io.getlime.security.powerauth.server.activation.PowerAuthServerActivation;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;

import javax.crypto.SecretKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author petrdvorak
 */
public class PowerAuthActivationTest {

	@BeforeClass
	public static void setUpClass() {
	}

	@AfterClass
	public static void tearDownClass() {
	}

	@Before
	public void setUp() {
		// Add Bouncy Castle Security Provider
		Security.addProvider(new BouncyCastleProvider());
		PowerAuthConfiguration.INSTANCE.setKeyConvertor(new CryptoProviderUtilBouncyCastle());
	}

	@After
	public void tearDown() {
	}

	@Test
	public void testGenerateKeys() throws Exception {
		CryptoProviderUtil keyConvertor = PowerAuthConfiguration.INSTANCE.getKeyConvertor();
		KeyGenerator keyGenerator = new KeyGenerator();
		KeyPair kp = keyGenerator.generateKeyPair();
		System.out.println("Private Key: " + BaseEncoding.base64().encode(keyConvertor.convertPrivateKeyToBytes(kp.getPrivate())));
		System.out.println("Public Key: " + BaseEncoding.base64().encode(keyConvertor.convertPublicKeyToBytes(kp.getPublic())));
	}

	/**
	 * Test of the complete activation process, orchestration between client and server.
	 *
	 * @throws java.lang.Exception
	 */
	@Test
	public void testActivationProcess() throws Exception {

		System.out.println("TEST: Activation Process");

		// Prepare test data
		KeyGenerator keyGenerator = new KeyGenerator();
		PowerAuthClientActivation clientActivation = new PowerAuthClientActivation();
		PowerAuthServerActivation serverActivation = new PowerAuthServerActivation();
		KeyPair masterKeyPair = keyGenerator.generateKeyPair();

		// Generate master keypair
		PrivateKey masterPrivateKey = masterKeyPair.getPrivate();
		PublicKey masterPublicKey = masterKeyPair.getPublic();

		for (int i = 0; i < 20; i++) {

			// SERVER: Generate data for activation
			@SuppressWarnings("unused")
			String activationId = serverActivation.generateActivationId();
			String activationIdShort = serverActivation.generateActivationIdShort();
			String activationOTP = serverActivation.generateActivationOTP();
			byte[] activationSignature = serverActivation.generateActivationSignature(activationIdShort, activationOTP, masterPrivateKey);
			KeyPair serverKeyPair = serverActivation.generateServerKeyPair();
			PrivateKey serverPrivateKey = serverKeyPair.getPrivate();
			PublicKey serverPublicKey = serverKeyPair.getPublic();

			// CLIENT: Verify activation signature
			boolean activationSignatureOK = clientActivation.verifyActivationDataSignature(activationIdShort, activationOTP, activationSignature, masterPublicKey);
			assertTrue(activationSignatureOK);

			// CLIENT: Generate and send public key
			KeyPair deviceKeyPair = clientActivation.generateDeviceKeyPair();
			PrivateKey devicePrivateKey = deviceKeyPair.getPrivate();
			PublicKey devicePublicKey = deviceKeyPair.getPublic();
			byte[] clientNonce = clientActivation.generateActivationNonce();
			byte[] c_devicePublicKey = clientActivation.encryptDevicePublicKey(devicePublicKey, activationOTP, activationIdShort, clientNonce);

			// SERVER: Decrypt device public key
			PublicKey decryptedDevicePublicKey = serverActivation.decryptDevicePublicKey(c_devicePublicKey, activationIdShort, activationOTP, clientNonce);
			assertEquals(devicePublicKey, decryptedDevicePublicKey);

			// SERVER: Encrypt and send encrypted server public and it's signature
			KeyPair ephemeralKeyPair = keyGenerator.generateKeyPair();
			PrivateKey ephemeralPrivateKey = ephemeralKeyPair.getPrivate();
			PublicKey ephemeralPublicKey = ephemeralKeyPair.getPublic();
			byte[] serverNonce = serverActivation.generateActivationNonce();
			byte[] c_serverPublicKey = serverActivation.encryptServerPublicKey(serverPublicKey, devicePublicKey, ephemeralPrivateKey, activationOTP, activationIdShort, serverNonce);
			byte[] c_serverPublicKeySignature = serverActivation.computeServerPublicKeySignature(c_serverPublicKey, masterPrivateKey);

			// CLIENT: Validate server public key signature and decrypt server public key
			boolean serverPublicKeySignatureOK = clientActivation.verifyServerPublicKeySignature(c_serverPublicKey, c_serverPublicKeySignature, masterPublicKey);
			assertTrue(serverPublicKeySignatureOK);

			PublicKey decryptedServerPublicKey = clientActivation.decryptServerPublicKey(c_serverPublicKey, devicePrivateKey, ephemeralPublicKey, activationOTP, activationIdShort, serverNonce);
			assertEquals(serverPublicKey, decryptedServerPublicKey);

			// CLIENT and SERVER: Compute device public key fingerprint
			int devicePublicKeyFingerprintClient = clientActivation.computeDevicePublicKeyFingerprint(devicePublicKey);
			int devicePublicKeyFingerprintServer = serverActivation.computeDevicePublicKeyFingerprint(decryptedDevicePublicKey);
			assertEquals(devicePublicKeyFingerprintClient, devicePublicKeyFingerprintServer);

			// CLIENT and SERVER: Compute shared master secret
			SecretKey sharedMasterSecretDevice = keyGenerator.computeSharedKey(devicePrivateKey, serverPublicKey);
			SecretKey sharedMasterSecretServer = keyGenerator.computeSharedKey(serverPrivateKey, devicePublicKey);
			assertEquals(sharedMasterSecretDevice, sharedMasterSecretServer);

		}
	}

	@Test
	public void testActivationGenerate() throws Exception {
		String activationOTP = "CKZ2O-OE544";
		String activationIdShort = "IFA6F-3NPAZ";
		byte[] activationNonce = BaseEncoding.base64().decode("grDwkvXrgfUdKBsqg0xYYw==");
		byte[] publicKeyBytes = BaseEncoding.base64().decode("BJXfJMCANX+T9FzsG6Hi0KTYPN64i7HxMiWoMYPd17DYfBR+IwzOesTh/jj/B3trL9m3O1oODYil+8ssJzDt/QA=");

		PublicKey publicKey = PowerAuthConfiguration.INSTANCE.getKeyConvertor().convertBytesToPublicKey(publicKeyBytes);
		PowerAuthClientActivation activation = new PowerAuthClientActivation();

		byte[] cDevicePublicKey = activation.encryptDevicePublicKey(publicKey, activationOTP, activationIdShort, activationNonce);
		assertArrayEquals(cDevicePublicKey, BaseEncoding.base64().decode("el8ZgYcenmCPsu2eITa4T/cmTwFxKbHRotmEW0veOyi81RSBPCy4n/WZTOFvvHdIM/IUwUyBTI8+xjKcQ9g14RUuwvnxwqbH3DoMEDWKAx8="));
		
	}

}
