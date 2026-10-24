import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { PageShellComponent } from '../../shared/components/page-shell/page-shell.component';
import { LedStripComponent } from '../../shared/ui/led-strip.component';
import { DeviceStore } from '../../core/state/device.store';
import { EFFECTS, effectLabel } from '../../shared/models/effects';

type Filter = 'all' | 'color' | 'palette';

const FILTERS: { id: Filter; label: string }[] = [
  { id: 'all', label: 'Todos' },
  { id: 'color', label: 'Com sua cor' },
  { id: 'palette', label: 'Paletas' },
];

/** Aba Efeitos. */
@Component({
  selector: 'app-colors',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, PageShellComponent, LedStripComponent],
  template: `
<app-page-shell title="Efeitos" subtitle="Prévia dos 9 LEDs da sua fita">

  <div class="flex flex-col gap-4 md:gap-6">
    <section class="card md:rounded-[28px] md:p-6 flex flex-col gap-[14px]" aria-live="polite">
      <div class="flex justify-between items-center gap-3">
        <div class="flex flex-col gap-0.5 min-w-0">
          <span class="text-[12px] font-semibold text-muted">{{ store.power() ? 'Tocando agora' : 'Fita desligada' }}</span>
          <span class="text-[18px] font-extrabold truncate">{{ store.power() ? current() : 'Toque em um efeito para ligar' }}</span>
        </div>
        <a routerLink="/home" class="btn btn-secondary min-h-10 h-10 px-3.5 text-[13px] flex-shrink-0">
          <span class="block w-3.5 h-3.5 rounded-full" [style.background]="store.hex()"></span>
          Cor base
        </a>
      </div>
      <div class="rounded-[14px] bg-bg px-2.5 py-3.5 flex justify-center">
        <app-led-strip class="w-full justify-between max-w-md"
                       [effect]="store.power() ? store.effect() : 'D'" [color]="store.color()"
                       [size]="22" [glow]="store.power()" />
      </div>
    </section>

    <div class="flex gap-2 overflow-x-auto -mx-1 px-1" role="group" aria-label="Filtrar efeitos">
      @for (f of filters; track f.id) {
        <button type="button" class="chip" [attr.aria-pressed]="filter() === f.id" (click)="filter.set(f.id)">{{ f.label }}</button>
      }
    </div>

    <div class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-[10px]">
      @for (e of visible(); track e.cmd) {
        <button type="button" class="effect-card min-h-[88px] p-[14px]"
                [attr.aria-pressed]="store.power() && store.effect() === e.cmd"
                (click)="store.applyEffect(e.cmd)">
          <app-led-strip [effect]="e.cmd" [color]="store.color()" [size]="10" />
          <span class="flex flex-col gap-0.5">
            <span class="text-[14px] font-bold">{{ e.label }}</span>
            <span class="text-[12px] text-muted">{{ e.usesColor ? 'Usa sua cor' : 'Paleta própria' }}</span>
          </span>
        </button>
      }
    </div>
  </div>
</app-page-shell>
  `
})
export class ColorsComponent {
  protected readonly store = inject(DeviceStore);
  protected readonly filters = FILTERS;
  protected readonly filter = signal<Filter>('all');

  protected readonly visible = computed(() => {
    const f = this.filter();
    return EFFECTS.filter(e => f === 'all' || (f === 'color' ? e.usesColor : !e.usesColor));
  });
  protected readonly current = computed(() => effectLabel(this.store.effect()));
}
