import {Injectable} from '@angular/core';

@Injectable({providedIn: 'root'})
export class ThemeService {
  private readonly darkClass = 'dark';
  private readonly storageKey = 'theme';

  constructor() {
    this.initTheme();
  }

  initTheme(): void {
    const saved = localStorage.getItem(this.storageKey);

    if (saved === 'dark') {
      this.enableDarkMode();
    } else if (saved === 'light') {
      this.disableDarkMode();
    } else {
      // No saved preference: fallback to system preference
      const prefersDark = window.matchMedia(
        '(prefers-color-scheme: dark)',
      ).matches;
      prefersDark ? this.enableDarkMode() : this.disableDarkMode();
    }
  }

  toggleDarkMode(): void {
    const isDark = document.documentElement.classList.toggle(this.darkClass);
    localStorage.setItem(this.storageKey, isDark ? 'dark' : 'light');
  }

  enableDarkMode(): void {
    document.documentElement.classList.add(this.darkClass);
    localStorage.setItem(this.storageKey, 'dark');
  }

  disableDarkMode(): void {
    document.documentElement.classList.remove(this.darkClass);
    localStorage.setItem(this.storageKey, 'light');
  }

}
