package com.discordvpn.manager;
import android.app.Application;
import android.content.*;
import com.wireguard.android.backend.*;
public class ManagerApp extends Application {
 public static volatile GoBackend backend; public static volatile boolean up,connecting; public static volatile String message="Pronto para conectar",activeName="",error=""; public static volatile long rx,tx,handshake;
 public static final Tunnel tunnel=new Tunnel(){public String getName(){return "DiscordVPN";}public void onStateChange(State s){up=s==State.UP;}};
 public void onCreate(){super.onCreate();GoBackend.setAlwaysOnCallback(()->{
  try{startForegroundService(new Intent(this,TunnelService.class).setAction("ALWAYS_ON"));}catch(Exception e){error="Abra o app para retomar a VPN";}
 });}
 public static synchronized GoBackend engine(Context c){if(backend==null)backend=new GoBackend(c.getApplicationContext());return backend;}
}
