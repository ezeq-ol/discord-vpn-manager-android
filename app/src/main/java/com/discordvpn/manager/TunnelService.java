package com.discordvpn.manager;
import android.app.*;
import android.content.*;
import android.net.VpnService;
import android.os.*;
import android.content.pm.ServiceInfo;
import com.wireguard.android.backend.*;
import com.wireguard.config.Config;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
import org.json.JSONObject;
public class TunnelService extends Service {
 private final ExecutorService executor=Executors.newSingleThreadExecutor();
 private final Handler ui=new Handler(Looper.getMainLooper()); private volatile boolean stopping;
 private final Runnable tick=new Runnable(){public void run(){
  if(ManagerApp.up){ executor.execute(()->{try{Statistics s=ManagerApp.engine(TunnelService.this).getStatistics(ManagerApp.tunnel);ManagerApp.rx=s.totalRx();ManagerApp.tx=s.totalTx();long h=0;for(var k:s.peers())h=Math.max(h,s.peer(k).latestHandshakeEpochMillis());ManagerApp.handshake=h;}catch(Exception ignored){}}); }
  else if(!ManagerApp.connecting && !stopping){ stopForeground(STOP_FOREGROUND_REMOVE);stopSelf();return; }
  ui.postDelayed(this,2500);
 }};
 public void onCreate(){super.onCreate();getSystemService(NotificationManager.class).createNotificationChannel(new NotificationChannel("tunnel","Conexão VPN",NotificationManager.IMPORTANCE_LOW));}
 private Notification notification(String text){
  PendingIntent open=PendingIntent.getActivity(this,0,new Intent(this,MainActivity.class),PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);
  PendingIntent stop=PendingIntent.getService(this,1,new Intent(this,TunnelService.class).setAction("STOP"),PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);
  return new Notification.Builder(this,"tunnel").setSmallIcon(R.drawable.ic_notification).setContentTitle("Discord VPN Manager").setContentText(text).setContentIntent(open).setOngoing(true).setCategory(Notification.CATEGORY_SERVICE).addAction(new Notification.Action.Builder(null,"Desconectar",stop).build()).build();
 }
 private void foreground(String text){if(Build.VERSION.SDK_INT>=34)startForeground(77,notification(text),ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);else startForeground(77,notification(text));}
 public int onStartCommand(Intent intent,int flags,int startId){
  String action=intent==null?"RESTORE":intent.getAction();
  if("STOP".equals(action)){stopping=true;foreground("Desconectando...");getSharedPreferences("settings",0).edit().putBoolean("wanted",false).apply();executor.execute(()->{try{ManagerApp.engine(this).setState(ManagerApp.tunnel,Tunnel.State.DOWN,null);}catch(Exception ignored){}finally{ManagerApp.up=false;ManagerApp.connecting=false;ManagerApp.message="VPN desconectada";ui.post(()->{stopForeground(STOP_FOREGROUND_REMOVE);stopSelf();});}});return START_NOT_STICKY;}
  if(intent==null&&!getSharedPreferences("settings",0).getBoolean("wanted",false)){stopSelf();return START_NOT_STICKY;}
  foreground("Preparando conexão exclusiva para o Discord");
  if(ManagerApp.up||ManagerApp.connecting)return START_STICKY;
  stopping=false;ManagerApp.connecting=true;ManagerApp.error="";ManagerApp.message="Conectando ao perfil...";ManagerApp.handshake=0;ManagerApp.rx=0;ManagerApp.tx=0;
  getSharedPreferences("settings",0).edit().putBoolean("wanted",true).apply();
  executor.execute(()->{
   try {
    if(VpnService.prepare(this)!=null)throw new Exception("Abra o app e autorize a conexão VPN.");
    try{getPackageManager().getPackageInfo("com.discord",0);}catch(Exception e){throw new Exception("Instale o Discord oficial antes de conectar.");}
    JSONObject profile=new Vault(this).selected();if(profile==null)throw new Exception("Importe um perfil .conf primeiro.");
    Config config=Config.parse(new ByteArrayInputStream(ProfilePolicy.discordOnly(profile.getString("config")).getBytes(StandardCharsets.UTF_8)));
    Exception last=null;
    for(int attempt=0;attempt<4&&!stopping;attempt++){try{ManagerApp.engine(this).setState(ManagerApp.tunnel,Tunnel.State.UP,config);last=null;break;}catch(Exception e){last=e;if(attempt<3)Thread.sleep(3000);}}
    if(stopping)return;
    if(last!=null)throw new Exception("Não foi possível iniciar o túnel. Confira o perfil e sua internet.");
    ManagerApp.activeName=profile.getString("name");ManagerApp.message="Somente o Discord usa esta VPN";
    ui.post(()->{getSystemService(NotificationManager.class).notify(77,notification("Túnel ativo • somente Discord"));ui.removeCallbacks(tick);ui.post(tick);});
   } catch(Exception e){ManagerApp.error=e instanceof com.wireguard.config.BadConfigException?"Perfil WireGuard inválido. Importe outro arquivo.":e.getMessage();ManagerApp.message="Falha ao conectar";getSharedPreferences("settings",0).edit().putBoolean("wanted",false).apply();ui.post(()->{stopForeground(STOP_FOREGROUND_REMOVE);stopSelf();});}
   finally{ManagerApp.connecting=false;}
  });return START_STICKY;
 }
 public IBinder onBind(Intent i){return null;}
 public void onDestroy(){ui.removeCallbacksAndMessages(null);executor.shutdown();super.onDestroy();}
}
