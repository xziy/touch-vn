import { expect, test } from '@playwright/test';
import { back, node, open, shown, tap, title, type } from './app';

const services = ['food', 'flowers', 'ads', 'visarun', 'exchange'];

test('the wasm build starts and Home lists every service', async ({ page }) => {
  const errors: string[] = [];
  page.on('pageerror', (e) => errors.push(e.message));
  await open(page);
  for (const id of services) await shown(page, `home:service-${id}`);
  await expect(page.getByText('Сервисы', { exact: true })).toBeVisible();
  await shown(page, 'home:pick-pho-bo');
  expect(errors).toEqual([]);
});

test('a tile opens its service and back returns Home', async ({ page }) => {
  await open(page);
  await tap(page, 'home:service-flowers');
  await title(page, 'Цветы');
  await shown(page, 'flowers:bouquet-red-25');
  await back(page);
  await shown(page, 'home:service-flowers');
  await expect(node(page, 'flowers:bouquet-red-25')).toHaveCount(0);
});

for (const id of services) {
  test(`deep link touchvn://app/${id} opens it`, async ({ page }) => {
    await open(page, id);
    const first = {
      food: 'food:restaurant-pho-hung',
      flowers: 'flowers:bouquet-red-25',
      ads: 'ads:new',
      visarun: 'visarun:calculator',
      exchange: 'exchange:amount',
    }[id]!;
    await shown(page, first);
  });
}

test('search narrows Home to what matches, and a popular pick opens its service', async ({ page }) => {
  await open(page);
  await type(page, 'home:search', 'роз');
  await shown(page, 'home:pick-red-25');
  await expect(node(page, 'home:pick-pho-bo')).toHaveCount(0);
  await expect(node(page, 'home:service-food')).toHaveCount(0);
  await tap(page, 'home:pick-red-25');
  await title(page, 'Цветы');
  await shown(page, 'flowers:bouquet-red-25');
});
