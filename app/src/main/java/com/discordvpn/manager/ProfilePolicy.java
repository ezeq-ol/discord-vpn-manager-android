package com.discordvpn.manager;
public final class ProfilePolicy {
 private ProfilePolicy() {}
 public static String discordOnly(String input) {
  StringBuilder out=new StringBuilder(); boolean found=false;
  for(String line:input.replace("\uFEFF", "").split("\r?\n")) {
   String t=line.trim();
   if(t.matches("(?i)^(IncludedApplications|ExcludedApplications)\\s*=.*")) continue;
   out.append(line).append('\n');
   if(t.equalsIgnoreCase("[Interface]")) { found=true; out.append("IncludedApplications = com.discord\n"); }
  }
  if(!found) throw new IllegalArgumentException("Perfil sem seção Interface");
  return out.toString();
 }
}
