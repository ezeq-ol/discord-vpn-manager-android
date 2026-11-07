package com.discordvpn.manager;
import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.*;
import android.graphics.drawable.*;
import android.net.*;
import android.os.*;
import android.provider.*;
import android.view.*;
import android.widget.*;
import org.json.*;
import com.wireguard.config.Config;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class MainActivity extends Activity {
 private static final int BG=0xff101019,CARD=0xff1c1c2b,WHITE=0xfff3f0ff,MUTED=0xffa5a2b8,PURPLE=0xff9560ff,GREEN=0xff61dfb1;
 private LinearLayout root; private TextView badge,status,subtitle,profileName,endpoint,rx,tx,handshake,activity;
 private PowerButton power; private Button open;private Switch startup;private boolean binding;private Vault vault;
 private final Handler handler=new Handler(Looper.getMainLooper());
 private final Runnable refresh=new Runnable(){public void run(){render();handler.postDelayed(this,1000);}};
 int d(float n){return Math.round(n*getResources().getDisplayMetrics().density);}
 TextView text(String s,int size,int color){TextView v=new TextView(this);v.setText(s);v.setTextSize(size);v.setTextColor(color);v.setFontFeatureSettings("kern");return v;}
 void bold(TextView v){v.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));}
 GradientDrawable shape(int color,int radius){GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(d(radius));return g;}
 LinearLayout col(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);return l;}
 LinearLayout row(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.HORIZONTAL);l.setGravity(Gravity.CENTER_VERTICAL);return l;}
 LinearLayout.LayoutParams lp(int w,int h){return new LinearLayout.LayoutParams(w<0?w:d(w),h<0?h:d(h));}
 void gap(LinearLayout l,int h){View v=new View(this);l.addView(v,lp(1,h));}
 LinearLayout card(){LinearLayout l=col();l.setPadding(d(20),d(20),d(20),d(20));l.setBackground(shape(CARD,24));return l;}
 Button button(String s,boolean accent){Button b=new Button(this);b.setText(s);b.setAllCaps(false);b.setTextColor(WHITE);b.setTextSize(15);bold(b);b.setMinHeight(d(50));b.setPadding(d(14),0,d(14),0);b.setBackground(shape(accent?0xff8247ef:0xff2c2b42,15));return b;}
 TextView section(String s){TextView v=text(s,11,MUTED);v.setLetterSpacing(.15f);bold(v);return v;}
 @Override public void onCreate(Bundle b){super.onCreate(b);vault=new Vault(this);build();}
 private void build(){
  ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setClipToPadding(false);scroll.setBackgroundColor(BG);setContentView(scroll);
  root=col();root.setPadding(d(22),d(24),d(22),d(26));scroll.addView(root,new ScrollView.LayoutParams(-1,-2));
  LinearLayout header=row();ImageView logo=new ImageView(this);logo.setImageResource(R.drawable.manager_logo);logo.setContentDescription("Ícone Discord VPN Manager");header.addView(logo,lp(48,48));
  LinearLayout title=col();LinearLayout.LayoutParams tp=lp(0,-2);tp.weight=1;tp.leftMargin=d(12);header.addView(title,tp);
  TextView brand=text("Discord VPN",24,WHITE);bold(brand);title.addView(brand);TextView tag=text("MANAGER  /  ANDROID",10,MUTED);tag.setLetterSpacing(.16f);title.addView(tag);
  TextView help=text("?",18,WHITE);help.setGravity(Gravity.CENTER);help.setBackground(shape(0xff29263b,22));help.setContentDescription("Ajuda e informações");header.addView(help,lp(38,38));help.setOnClickListener(v->help());root.addView(header);gap(root,26);
  LinearLayout hero=card();GradientDrawable glow=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{0xff2e204b,0xff1b1b2c});glow.setCornerRadius(d(28));hero.setBackground(glow);hero.setGravity(Gravity.CENTER_HORIZONTAL);
  badge=text("●  DESCONECTADO",10,0xffc0aedf);badge.setLetterSpacing(.13f);badge.setPadding(d(13),d(8),d(13),d(8));badge.setBackground(shape(0xff38294e,20));hero.addView(badge);gap(hero,16);
  power=new PowerButton();hero.addView(power,lp(146,146));power.setOnClickListener(v->{if(ManagerApp.connecting)return;if(ManagerApp.up)disconnect();else requestConnect();});gap(hero,12);
  status=text("Seu Discord. Sua conexão.",23,WHITE);bold(status);status.setGravity(Gravity.CENTER);hero.addView(status);gap(hero,7);
  subtitle=text("Ative a VPN só para o Discord.\nO restante continua na sua rede.",13,0xffbbb2d0);subtitle.setGravity(Gravity.CENTER);subtitle.setLineSpacing(d(2),1);hero.addView(subtitle);gap(hero,20);
  LinearLayout stats=row();stats.setGravity(Gravity.CENTER);rx=metric(stats,"RECEBIDO");tx=metric(stats,"ENVIADO");handshake=metric(stats,"SERVIDOR");hero.addView(stats,lp(-1,-2));root.addView(hero);gap(root,23);
  LinearLayout profileTitle=row();TextView ptitle=section("SEU PERFIL");profileTitle.addView(ptitle,new LinearLayout.LayoutParams(0,-2,1));TextView manage=text("Gerenciar  ↗",12,0xffbca0ff);manage.setPadding(d(8),d(8),0,d(8));manage.setOnClickListener(v->profiles());profileTitle.addView(manage);root.addView(profileTitle);gap(root,8);
  LinearLayout profile=card();LinearLayout pr=row();TextView symbol=text("⇄",24,0xffb99bff);symbol.setGravity(Gravity.CENTER);symbol.setBackground(shape(0xff302343,13));pr.addView(symbol,lp(44,44));LinearLayout pn=col();LinearLayout.LayoutParams nlp=lp(0,-2);nlp.weight=1;nlp.leftMargin=d(13);pr.addView(pn,nlp);profileName=text("Importe seu primeiro perfil",15,WHITE);bold(profileName);profileName.setSingleLine(true);profileName.setEllipsize(android.text.TextUtils.TruncateAt.END);pn.addView(profileName);endpoint=text("Arquivo WireGuard .conf",11,MUTED);endpoint.setSingleLine(true);pn.addView(endpoint);pr.addView(text("›",26,MUTED));profile.addView(pr);profile.setOnClickListener(v->profiles());gap(profile,17);Button imp=button("＋  Importar perfil .conf",false);imp.setOnClickListener(v->importProfile());profile.addView(imp,lp(-1,48));root.addView(profile);gap(root,14);
  open=button("Abrir Discord  ↗",true);open.setOnClickListener(v->openDiscord());root.addView(open,lp(-1,52));gap(root,25);
  root.addView(section("DO SEU JEITO"));gap(root,12);LinearLayout settings=card();LinearLayout ar=row();LinearLayout al=col();ar.addView(al,new LinearLayout.LayoutParams(0,-2,1));TextView at=text("Iniciar com o Android",15,WHITE);bold(at);al.addView(at);TextView ad=text("Conectar com o último perfil ao reiniciar",11,MUTED);al.addView(ad);startup=new Switch(this);startup.setContentDescription("Iniciar VPN com o Android");ar.addView(startup);settings.addView(ar);startup.setOnCheckedChangeListener((b,on)->{
   if(binding)return;try{if(on&&vault.selected()==null){toast("Importe um perfil primeiro.");binding=true;startup.setChecked(false);binding=false;return;}}catch(Exception e){toast("Não foi possível ler os perfis.");return;}
   getSharedPreferences("settings",0).edit().putBoolean("auto",on).apply();
   if(on){new AlertDialog.Builder(this).setTitle("Pronto para iniciar com o Android").setMessage("Autorize a VPN uma vez. Para o Android manter a conexão após reiniciar, ative também VPN sempre ativa nas configurações. Não ative Bloquear conexões sem VPN: essa opção afeta os outros apps.").setPositiveButton("Entendi",(x,y)->requestConnect()).setNegativeButton("Depois",null).show();}
  });gap(settings,17);View divider=new View(this);divider.setBackgroundColor(0xff343145);settings.addView(divider,lp(-1,1));gap(settings,14);
  TextView vpnSettings=text("VPN sempre ativa                   Configurar  ↗",13,0xffbca0ff);vpnSettings.setPadding(0,d(7),0,d(7));vpnSettings.setOnClickListener(v->settingsVpn());settings.addView(vpnSettings);
  TextView battery=text("Execução em segundo plano       Ajustar  ↗",13,0xffbca0ff);battery.setPadding(0,d(9),0,d(7));battery.setOnClickListener(v->{try{startActivity(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));}catch(Exception e){toast("Abra as configurações de bateria do Android.");}});settings.addView(battery);root.addView(settings);gap(root,18);
  activity=text("Importe um perfil para começar.",12,MUTED);activity.setLineSpacing(d(3),1);root.addView(activity);gap(root,18);
  TextView footer=text("●  SÓ O DISCORD USA A VPN\nSeus outros apps seguem na conexão normal.",11,0xff858097);footer.setGravity(Gravity.CENTER);footer.setLineSpacing(d(4),1);root.addView(footer);
 }
 TextView metric(LinearLayout parent,String label){LinearLayout c=col();c.setGravity(Gravity.CENTER);parent.addView(c,new LinearLayout.LayoutParams(0,-2,1));TextView value=text("—",15,WHITE);bold(value);c.addView(value);gap(c,5);TextView cap=text(label,8,0xffa79bbd);cap.setLetterSpacing(.08f);c.addView(cap);return value;}
 private String size(long n){if(n<1024)return n+" B";if(n<1048576)return String.format(Locale.US,"%.1f KB",n/1024.0);return String.format(Locale.US,"%.1f MB",n/1048576.0);}
 private void render(){boolean up=ManagerApp.up,busy=ManagerApp.connecting;power.active=up;power.busy=busy;power.setContentDescription(up?"Desconectar VPN":busy?"Conectando VPN":"Conectar VPN");power.invalidate();badge.setText(busy?"●  CONECTANDO":up?"●  TÚNEL ATIVO":"●  DESCONECTADO");badge.setTextColor(up?GREEN:0xffc0aedf);status.setText(busy?"Preparando sua conexão":up?"Discord pela VPN":"Seu Discord. Sua conexão.");subtitle.setText(busy?"Estabelecendo o túnel com seu perfil.":up?"Voz, vídeo e chat no mesmo túnel.\nSó o Discord. Do seu jeito.":"Toque no botão para conectar.\nO restante continua na sua rede.");rx.setText(up?size(ManagerApp.rx):"—");tx.setText(up?size(ManagerApp.tx):"—");handshake.setText(!up?"—":ManagerApp.handshake>0?"OK":"Aguardando");open.setEnabled(up&&!busy);open.setAlpha(up&&!busy?1f:.45f);
  try{JSONObject selected=vault.selected();profileName.setText(selected==null?"Importe seu primeiro perfil":selected.getString("name"));endpoint.setText(selected==null?"Arquivo WireGuard .conf":endpointOf(selected.getString("config")));}catch(Exception e){profileName.setText("Não foi possível ler o perfil");endpoint.setText("Importe o arquivo novamente");}
  binding=true;startup.setChecked(getSharedPreferences("settings",0).getBoolean("auto",false));binding=false;
  activity.setText(!ManagerApp.error.isEmpty()?ManagerApp.error:busy?"Conectando ao servidor do perfil...":up?(ManagerApp.handshake>0?"Conexão confirmada com o servidor.":"Túnel ativo. A confirmação do servidor aparece quando houver troca de dados."):"O app pode ser fechado. A VPN ativa continua na notificação.");activity.setTextColor(ManagerApp.error.isEmpty()?MUTED:0xffffa3ad);
 }
 private String endpointOf(String conf){for(String line:conf.split("\n"))if(line.trim().toLowerCase(Locale.ROOT).startsWith("endpoint")){String[] p=line.split("=",2);if(p.length==2)return p[1].trim();}return "Perfil WireGuard";}
 @Override protected void onResume(){super.onResume();handler.removeCallbacks(refresh);handler.post(refresh);}
 @Override protected void onPause(){handler.removeCallbacks(refresh);super.onPause();}
 private boolean idle(){if(ManagerApp.up||ManagerApp.connecting){toast("Desconecte a VPN antes de alterar o perfil.");return false;}return true;}
 private void importProfile(){if(!idle())return;Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("*/*").addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,10);}
 private void profiles(){try{JSONArray a=vault.read();if(a.length()==0){importProfile();return;}String[] names=new String[a.length()];int selected=0;JSONObject current=vault.selected();for(int i=0;i<a.length();i++){names[i]=a.getJSONObject(i).getString("name");if(current!=null&&current.getString("id").equals(a.getJSONObject(i).getString("id")))selected=i;}
  new AlertDialog.Builder(this).setTitle("Seus perfis").setSingleChoiceItems(names,selected,(dialog,which)->{if(!idle())return;try{getSharedPreferences("settings",0).edit().putString("selected",a.getJSONObject(which).getString("id")).apply();render();dialog.dismiss();}catch(Exception e){toast("Não foi possível selecionar.");}}).setPositiveButton("Importar",(x,y)->importProfile()).setNeutralButton("Excluir selecionado",(x,y)->removeProfile()).setNegativeButton("Fechar",null).show();
 }catch(Exception e){toast("Não foi possível ler os perfis. Importe novamente.");}}
 private void removeProfile(){if(!idle())return;try{JSONObject current=vault.selected();if(current==null)return;new AlertDialog.Builder(this).setTitle("Excluir este perfil?").setMessage(current.getString("name")).setNegativeButton("Cancelar",null).setPositiveButton("Excluir",(x,y)->{try{JSONArray a=vault.read(),next=new JSONArray();for(int i=0;i<a.length();i++)if(!a.getJSONObject(i).getString("id").equals(current.getString("id")))next.put(a.getJSONObject(i));vault.write(next);if(next.length()==0)getSharedPreferences("settings",0).edit().putBoolean("auto",false).apply();render();}catch(Exception e){toast("Não foi possível excluir.");}}).show();}catch(Exception e){toast("Não foi possível ler o perfil.");}}
 private void requestConnect(){try{if(vault.selected()==null){importProfile();return;}}catch(Exception e){toast("Importe um perfil válido.");return;}try{getPackageManager().getPackageInfo("com.discord",0);}catch(Exception e){new AlertDialog.Builder(this).setTitle("Instale o Discord").setMessage("Este app conecta somente o Discord oficial. Instale-o antes de ativar a VPN.").setPositiveButton("OK",null).show();return;}
  if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},30);return;}authorize();}
 private void authorize(){Intent p=android.net.VpnService.prepare(this);if(p!=null)startActivityForResult(p,20);else connect();}
 @Override public void onRequestPermissionsResult(int r,String[] p,int[] g){super.onRequestPermissionsResult(r,p,g);if(r==30)authorize();}
 private void connect(){try{startForegroundService(new Intent(this,TunnelService.class).setAction("CONNECT"));}catch(Exception e){toast("Não foi possível iniciar. Abra o app e tente novamente.");}}
 private void disconnect(){startService(new Intent(this,TunnelService.class).setAction("STOP"));}
 private void openDiscord(){Intent i=getPackageManager().getLaunchIntentForPackage("com.discord");if(i!=null)startActivity(i);else toast("O Discord não está instalado.");}
 private void settingsVpn(){new AlertDialog.Builder(this).setTitle("VPN sempre ativa").setMessage("Na lista do Android, toque na engrenagem de Discord VPN Manager e ative VPN sempre ativa.\n\nDeixe Bloquear conexões sem VPN desativado para preservar a conexão dos outros aplicativos.").setPositiveButton("Abrir configurações",(x,y)->{try{startActivity(new Intent(Settings.ACTION_VPN_SETTINGS));}catch(Exception e){toast("Abra VPN nas configurações do Android.");}}).setNegativeButton("Depois",null).show();}
 private void help(){new AlertDialog.Builder(this).setTitle("Sua conexão, sob controle").setMessage("1. Importe um perfil WireGuard .conf.\n2. Toque no botão e autorize a VPN.\n3. Abra o Discord. Voz, vídeo e chat usam o túnel.\n\nPara conectar após reiniciar, ative Iniciar com o Android e configure VPN sempre ativa. Alguns celulares também exigem liberar a bateria e o início automático.\n\nSó uma VPN pode ficar ativa por vez. O Discord deve estar no mesmo usuário/perfil do Android. Use um perfil diferente no celular e no PC se conectar os dois ao mesmo tempo.\n\nPerfis são criptografados no aparelho. Nenhum perfil vem incluído.\n\nMotor WireGuard © WireGuard LLC · Apache 2.0. App independente, sem vínculo com Discord ou Proton.").setPositiveButton("Entendi",null).show();}
 private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
 @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(request==20){if(result==RESULT_OK)connect();else toast("A VPN precisa da sua autorização para conectar.");return;}if(request!=10||result!=RESULT_OK||data==null||data.getData()==null)return;
  try{Uri uri=data.getData();String name="Novo perfil";try(Cursor c=getContentResolver().query(uri,null,null,null,null)){if(c!=null&&c.moveToFirst()){int idx=c.getColumnIndex(OpenableColumns.DISPLAY_NAME);if(idx>=0)name=c.getString(idx);}}
   byte[] bytes;try(InputStream in=getContentResolver().openInputStream(uri);ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] chunk=new byte[4096];int n;while((n=in.read(chunk))!=-1){out.write(chunk,0,n);if(out.size()>262144)throw new IOException("Arquivo grande demais");}bytes=out.toByteArray();}
   String conf=ProfilePolicy.discordOnly(new String(bytes,StandardCharsets.UTF_8));Config parsed=Config.parse(new ByteArrayInputStream(conf.getBytes(StandardCharsets.UTF_8)));if(parsed.getPeers().isEmpty()||parsed.getPeers().stream().anyMatch(peer->peer.getEndpoint().isEmpty()))throw new IOException("Sem servidor");
   if(!parsed.getInterface().getIncludedApplications().equals(Collections.singleton("com.discord")))throw new IOException("Regra de aplicativos inválida");
   name=name.replaceFirst("(?i)\\.conf$","");JSONArray a=vault.read();if(a.length()>=30)throw new IOException("Máximo de 30 perfis");JSONObject obj=new JSONObject().put("id",UUID.randomUUID().toString()).put("name",name).put("config",conf);a.put(obj);vault.write(a);getSharedPreferences("settings",0).edit().putString("selected",obj.getString("id")).apply();ManagerApp.error="";toast("Perfil importado. Pronto para conectar.");render();
  }catch(Exception e){new AlertDialog.Builder(this).setTitle("Não foi possível importar").setMessage("Selecione um perfil WireGuard .conf válido, com Interface, chave e servidor. O arquivo deve ter até 256 KB.").setPositiveButton("OK",null).show();}
 }
 private final class PowerButton extends View {
  boolean active,busy;Paint paint=new Paint(3);PowerButton(){super(MainActivity.this);setLayerType(View.LAYER_TYPE_SOFTWARE,null);setFocusable(true);setClickable(true);}
  protected void onDraw(Canvas canvas){float cx=getWidth()/2f,cy=getHeight()/2f,r=Math.min(cx,cy);paint.setStyle(Paint.Style.FILL);paint.setColor(active?0xff294e47:0xff46305f);canvas.drawCircle(cx,cy,r-d(1),paint);paint.setColor(active?GREEN:PURPLE);paint.setAlpha(32);canvas.drawCircle(cx,cy,r-d(8),paint);paint.setAlpha(255);paint.setShader(new LinearGradient(0,0,getWidth(),getHeight(),active?new int[]{0xff438b75,0xff235d50}:new int[]{0xffa36aff,0xff6d35db},null,Shader.TileMode.CLAMP));canvas.drawCircle(cx,cy,r-d(17),paint);paint.setShader(null);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(d(3.3f));paint.setStrokeCap(Paint.Cap.ROUND);paint.setColor(WHITE);float radius=d(19);canvas.drawArc(cx-radius,cy-radius,cx+radius,cy+radius,-48,276,false,paint);canvas.drawLine(cx,cy-d(27),cx,cy-d(5),paint);paint.setStyle(Paint.Style.FILL);}
 }
}
