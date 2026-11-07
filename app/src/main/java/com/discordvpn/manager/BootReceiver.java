package com.discordvpn.manager;
import android.content.*;
import android.net.VpnService;
public class BootReceiver extends BroadcastReceiver {
 public void onReceive(Context c,Intent i){
  String a=i.getAction();if(!Intent.ACTION_BOOT_COMPLETED.equals(a)&&!Intent.ACTION_MY_PACKAGE_REPLACED.equals(a))return;
  if(c.getSharedPreferences("settings",0).getBoolean("auto",false)&&VpnService.prepare(c)==null){
   try{c.startForegroundService(new Intent(c,TunnelService.class).setAction("BOOT"));}catch(Exception ignored){}
  }
 }
}
