import {ActivatedRouteSnapshot, Router, RouterStateSnapshot, UrlTree} from '@angular/router';
import {TestBed} from '@angular/core/testing';

export const mockRoute = {} as ActivatedRouteSnapshot;
export const mockState = {} as RouterStateSnapshot;

export let router: Router;
export let navigateSpy: jasmine.Spy;

export function setupTestBedWithRouter() {
  TestBed.configureTestingModule({
    providers: [
      {
        provide: Router,
        useValue: {
          createUrlTree: (commands: string[]) => {
            const path = Array.isArray(commands) ? commands.join('/') : commands;
            return {toString: () => '/' + path} as unknown as UrlTree;
          },
          navigateByUrl: () => Promise.resolve(true),
        },
      },
    ],
  });

  router = TestBed.inject(Router);
  navigateSpy = spyOn(router, 'navigateByUrl').and.callThrough();
}

/** Utility to generate a fake JWT with a given exp field */
export function generateFakeToken(payload: { exp: number }): string {
  const base64 = (obj: object) =>
    btoa(JSON.stringify(obj))
      .replace(/=/g, '')
      .replace(/\+/g, '-')
      .replace(/\//g, '_');

  return `${base64({alg: 'HS256', typ: 'JWT'})}.${base64(payload)}.signature`;
}
