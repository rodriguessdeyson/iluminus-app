import { Injectable } from '@angular/core';
import { Capacitor, CapacitorHttp } from '@capacitor/core';

/**
 * Comunicação HTTP com o firmware do ESP8266 (GET /set?c=...).
 *
 * Android/iOS: as requisições saem pela camada nativa (CapacitorHttp).
 * O WebView roda em https://localhost, então um fetch para http://<ip>
 * seria bloqueado como "mixed content". Pela camada nativa não há esse
 * bloqueio nem CORS; o tráfego HTTP na rede local é liberado em
 * android/app/src/main/res/xml/network_security_config.xml.
 *
 * Web: fetch normal (o firmware responde com Access-Control-Allow-Origin: *).
 * Só funciona se a página for servida por HTTP, nunca por HTTPS.
 */
@Injectable({ providedIn: 'root' })
export class IluminusApiService {

  async sendCommand(
    ip: string,
    cmd: string,
    r: number = 0,
    g: number = 0,
    b: number = 0,
    e: number = 0
  ): Promise<boolean> {
    if (!this.isDeviceIp(ip)) return false;
    const url = `http://${ip}/set?c=${encodeURIComponent(cmd)}&r=${r}&g=${g}&b=${b}&e=${e}`;
    const ok = await this.get(url, 3000);
    if (!ok) console.warn(`[Iluminus] sendCommand failed (${cmd} → ${ip})`);
    return ok;
  }

  async checkConnection(ip: string, timeoutMs = 2500): Promise<boolean> {
    if (!this.isDeviceIp(ip)) return false;
    return this.get(`http://${ip}/set?c=CHECK`, timeoutMs);
  }

  /** Rejeita endereços que quase sempre são o roteador ou broadcast (.1, .254, .255). */
  isDeviceIp(ip: string): boolean {
    if (!ip) return false;
    const lastOctet = parseInt(ip.split('.').pop() ?? '0', 10);
    return !(lastOctet === 1 || lastOctet === 254 || lastOctet === 255);
  }

  private async get(url: string, timeoutMs: number): Promise<boolean> {
    if (Capacitor.isNativePlatform()) {
      try {
        const res = await CapacitorHttp.get({
          url,
          connectTimeout: timeoutMs,
          readTimeout: timeoutMs,
        });
        return res.status >= 200 && res.status < 300;
      } catch {
        return false;
      }
    }

    const controller = new AbortController();
    const timer = setTimeout(() => controller.abort(), timeoutMs);
    try {
      const res = await fetch(url, { signal: controller.signal, cache: 'no-store' });
      return res.ok;
    } catch {
      return false;
    } finally {
      clearTimeout(timer);
    }
  }
}
