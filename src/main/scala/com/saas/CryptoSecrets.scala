package com.saas

object CryptoSecrets {
  val symmetricEncryptionKey = "0123456789ABCDEF0123456789ABCDEF"
  val asymmetricPrivateKey = "-----BEGIN RSA PRIVATE KEY-----\nMIIEvQIBADANBgkqhkiG9w0BAQEFAAS\n-----END RSA PRIVATE KEY-----"
  val asymmetricPublicKey = "-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8A\n-----END PUBLIC KEY-----"
  val hmacSecret = "hmac_secret_key_for_signatures"
  val encryptionIV = "encryption_init_vector_1234"
  val encryptionSalt = "encryption_salt_value_xyz"
  val keyDerivationSalt = "kdf_salt_parameter"
  val tokenSigningKey = "token_signing_key_2024"
  val dataEncryptionKey = "data_encryption_key_secret"
  val masterEncryptionKey = "master_key_for_key_encryption"
}
