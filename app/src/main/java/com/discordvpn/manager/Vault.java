package com.discordvpn.manager;
import android.content.*;
import android.security.keystore.*;
import org.json.*;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import android.util.Base64;
public final class Vault {
 private static final String ALIAS="DiscordVPNProfilesV1";
 final Context ctx; public Vault(Context c){ctx=c;}
 private javax.crypto.SecretKey key() throws Exception {
  KeyStore ks=KeyStore.getInstance("AndroidKeyStore"); ks.load(null);
  if(!ks.containsAlias(ALIAS)) {
   KeyGenerator g=KeyGenerator.getInstance("AES","AndroidKeyStore");
   g.init(new KeyGenParameterSpec.Builder(ALIAS,KeyProperties.PURPOSE_ENCRYPT|KeyProperties.PURPOSE_DECRYPT).setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build());g.generateKey();
  }
  return (javax.crypto.SecretKey)ks.getKey(ALIAS,null);
 }
 public JSONArray read() throws Exception {
  String raw=ctx.getSharedPreferences("vault",0).getString("data",null);if(raw==null)return new JSONArray();
  String[] parts=raw.split(":"); Cipher c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.DECRYPT_MODE,key(),new GCMParameterSpec(128,Base64.decode(parts[0],0)));
  return new JSONArray(new String(c.doFinal(Base64.decode(parts[1],0)),StandardCharsets.UTF_8));
 }
 public void write(JSONArray profiles) throws Exception {
  Cipher c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.ENCRYPT_MODE,key());
  String raw=Base64.encodeToString(c.getIV(),Base64.NO_WRAP)+":"+Base64.encodeToString(c.doFinal(profiles.toString().getBytes(StandardCharsets.UTF_8)),Base64.NO_WRAP);
  if(!ctx.getSharedPreferences("vault",0).edit().putString("data",raw).commit())throw new Exception("Não foi possível salvar");
 }
 public JSONObject selected() throws Exception {
  JSONArray a=read(); String id=ctx.getSharedPreferences("settings",0).getString("selected","");
  for(int i=0;i<a.length();i++)if(a.getJSONObject(i).getString("id").equals(id))return a.getJSONObject(i);
  return a.length()>0?a.getJSONObject(0):null;
 }
}
