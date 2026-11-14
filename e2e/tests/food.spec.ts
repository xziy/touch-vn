import { expect, test } from '@playwright/test';
import { absent, node, open, opened, reload, shown, tap, title, type } from './app';

test('filter by cuisine', async ({ page }) => {
  await open(page, 'food');
  await tap(page, 'food:chip-Russian');
  await shown(page, 'food:restaurant-pelmeni');
  await absent(page, 'food:restaurant-pho-hung');
  await tap(page, 'food:chip-all');
  await shown(page, 'food:restaurant-pho-hung');
});

test('an order goes to the restaurant on WhatsApp, with everything in it', async ({ page }) => {
  await open(page, 'food');
  await tap(page, 'food:restaurant-pho-hung');
  await title(page, 'Phở Hưng');
  await tap(page, 'food:dish-pho-bo-plus');
  await tap(page, 'food:dish-pho-bo-plus');
  await expect(node(page, 'food:dish-pho-bo-qty')).toHaveText('2');
  await tap(page, 'food:basket');
  await expect(node(page, 'food:total')).toHaveText('125 000 ₫');
  await tap(page, 'food:checkout');
  await shown(page, 'food:fill-in');

  await type(page, 'food:name', 'Иван');
  await type(page, 'food:phone', '+84 912 345 678');
  await type(page, 'food:address', 'Hùng Vương 1, номер 12');
  await tap(page, 'food:contact-whatsapp');
  await shown(page, 'food:sent');

  const [url] = await opened(page);
  expect(url).toMatch(/^https:\/\/wa\.me\/84900000101\?text=/);
  const text = decodeURIComponent(url.split('text=')[1]);
  expect(text).toContain('Фо бо × 2 — 110 000 ₫');
  expect(text).toContain('Итого: 125 000 ₫');
  expect(text).toContain('Адрес: Hùng Vương 1, номер 12');
});

test('the basket survives a reload, and below the minimum there is no checkout', async ({ page }) => {
  await open(page, 'food');
  await tap(page, 'food:restaurant-lac-canh');
  await tap(page, 'food:dish-fried-rice-plus');
  await reload(page);
  // The session is restored onto the menu; the basket is in storage.
  await shown(page, 'food:basket');
  await tap(page, 'food:basket');
  await expect(node(page, 'food:total')).toHaveText('115 000 ₫');
  await expect(page.getByText(/Минимальный заказ 200 000 ₫/)).toBeVisible();
});
