# Discord VPN Manager — Android

Aplicativo para Android 8.0 ou mais recente. Mesmo nome e ícone do Manager do Windows; interface escura com detalhes roxos.

## Instalar e conectar

1. Transfira **DiscordVPNManager-Android.apk** para o celular e abra o arquivo. Se o Android solicitar, autorize a instalação por esse aplicativo de arquivos/navegador.
2. Instale o Discord oficial no mesmo usuário do Android.
3. Abra **Discord VPN Manager** e toque em **Importar perfil .conf**. Use um perfil WireGuard válido do seu provedor; o APK não contém perfis nem sua chave do PC.
4. Toque no botão roxo e aceite a autorização de VPN do Android. Permita as notificações para acessar o botão **Desconectar** fora do app.
5. Quando o túnel estiver ativo, toque em **Abrir Discord**. A confirmação do servidor aparece depois de uma troca de dados; túnel criado e servidor respondendo são estados diferentes.

## Iniciar com o Android

- Ative **Iniciar com o Android** dentro do Manager. O último perfil selecionado será usado após reiniciar e desbloquear o aparelho.
- Use **VPN sempre ativa → Configurar**. Nas configurações do Android, abra a engrenagem de **Discord VPN Manager** e ative **VPN sempre ativa** para o sistema manter/restaurar o serviço.
- **Deixe “Bloquear conexões sem VPN” desativado**, pois essa opção do Android pode cortar a internet dos outros aplicativos.
- Em **Execução em segundo plano → Ajustar**, libere a economia de bateria para o Manager. Em marcas que oferecem “Início automático”, permita-o para o Manager.
- A autorização inicial da VPN precisa ser feita uma vez pelo usuário. “Forçar parada” nas configurações do Android impede o início automático até você abrir o app novamente.
- O modo “VPN sempre ativa” é controlado pelo Android e pode manter a VPN ativa mesmo se a opção de inicialização do app estiver desligada. Desative-o nas configurações do Android para parar esse comportamento.

## Uso diário

O Manager limita o túnel ao pacote oficial `com.discord`. Voz, vídeo e chat do Discord seguem as rotas do perfil. Outros aplicativos usam a rede normal. Fechar a tela do Manager não desliga o túnel: use o botão de desconexão no app ou na notificação.

Somente uma VPN pode estar ativa por vez no Android. Para usar PC e celular simultaneamente, gere perfis/chaves diferentes no provedor, evitando que um aparelho derrube a sessão do outro.

Os perfis são criptografados com uma chave do Android Keystore e não entram em backups. Ao desinstalar o app, importe seus arquivos novamente após reinstalar.

## Validação feita

- APK de release assinado, instalado e aberto em emulador Android 15.
- Quatro testes automatizados da regra de aplicativos, todos aprovados.
- Teste no Android de criptografia dos perfis, criação e desligamento do túnel e restauração pelo receptor de inicialização.
- Reinicialização real do emulador: túnel restaurado automaticamente após iniciar o sistema.
- Verificação do Android: os identificadores atendidos pela VPN correspondem somente ao app de teste com pacote `com.discord` e seu sandbox.
- Verificação da assinatura e do alinhamento de 16 KB do APK concluída.

Os testes de túnel usaram chaves e servidor fictícios. A autenticação com seu provedor e uma chamada real do Discord ainda precisam ser validadas no celular. Não foi usado nem alterado o perfil privado do PC para esses testes.

## Referências

Motor: WireGuard Android, licença Apache 2.0: https://git.zx2c4.com/wireguard-android/
VPN por aplicativo e VPN sempre ativa: https://developer.android.com/develop/connectivity/vpn

App pessoal independente, sem vínculo com Discord ou Proton.
