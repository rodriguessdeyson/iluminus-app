# iLuminus App

App para controlar a fita de LED **iLuminus** (ESP8266 + WS2812B) pela rede Wi-Fi local.
Um único código roda no **Android** (via Capacitor) e na **web**.

- Liga, desliga e ajusta cor, saturação e brilho em tempo real.
- 12 efeitos do firmware, com prévia de como cada um fica nos 9 LEDs da fita.
- Favoritos e histórico das cores usadas.
- Encontra a fita na rede (UDP no Android) ou conecta pelo IP.

## Stack

| Camada | Tecnologia |
|---|---|
| Framework | Angular 22 (standalone, signals, sem zone.js) |
| UI | Ionic 9 (`ion-tabs`) + Tailwind CSS 4 |
| Nativo | Capacitor 8 (Android) |
| Armazenamento | `@capacitor/preferences` |
| Descoberta | `capacitor-udp-socket` (UDP broadcast) |

## Como rodar

Pré-requisitos: Node.js 22+, e para o Android, Android Studio com SDK 36.

```bash
npm install
npm start            # web em http://localhost:4200
```

### Android

```bash
npm run build
npx cap sync android
npx cap open android # abre no Android Studio para rodar ou gerar o APK/AAB
```

O `appId` é `com.rad.iluminus`, o mesmo do app nativo anterior. Para publicar como
atualização na Play Store, assine com a mesma chave.

### Scripts

| Comando | O que faz |
|---|---|
| `npm start` | Servidor de desenvolvimento |
| `npm run build` | Build de produção em `www/` |
| `npm run lint` | ESLint (angular-eslint) |
| `npm test` | Testes unitários (Vitest) |

## Comunicação com a fita

O firmware é o projeto `iluminus-wireless-firmware` (Arduino, ESP8266).

| | |
|---|---|
| Comando | `GET http://<ip>/set?c=<cmd>&r=<0-255>&g=<0-255>&b=<0-255>` |
| Teste de conexão | `GET /set?c=CHECK` responde `200` |
| Descoberta | UDP broadcast `DISCOVER_ILUMINUS` na porta `4210`, resposta `ILUMINUS_ACK` |

Comandos: `L` cor sólida, `D` desligar, `R` arco-íris, `T` perseguição, `SP` faíscas,
`SS` pôr do sol, `OC` oceano, `FR` floresta, `N` neve, `C` scanner, `SL` dormir,
`O` pulsar, `FE` fogo. O brilho é aplicado no app, multiplicando o RGB.

**Android:** as requisições saem pela camada nativa (`CapacitorHttp`), porque o WebView
roda em `https://localhost` e bloquearia HTTP para o ESP. O tráfego HTTP na rede local é
liberado em `android/app/src/main/res/xml/network_security_config.xml`.

**Web:** usa `fetch` direto (o firmware envia `Access-Control-Allow-Origin: *`). Só
funciona com a página servida por HTTP; em HTTPS o navegador bloqueia as chamadas para a
rede local. Sem UDP no navegador, a busca testa os IPs mais comuns da rede.

## Estrutura

```
src/app/
  core/
    navigation.ts            itens das abas e da sidebar
    services/                HTTP com o ESP, descoberta, armazenamento
    state/device.store.ts    estado único: conexão, cor, brilho, efeito
  layout/shell/              abas embaixo (< 768px) e sidebar (≥ 768px)
  features/
    home/                    Luz: liga/desliga, cor, brilho, atalhos
    colors/                  Efeitos
    history/                 Favoritos: salvas e recentes
    devices/                 Dispositivos: fita atual, busca, IP manual
    splash/                  abertura e teste de conexão
  shared/
    components/page-shell/   cabeçalho flutuante + área rolável
    ui/                      ícones SVG e prévia dos LEDs
    models/                  efeitos, utilitários de cor, tipos
```

Pontos importantes:

- **Só o `DeviceStore` fala com o ESP.** O envio é "último valor vence": no máximo uma
  requisição em voo, e as mudanças feitas enquanto ela não volta viram um único envio.
  Arrastar um slider não enfileira dezenas de requisições no ESP8266.
- **Tema** em `src/styles.scss`: tokens no `@theme` do Tailwind (`bg-surface`,
  `text-muted`…) e classes de componente em `@layer components`. A cor atual da fita fica
  em `--app-led`. O CSS do Ionic fica na layer `ionic`, abaixo do Tailwind.
- **Safe area:** o Android 15+ desenha atrás das barras do sistema; as telas usam
  `--app-safe-*`, alimentadas pelo plugin `SystemBars` do Capacitor.
- **Offline:** fontes e ícones vêm empacotados no app, porque o celular pode estar no
  Wi-Fi do ESP sem internet.

## Pendências

- Ícone e splash nativos do Android ainda são os padrão do Capacitor.
- `electron/` foi gerado para o Capacitor 5 e não é compatível com o Capacitor 8.
- `src/app/app.component.spec.ts` precisa ser atualizado para componentes standalone.

## Licença

[MIT](LICENSE)
