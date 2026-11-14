import { defineConfig, devices } from '@playwright/test';
import { existsSync } from 'node:fs';

/**
 * End-to-end tests against the real wasm build (composeApp/build/dist/wasmJs/productionExecutable).
 * Build it first: ./e2e/run.sh does both.
 *
 * Compose draws into a canvas; the page is readable because the host turns on web accessibility
 * (isA11YEnabled): every semantics node becomes a positioned ARIA element and a testTag becomes its
 * `id`. Tests find nodes by the same tags as the JVM tests and click them with a real mouse.
 */
const PORT = 8094;
const DIST = '../composeApp/build/dist/wasmJs/productionExecutable';

// A system Chromium if there is one (no download needed); otherwise Playwright's own.
const chromium = process.env.CHROME_BIN ?? ['/usr/bin/chromium', '/usr/bin/chromium-browser', '/usr/bin/google-chrome']
  .find((p) => existsSync(p));

export default defineConfig({
  testDir: './tests',
  timeout: 60_000,
  expect: { timeout: 15_000 },
  fullyParallel: true,
  workers: process.env.CI ? 2 : 4,
  retries: process.env.CI ? 1 : 0,
  reporter: [['list'], ['html', { open: 'never' }]],
  use: {
    baseURL: `http://localhost:${PORT}`,
    ...devices['Pixel 7'],
    // A phone-shaped viewport, but desktop input: the tests use the mouse, not touch.
    hasTouch: false,
    isMobile: false,
    locale: 'ru-RU',
    timezoneId: 'Asia/Ho_Chi_Minh',
    screenshot: 'only-on-failure',
    trace: 'retain-on-failure',
    launchOptions: {
      executablePath: chromium,
      // Skiko needs WebGL; with no GPU this is a software one, not a blank white page.
      args: ['--use-gl=angle', '--use-angle=swiftshader', '--enable-unsafe-swiftshader'],
    },
  },
  projects: [{ name: 'chromium' }],
  webServer: {
    command: `python3 -m http.server ${PORT} --directory ${DIST}`,
    url: `http://localhost:${PORT}/`,
    reuseExistingServer: !process.env.CI,
    timeout: 30_000,
  },
});
