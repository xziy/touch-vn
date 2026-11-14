import { expect, test } from '@playwright/test';
import { node, open, opened, reload, shown, tap, type } from './app';

/** dd.mm.yyyy of today in Vietnam, shifted by `days`. */
function vnDate(days: number): string {
  const d = new Date(Date.now() + 7 * 3600_000 + days * 86_400_000);
  const p = (n: number) => String(n).padStart(2, '0');
  return `${p(d.getUTCDate())}.${p(d.getUTCMonth() + 1)}.${d.getUTCFullYear()}`;
}

test('the stay calculator counts the entry day as day one and remembers it', async ({ page }) => {
  await open(page, 'visarun');
  await tap(page, 'visarun:calculator');
  await type(page, 'visarun:entry', vnDate(-40));
  await expect(node(page, 'visarun:left')).toHaveText(`Последний день — ${vnDate(4)}, осталось 4 дн.`);
  await shown(page, 'visarun:pick');

  await tap(page, 'visarun:regime:chip-EVisa90');
  await expect(node(page, 'visarun:left')).toHaveText(`Последний день — ${vnDate(49)}, осталось 49 дн.`);

  // A reload starts the app on its list; the summary card there reads the stored date.
  await reload(page);
  await expect(node(page, 'visarun:calculator')).toContainText('осталось 49 дн.');
});

test('a wrong date is pointed out', async ({ page }) => {
  await open(page, 'visarun');
  await tap(page, 'visarun:calculator');
  await type(page, 'visarun:entry', '31.02.2026');
  await expect(page.getByText('Не похоже на дату — нужен формат 01.11.2026')).toBeVisible();
  await expect(node(page, 'visarun:result')).toHaveCount(0);
});

test('a border run is booked with the organiser', async ({ page }) => {
  await open(page, 'visarun');
  await tap(page, 'visarun:chip-Cambodia');
  await tap(page, 'visarun:offer-mocbai-bus');
  await tap(page, 'visarun:book');
  await type(page, 'visarun:name', 'Ivan Petrov');
  await type(page, 'visarun:phone', '+84 912 345 678');
  await type(page, 'visarun:date', '20.11');
  await tap(page, 'visarun:contact-telegram');
  await shown(page, 'visarun:sent');
  const [url] = await opened(page);
  expect(decodeURIComponent(url)).toContain('Тур: Мокбай — Камбоджа, автобус (Visarun NT)');
});
