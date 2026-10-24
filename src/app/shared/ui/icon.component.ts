import { ChangeDetectionStrategy, Component, input } from '@angular/core';

export type IconName =
  | 'light' | 'sparkle' | 'heart' | 'devices'
  | 'power' | 'swap' | 'plus' | 'trash' | 'refresh' | 'radar'
  | 'back' | 'close' | 'check' | 'wifi-off' | 'edit' | 'chevron';

/**
 * Ícones de traço em SVG inline: sem fonte de ícones, funcionam offline
 * e herdam a cor do texto (currentColor).
 */
@Component({
  selector: 'app-icon',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { 'aria-hidden': 'true', class: 'app-icon' },
  styles: [`:host { display: inline-flex; line-height: 0; flex-shrink: 0; }`],
  template: `
    <svg [attr.width]="size()" [attr.height]="size()" viewBox="0 0 24 24" fill="none"
         stroke="currentColor" [attr.stroke-width]="stroke()" stroke-linecap="round" stroke-linejoin="round">
      @switch (name()) {
        @case ('light') {
          <svg:path d="M9 18h6M10 21h4"/>
          <svg:path d="M12 3a6 6 0 0 0-3.5 10.9V15h7v-1.1A6 6 0 0 0 12 3z"/>
        }
        @case ('sparkle') {
          <svg:path d="M12 3l1.9 5.1L19 10l-5.1 1.9L12 17l-1.9-5.1L5 10l5.1-1.9z"/>
          <svg:path d="M19 16l.7 1.8 1.8.7-1.8.7L19 21l-.7-1.8-1.8-.7 1.8-.7z"/>
        }
        @case ('heart') {
          <svg:path d="M12 20s-7.5-4.6-7.5-10.2A4.3 4.3 0 0 1 12 7.3a4.3 4.3 0 0 1 7.5 2.5C19.5 15.4 12 20 12 20z"/>
        }
        @case ('devices') {
          <svg:rect x="3" y="13" width="18" height="7" rx="2"/>
          <svg:path d="M7 16.5h.01M11 16.5h.01"/>
          <svg:path d="M8.5 9.5a5 5 0 0 1 7 0M5.5 6.5a9.5 9.5 0 0 1 13 0"/>
        }
        @case ('power') {
          <svg:path d="M12 3v8"/>
          <svg:path d="M6.3 6.8a7.5 7.5 0 1 0 11.4 0"/>
        }
        @case ('swap') { <svg:path d="M7 7h11l-3-3M17 17H6l3 3"/> }
        @case ('plus') { <svg:path d="M12 5v14M5 12h14"/> }
        @case ('trash') {
          <svg:path d="M4 7h16M10 11v6M14 11v6"/>
          <svg:path d="M6 7l1 13h10l1-13M9 7V4h6v3"/>
        }
        @case ('refresh') {
          <svg:path d="M20 11a8 8 0 1 0-2.3 5.7"/>
          <svg:path d="M20 4v7h-7"/>
        }
        @case ('radar') {
          <svg:circle cx="12" cy="12" r="2"/>
          <svg:path d="M16.2 7.8a6 6 0 0 1 0 8.4M7.8 16.2a6 6 0 0 1 0-8.4"/>
          <svg:path d="M19 5a10 10 0 0 1 0 14M5 19A10 10 0 0 1 5 5"/>
        }
        @case ('back') { <svg:path d="M19 12H5M11 6l-6 6 6 6"/> }
        @case ('close') { <svg:path d="M6 6l12 12M18 6L6 18"/> }
        @case ('check') { <svg:path d="M5 12.5l4.5 4.5L19 7"/> }
        @case ('wifi-off') {
          <svg:path d="M3 3l18 18"/>
          <svg:path d="M8.5 16.5a5 5 0 0 1 7 0M5 13a10 10 0 0 1 5.2-2.8M19 13a10 10 0 0 0-2.4-1.8M2 9.5a15 15 0 0 1 4.7-3M22 9.5A15 15 0 0 0 11 5.6"/>
          <svg:path d="M12 20h.01"/>
        }
        @case ('edit') {
          <svg:path d="M4 20h4L19 9l-4-4L4 16z"/>
          <svg:path d="M13.5 6.5l4 4"/>
        }
        @case ('chevron') { <svg:path d="M9 6l6 6-6 6"/> }
      }
    </svg>
  `,
})
export class IconComponent {
  readonly name = input.required<IconName>();
  readonly size = input(22);
  readonly stroke = input(1.9);
}
