import { expect, test } from '@playwright/test';
import { node, open, opened, reload, tap, type } from './app';

test('roubles: best rate first, and the amount in dong', async ({ page }) => {
  await open(page, 'exchange');
  await tap(page, 'exchange:chip-RUB');
  await type(page, 'exchange:amount', '50000', true);
  const best = node(page, 'exchange:point-private-rub');
  await expect(best).toContainText('Лучший');
  await expect(best).toContainText('15 600 000 ₫');
  // The bank does not buy roubles at all.
  await expect(node(page, 'exchange:point-bank-vcb')).toHaveCount(0);
});

test('the currency and amount are remembered, and reach the exchanger', async ({ page }) => {
  await open(page, 'exchange');
  await tap(page, 'exchange:chip-EUR');
  await type(page, 'exchange:amount', '200', true);
  await reload(page);
  await expect(node(page, 'exchange:amount')).toHaveText('200');
  await tap(page, 'exchange:point-gold-center');
  await tap(page, 'exchange:contact-zalo');
  const [url] = await opened(page);
  expect(url).toBe('https://zalo.me/84900000502');
});
