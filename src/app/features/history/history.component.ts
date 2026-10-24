import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { PageShellComponent } from '../../shared/components/page-shell/page-shell.component';
import { IconComponent } from '../../shared/ui/icon.component';
import { StorageService } from '../../core/services/storage.service';
import { DeviceStore } from '../../core/state/device.store';
import { ColorFavorite, ColorHistory } from '../../shared/models/color.model';
import { effectLabel } from '../../shared/models/effects';
import { rgbCss, scaleBrightness, toHex } from '../../shared/models/color.utils';

type View = 'saved' | 'recent';

/** Aba Favoritos: cores salvas e histórico recente. */
@Component({
  selector: 'app-history',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [PageShellComponent, IconComponent],
  template: `
<app-page-shell title="Favoritos">

  <div class="flex flex-col gap-4">
    <div class="segmented max-w-sm" role="tablist" aria-label="Ver">
      <button type="button" role="tab" [attr.aria-selected]="view() === 'saved'" (click)="view.set('saved')">Salvas</button>
      <button type="button" role="tab" [attr.aria-selected]="view() === 'recent'" (click)="view.set('recent')">Recentes</button>
    </div>

    @if (view() === 'saved') {
      <div class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-[10px]">
        @for (f of favorites(); track f.id) {
          <div class="rounded-[20px] bg-surface p-2.5 flex flex-col gap-2.5">
            <button type="button" class="h-[84px] w-full rounded-[14px] border-0 p-2.5 flex items-end cursor-pointer"
                    [style.background]="swatch(f)" [attr.aria-label]="'Aplicar ' + f.name"
                    (click)="store.applyPreset(f)">
              <span class="font-mono text-[11px] font-semibold px-1.5 py-0.5 rounded-md bg-bg/75 text-ink">{{ hex(f) }}</span>
            </button>
            <div class="flex items-center gap-1 pl-1">
              <div class="flex-1 min-w-0">
                <p class="m-0 text-[14px] font-bold truncate">{{ f.name }}</p>
                <p class="m-0 text-[12px] text-muted truncate">{{ label(f.effect) }} · {{ f.brightness }}%</p>
              </div>
              <button type="button" class="w-11 h-11 grid place-items-center rounded-xl border-0 bg-transparent text-muted cursor-pointer hover:bg-surface-2"
                      [attr.aria-label]="'Remover ' + f.name" (click)="remove(f)">
                <app-icon name="trash" [size]="18" />
              </button>
            </div>
          </div>
        }
        <button type="button"
                class="min-h-[160px] rounded-[20px] border-[1.5px] border-dashed border-white/15 bg-transparent text-soft flex flex-col items-center justify-center gap-2 text-[14px] font-bold cursor-pointer hover:bg-surface"
                (click)="saveCurrent()">
          <app-icon [name]="justSaved() ? 'check' : 'plus'" [size]="22" [stroke]="2" />
          {{ justSaved() ? 'Salva' : 'Salvar cor atual' }}
        </button>
      </div>
    } @else {
      @if (history().length === 0) {
        <div class="card p-8 flex flex-col items-center gap-2 text-center">
          <p class="m-0 text-[14px] font-bold">Nenhum histórico ainda</p>
          <p class="m-0 text-[13px] text-muted">As cores e efeitos que você usar aparecem aqui.</p>
        </div>
      } @else {
        <div class="flex flex-col gap-2 max-w-2xl">
          @for (entry of history(); track entry.id) {
            <div class="flex items-center gap-3 rounded-2xl bg-surface py-2.5 pr-2.5 pl-3">
              <span class="w-10 h-10 rounded-xl flex-shrink-0" [style.background]="swatch(entry)"></span>
              <div class="flex-1 min-w-0">
                <p class="m-0 font-mono text-[13px] font-semibold">{{ hex(entry) }}</p>
                <p class="m-0 text-[12px] text-muted truncate">{{ label(entry.effect) }} · {{ when(entry.timestamp) }}</p>
              </div>
              <button type="button" class="btn btn-secondary px-3.5 text-[13px]" (click)="store.applyPreset(entry)">Aplicar</button>
            </div>
          }
          <button type="button" class="btn btn-text text-danger mt-1 self-center" (click)="clearHistory()">Limpar histórico</button>
        </div>
      }
    }
  </div>
</app-page-shell>
  `
})
export class HistoryComponent {
  protected readonly store = inject(DeviceStore);
  private readonly storage = inject(StorageService);

  protected readonly view = signal<View>('saved');
  protected readonly favorites = signal<ColorFavorite[]>([]);
  protected readonly history = signal<ColorHistory[]>([]);
  protected readonly justSaved = signal(false);

  /** As abas mantêm a tela viva; recarrega toda vez que ela aparece. */
  async ionViewWillEnter() {
    const [favs, hist] = await Promise.all([this.storage.getFavorites(), this.storage.getHistory()]);
    this.favorites.set(favs.reverse());
    this.history.set(hist.reverse()); // mais recentes primeiro
  }

  protected label(cmd: string) { return effectLabel(cmd); }

  protected swatch(p: ColorFavorite | ColorHistory) {
    return rgbCss(scaleBrightness(p, Math.max(p.brightness, 25)));
  }

  protected hex(p: ColorFavorite | ColorHistory) {
    return toHex(scaleBrightness(p, p.brightness));
  }

  protected when(ts: number) {
    const min = Math.round((Date.now() - ts) / 60000);
    if (min < 1) return 'agora';
    if (min < 60) return `há ${min} min`;
    const h = Math.round(min / 60);
    if (h < 24) return `há ${h} h`;
    return new Date(ts).toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit' });
  }

  async saveCurrent() {
    const fav = await this.store.saveFavorite();
    this.favorites.update(list => [fav, ...list]);
    this.justSaved.set(true);
    setTimeout(() => this.justSaved.set(false), 1500);
  }

  async remove(f: ColorFavorite) {
    await this.storage.removeFavorite(f.id);
    this.favorites.update(list => list.filter(x => x.id !== f.id));
  }

  async clearHistory() {
    await this.storage.clearHistory();
    this.history.set([]);
  }
}
