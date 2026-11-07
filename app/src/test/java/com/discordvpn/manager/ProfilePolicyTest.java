package com.discordvpn.manager;
import org.junit.Test;
import static org.junit.Assert.*;
public class ProfilePolicyTest {
 @Test public void overridesBothUserProvidedApplicationLists(){
  String input="[Interface]\r\nPrivateKey = secret\r\nIncludedApplications = com.browser, com.bank\r\nExcludedApplications = com.discord\r\n[Peer]\r\nAllowedIPs = 0.0.0.0/0, ::/0\r\nEndpoint = server:51820";
  String output=ProfilePolicy.discordOnly(input);
  assertTrue(output.contains("IncludedApplications = com.discord\n"));
  assertFalse(output.contains("com.browser"));assertFalse(output.contains("ExcludedApplications"));
  assertTrue(output.contains("AllowedIPs = 0.0.0.0/0, ::/0"));assertTrue(output.contains("PrivateKey = secret"));
 }
 @Test public void isIdempotent(){String a="[Interface]\nAddress = 10.0.0.1/32\n[Peer]\n";assertEquals(ProfilePolicy.discordOnly(a),ProfilePolicy.discordOnly(ProfilePolicy.discordOnly(a)));}
 @Test public void ignoresCaseAndWhitespace(){String a="[Interface]\n includedapplications  = com.other\n EXCLUDEDAPPLICATIONS= com.discord\n";String b=ProfilePolicy.discordOnly(a);assertFalse(b.contains("com.other"));assertFalse(b.contains("EXCLUDEDAPPLICATIONS"));}
 @Test(expected=IllegalArgumentException.class) public void requiresInterface(){ProfilePolicy.discordOnly("[Peer]\nEndpoint = example:51820");}
}
