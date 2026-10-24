export interface ColorFavorite {
  id: string;
  name: string;
  r: number;
  g: number;
  b: number;
  brightness: number;
  effect: string;
}

export interface ColorHistory {
  id: string;
  timestamp: number;
  r: number;
  g: number;
  b: number;
  brightness: number;
  effect: string;
}
