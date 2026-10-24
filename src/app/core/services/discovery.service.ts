import { Injectable, inject } from '@angular/core';
import { Preferences } from '@capacitor/preferences';
import { Capacitor } from '@capacitor/core';
import { IluminusApiService } from './iluminus-api.service';

/**
 * DiscoveryService
 *
 * Handles discovering Iluminus ESP8266 devices on the local network.
 *
 * Protocol (from firmware iluminus.ino):
 *  - Firmware listens on UDP port 4210
 *  - We broadcast "DISCOVER_ILUMINUS" to 255.255.255.255:4210
 *  - Firmware replies with "ILUMINUS_ACK" from its IP
 *
 * On native (Android/iOS): uses capacitor-udp-socket
 * On web/browser: falls back to HTTP scan of common IPs (192.168.1.x)
 */
@Injectable({
  providedIn: 'root'
})
export class DiscoveryService {
  private readonly IP_KEY = 'iluminus_ip';
  private readonly UDP_PORT = 4210;
  private readonly DISCOVER_MSG = 'DISCOVER_ILUMINUS';
  private readonly ACK_MSG = 'ILUMINUS_ACK';
  private readonly DEFAULT_IP = '192.168.15.100';

  private readonly api = inject(IluminusApiService);

  // Vazio até a primeira leitura do Preferences. Antes começava preenchido,
  // e o IP salvo nunca era lido.
  private currentIp = '';

  async getIp(): Promise<string> {
    if (this.currentIp) return this.currentIp;
    const { value } = await Preferences.get({ key: this.IP_KEY });
    // Guard: reject obviously-wrong IPs (router gateways saved by mistake)
    const saved = value ?? '';
    this.currentIp = (saved && this.api.isDeviceIp(saved)) ? saved : this.DEFAULT_IP;
    return this.currentIp;
  }

  async setIp(ip: string): Promise<void> {
    this.currentIp = ip;
    await Preferences.set({ key: this.IP_KEY, value: ip });
  }

  async clearIp(): Promise<void> {
    this.currentIp = '';
    await Preferences.remove({ key: this.IP_KEY });
  }

  /**
   * Main discovery method. Returns a list of discovered device IPs.
   * Uses UDP broadcast on native, HTTP scan on web.
   */
  async scanNetwork(
    onFound: (ip: string) => void,
    onProgress?: (msg: string) => void
  ): Promise<string[]> {
    const isNative = Capacitor.isNativePlatform();

    if (isNative) {
      return this.scanViaUdp(onFound, onProgress);
    } else {
      return this.scanViaHttp(onFound, onProgress);
    }
  }

  // ─── NATIVE: UDP Broadcast ────────────────────────────────────────────────

  private async scanViaUdp(
    onFound: (ip: string) => void,
    onProgress?: (msg: string) => void
  ): Promise<string[]> {
    const found: string[] = [];

    try {
      // Dynamic import to avoid crash on web platform
      const { UdpSocket } = await import('capacitor-udp-socket');

      onProgress?.('Criando socket UDP...');
      const { socketId } = await UdpSocket.create();

      // Bind to a random local port
      await UdpSocket.bind({ socketId, port: 0 });

      // Enable broadcast
      await UdpSocket.setBroadcast({ socketId, enabled: true });

      onProgress?.('Enviando broadcast UDP DISCOVER_ILUMINUS...');

      // Listen for responses
      const listenerHandle = await UdpSocket.addListener('receive', (event) => {
        const reply = event.buffer ?? '';
        const ip = event.remoteAddress ?? '';
        if (reply.includes(this.ACK_MSG) && ip) {
          if (!found.includes(ip)) {
            found.push(ip);
            onFound(ip);
            onProgress?.(`Dispositivo encontrado: ${ip}`);
          }
        }
      });

      // Send broadcast
      await UdpSocket.send({
        socketId,
        address: '255.255.255.255',
        port: this.UDP_PORT,
        buffer: this.DISCOVER_MSG,
      });

      // Wait 4 seconds for responses
      await new Promise(r => setTimeout(r, 4000));

      // Cleanup
      listenerHandle.remove();
      await UdpSocket.close({ socketId });

    } catch (err) {
      console.error('UDP scan failed:', err);
      onProgress?.('UDP falhou, tentando scan HTTP...');
      return this.scanViaHttp(onFound, onProgress);
    }

    return found;
  }

  // ─── WEB FALLBACK: HTTP Ping Scan ────────────────────────────────────────

  private async scanViaHttp(
    onFound: (ip: string) => void,
    onProgress?: (msg: string) => void
  ): Promise<string[]> {
    const found: string[] = [];

    // Try saved IP first (but validate it's really an ESP8266)
    const savedIp = await this.getIp();
    if (this.api.isDeviceIp(savedIp)) {
      onProgress?.(`Verificando IP salvo: ${savedIp}...`);
      const ok = await this.pingIluminus(savedIp);
      if (ok) {
        found.push(savedIp);
        onFound(savedIp);
        onProgress?.(`Dispositivo encontrado: ${savedIp}`);
        return found;
      }
    }

    // Scan common subnets — 192.168.15 first (user's network)
    const subnets = ['192.168.15', '192.168.1', '192.168.0', '10.0.0'];
    // Skip .1 (gateway/router) and .254 (broadcast), scan .2 → .100
    const range = Array.from({ length: 99 }, (_, i) => i + 2);

    for (const subnet of subnets) {
      onProgress?.(`Varrendo sub-rede ${subnet}.0/24...`);

      // Scan in parallel batches of 15
      for (let i = 0; i < range.length; i += 15) {
        const batch = range.slice(i, i + 15);
        const results = await Promise.all(
          batch.map(async (octet) => {
            const ip = `${subnet}.${octet}`;
            const ok = await this.pingIluminus(ip, 900);
            return ok ? ip : null;
          })
        );

        for (const ip of results) {
          if (ip && !found.includes(ip)) {
            found.push(ip);
            onFound(ip);
            onProgress?.(`Dispositivo encontrado: ${ip}`);
          }
        }

        if (found.length > 0) return found; // Stop early if found
      }
    }

    if (found.length === 0) {
      onProgress?.('Nenhum dispositivo encontrado automaticamente.');
    }
    return found;
  }

  /**
   * Confirma que o IP é mesmo um iLuminus: o firmware responde 200 em /set?c=CHECK,
   * e um roteador ou outro aparelho não responde 200 nesse caminho.
   */
  private pingIluminus(ip: string, timeoutMs = 1200): Promise<boolean> {
    return this.api.checkConnection(ip, timeoutMs);
  }
}
