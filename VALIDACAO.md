# Validação — 7 de novembro de 2026

Release: assembleRelease e lintRelease aprovados (0 erros; avisos não bloqueantes de estilo/recursos).
Unidade: ProfilePolicyTest — 4 testes, 0 falhas e 0 erros.
Instrumentação Android 15 API 35 x86_64: TunnelIntegrationTest — 1 teste aprovado, abrangendo criptografia, regra exclusiva, conectar, desconectar e entrada de boot.
Reboot real do emulador: serviço TunnelService em primeiro plano e túnel DiscordVPN UP sem abrir manualmente o Manager.
VPN do Android: OwnerUid 10149; Uids 10151 e sandbox 20151; pacote da fixture com.discord = uid 10151.
Persistência ao sair da tela: VPN e notificação mantidas após Home.
Release final: instalado e aberto no emulador, sem exceções AndroidRuntime.
Assinatura APK v2: verificada. Alinhamento ZIP/nativo de 16 KB: verificado.
Arquiteturas: arm64-v8a, armeabi-v7a, x86 e x86_64.

Limite: sem autenticação contra um provedor real ou chamada de voz real. Nenhum perfil privado do PC foi incluído ou usado.
