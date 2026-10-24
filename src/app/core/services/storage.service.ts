import { Injectable } from '@angular/core';
import { Preferences } from '@capacitor/preferences';
import { ColorFavorite, ColorHistory } from '../../shared/models/color.model';

@Injectable({
  providedIn: 'root'
})
export class StorageService {
  private readonly FAVORITES_KEY = 'iluminus_favorites';
  private readonly HISTORY_KEY = 'iluminus_history';

  constructor() {}

  async getFavorites(): Promise<ColorFavorite[]> {
    const { value } = await Preferences.get({ key: this.FAVORITES_KEY });
    return value ? JSON.parse(value) : [];
  }

  async saveFavorite(color: ColorFavorite): Promise<void> {
    const favorites = await this.getFavorites();
    favorites.push(color);
    await Preferences.set({
      key: this.FAVORITES_KEY,
      value: JSON.stringify(favorites)
    });
  }

  async removeFavorite(id: string): Promise<void> {
    let favorites = await this.getFavorites();
    favorites = favorites.filter(f => f.id !== id);
    await Preferences.set({
      key: this.FAVORITES_KEY,
      value: JSON.stringify(favorites)
    });
  }

  async getHistory(): Promise<ColorHistory[]> {
    const { value } = await Preferences.get({ key: this.HISTORY_KEY });
    return value ? JSON.parse(value) : [];
  }

  async addHistory(color: ColorHistory): Promise<void> {
    const history = await this.getHistory();
    // Keep max 50 items
    if (history.length >= 50) {
      history.shift();
    }
    history.push(color);
    await Preferences.set({
      key: this.HISTORY_KEY,
      value: JSON.stringify(history)
    });
  }

  async clearHistory(): Promise<void> {
    await Preferences.remove({ key: this.HISTORY_KEY });
  }
}
