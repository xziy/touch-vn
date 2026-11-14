import { expect, test } from '@playwright/test';
import { back, node, open, opened, shown, tap, type } from './app';

test('Home shows the city bonus balance and opens the bonus screen', async ({ page }) => {
  await open(page);
  await expect(node(page, 'host:bonus')).toContainText('300 бонусов');
  await tap(page, 'host:bonus');
  await expect(node(page, 'bonus:balance')).toHaveText('300');
  await shown(page, 'bonus:place-food');
});

test('bonuses pay part of a food order, the message says so, and new ones come back', async ({ page }) => {
  await open(page, 'food');
  await tap(page, 'food:restaurant-pho-hung');
  await shown(page, 'food:bonus-offer');
  await tap(page, 'food:dish-pho-bo-plus');
  await tap(page, 'food:dish-pho-bo-plus');
  await tap(page, 'food:basket');
  await tap(page, 'food:checkout');
  await expect(node(page, 'food:bonus-discount')).toHaveText('−30 000 ₫');
  await expect(node(page, 'food:total')).toHaveText('95 000 ₫');

  await type(page, 'food:name', 'Иван');
  await type(page, 'food:phone', '+84 912 345 678');
  await type(page, 'food:address', 'Hùng Vương 1');
  await tap(page, 'food:contact-whatsapp');
  await shown(page, 'food:bonus-result');

  const [url] = await opened(page);
  const text = decodeURIComponent(url.split('text=')[1]);
  expect(text).toContain('Оплачено городскими бонусами Touch VN: −30 000 ₫ (300 бонусов)');
  expect(text).toContain('К оплате: 95 000 ₫');
  // 5% of 95 000 ₫.
  await expect(node(page, 'food:bonus-result')).toContainText('На счёте 47 бонусов');
});

test('the bonus screen opens in place from a service and back returns', async ({ page }) => {
  await open(page, 'flowers');
  await tap(page, 'flowers:bonus');
  await shown(page, 'bonus:page');
  await back(page);
  await shown(page, 'flowers:bonus');
});
