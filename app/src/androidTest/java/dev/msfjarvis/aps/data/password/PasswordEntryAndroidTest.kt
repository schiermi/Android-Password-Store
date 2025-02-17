/*
 * Copyright © 2014-2021 The Android Password Store Authors. All Rights Reserved.
 * SPDX-License-Identifier: GPL-3.0-only
 */

package dev.msfjarvis.aps.data.password

import com.github.michaelbull.result.get
import dev.msfjarvis.aps.util.totp.Otp
import dev.msfjarvis.aps.util.totp.UriTotpFinder
import java.util.Date
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.Test

class PasswordEntryAndroidTest {

  private fun makeEntry(content: String) = PasswordEntry(content, UriTotpFinder())

  @Test
  fun testGetPassword() {
    assertEquals("fooooo", makeEntry("fooooo\nbla\n").password)
    assertEquals("fooooo", makeEntry("fooooo\nbla").password)
    assertEquals("fooooo", makeEntry("fooooo\n").password)
    assertEquals("fooooo", makeEntry("fooooo").password)
    assertEquals("", makeEntry("\nblubb\n").password)
    assertEquals("", makeEntry("\nblubb").password)
    assertEquals("", makeEntry("\n").password)
    assertEquals("", makeEntry("").password)
  }

  @Test
  fun testGetExtraContent() {
    assertEquals("bla\n", makeEntry("fooooo\nbla\n").extraContent)
    assertEquals("bla", makeEntry("fooooo\nbla").extraContent)
    assertEquals("", makeEntry("fooooo\n").extraContent)
    assertEquals("", makeEntry("fooooo").extraContent)
    assertEquals("blubb\n", makeEntry("\nblubb\n").extraContent)
    assertEquals("blubb", makeEntry("\nblubb").extraContent)
    assertEquals("", makeEntry("\n").extraContent)
    assertEquals("", makeEntry("").extraContent)
  }

  @Test
  fun testGetUsername() {
    for (field in PasswordEntry.USERNAME_FIELDS) {
      assertEquals("username", makeEntry("\n$field username").username)
      assertEquals("username", makeEntry("\n${field.toUpperCase()} username").username)
    }
    assertEquals("username", makeEntry("secret\nextra\nlogin: username\ncontent\n").username)
    assertEquals("username", makeEntry("\nextra\nusername: username\ncontent\n").username)
    assertEquals("username", makeEntry("\nUSERNaMe:  username\ncontent\n").username)
    assertEquals("username", makeEntry("\nlogin:    username").username)
    assertEquals("foo@example.com", makeEntry("\nemail: foo@example.com").username)
    assertEquals("username", makeEntry("\nidentity: username\nlogin: another_username").username)
    assertEquals("username", makeEntry("\nLOGiN:username").username)
    assertNull(makeEntry("secret\nextra\ncontent\n").username)
  }

  @Test
  fun testHasUsername() {
    assertTrue(makeEntry("secret\nextra\nlogin: username\ncontent\n").hasUsername())
    assertFalse(makeEntry("secret\nextra\ncontent\n").hasUsername())
    assertFalse(makeEntry("secret\nlogin failed\n").hasUsername())
    assertFalse(makeEntry("\n").hasUsername())
    assertFalse(makeEntry("").hasUsername())
  }

  @Test
  fun testGeneratesOtpFromTotpUri() {
    val entry = makeEntry("secret\nextra\n$TOTP_URI")
    assertTrue(entry.hasTotp())
    val code =
      Otp.calculateCode(
          entry.totpSecret!!,
          // The hardcoded date value allows this test to stay reproducible.
          Date(8640000).time / (1000 * entry.totpPeriod),
          entry.totpAlgorithm,
          entry.digits
        )
        .get()
    assertNotNull(code) { "Generated OTP cannot be null" }
    assertEquals(entry.digits.toInt(), code.length)
    assertEquals("545293", code)
  }

  @Test
  fun testGeneratesOtpWithOnlyUriInFile() {
    val entry = makeEntry(TOTP_URI)
    assertTrue(entry.password.isEmpty())
    assertTrue(entry.hasTotp())
    val code =
      Otp.calculateCode(
          entry.totpSecret!!,
          // The hardcoded date value allows this test to stay reproducible.
          Date(8640000).time / (1000 * entry.totpPeriod),
          entry.totpAlgorithm,
          entry.digits
        )
        .get()
    assertNotNull(code) { "Generated OTP cannot be null" }
    assertEquals(entry.digits.toInt(), code.length)
    assertEquals("545293", code)
  }

  @Test
  fun testOnlyLooksForUriInFirstLine() {
    val entry = makeEntry("id:\n$TOTP_URI")
    assertTrue(entry.password.isNotEmpty())
    assertTrue(entry.hasTotp())
    assertFalse(entry.hasUsername())
  }

  companion object {

    const val TOTP_URI =
      "otpauth://totp/ACME%20Co:john@example.com?secret=HXDMVJECJJWSRB3HWIZR4IFUGFTMXBOZ&issuer=ACME%20Co&algorithm=SHA1&digits=6&period=30"
  }
}
