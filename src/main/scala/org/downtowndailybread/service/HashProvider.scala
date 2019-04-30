package org.downtowndailybread.service

import java.security.{SecureRandom, SecureRandomParameters}
import java.util.Base64

import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

trait HashProvider {

  def hashPassword(
                  password: String,
                  salt: String
                  ): String = {
    val skf = SecretKeyFactory.getInstance( "PBKDF2WithHmacSHA512" )
    val spec = new PBEKeySpec(password.toCharArray, salt.toCharArray.map(_.toByte), 10000, 512)
    val key = skf.generateSecret(spec)
    val res = key.getEncoded
    Base64.getEncoder.encodeToString(res)
  }

  def generateSalt(): String = {
    val sr =  SecureRandom.getInstanceStrong
    val bytes = new Array[Byte](64)
    sr.nextBytes(bytes)
    Base64.getEncoder.encodeToString(bytes)
  }
}
