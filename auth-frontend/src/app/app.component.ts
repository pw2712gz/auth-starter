import {Component, inject, OnInit} from '@angular/core';
import {RouterOutlet} from '@angular/router';
import {UserSessionService} from './core/services/user-session.service';
import {ThemeService} from './core/services/theme.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  templateUrl: './app.component.html',
})
export class AppComponent implements OnInit {
  private session = inject(UserSessionService);
  private themeService = inject(ThemeService);

  ngOnInit(): void {
    this.session.initUserSession(); // load /me on app start
    this.themeService.initTheme();  // apply saved or default theme
  }
}
