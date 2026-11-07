package com.discordvpn.manager;
import android.content.*;
import android.net.*;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.*;
import org.junit.runner.RunWith;
import org.json.*;
import java.util.*;
import com.wireguard.config.Config;
import com.wireguard.crypto.KeyPair;
import java.io.*;
import java.nio.charset.StandardCharsets;
import static org.junit.Assert.*;
@RunWith(AndroidJUnit4.class)
public class TunnelIntegrationTest {
 @Test public void encryptedStorageAndDiscordOnlyTunnel() throws Exception {
  Context c=InstrumentationRegistry.getInstrumentation().getTargetContext();
  assertNull("VPN consent must be granted for emulator test",VpnService.prepare(c));
  String secret=new KeyPair().getPrivateKey().toBase64();
  String config="[Interface]\nPrivateKey = "+secret+"\nAddress = 10.200.0.2/32\nDNS = 1.1.1.1\nIncludedApplications = com.anything\n[Peer]\nPublicKey = "+new KeyPair().getPublicKey().toBase64()+"\nEndpoint = 192.0.2.1:51820\nAllowedIPs = 0.0.0.0/0\nPersistentKeepalive = 25\n";
  String only=ProfilePolicy.discordOnly(config);
  Config parsed=Config.parse(new ByteArrayInputStream(only.getBytes(StandardCharsets.UTF_8)));
  assertEquals(Collections.singleton("com.discord"),parsed.getInterface().getIncludedApplications());
  assertTrue(parsed.getInterface().getExcludedApplications().isEmpty());
  JSONObject profile=new JSONObject().put("id","test").put("name","Perfil de teste").put("config",only);
  Vault vault=new Vault(c);vault.write(new JSONArray().put(profile));c.getSharedPreferences("settings",0).edit().putString("selected","test").putBoolean("auto",true).commit();
  assertEquals(only,vault.selected().getString("config"));
  String stored=c.getSharedPreferences("vault",0).getString("data","");assertFalse(stored.contains(secret));assertFalse(stored.contains("PrivateKey"));
  c.startForegroundService(new Intent(c,TunnelService.class).setAction("CONNECT"));
  for(int n=0;n<150&&!ManagerApp.up;n++)Thread.sleep(100);
  assertTrue("Tunnel failed: "+ManagerApp.error,ManagerApp.up);
  c.startService(new Intent(c,TunnelService.class).setAction("STOP"));
  for(int n=0;n<150&&(ManagerApp.up||ManagerApp.connecting);n++)Thread.sleep(100);
  assertFalse(ManagerApp.up);Thread.sleep(500);
  // Exercise the same entry point used by a reboot with the user toggle enabled.
  new BootReceiver().onReceive(c,new Intent(Intent.ACTION_BOOT_COMPLETED));
  for(int n=0;n<150&&!ManagerApp.up;n++)Thread.sleep(100);
  assertTrue("Boot restore failed: "+ManagerApp.error,ManagerApp.up);
 }
}
