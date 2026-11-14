import { expect, test } from '@playwright/test';
import { absent, open, opened, reload, shown, tap, tapAt, type } from './app';

test('search narrows the feed and an ad opens its seller', async ({ page }) => {
  await open(page, 'ads');
  await type(page, 'ads:search', 'honda');
  await shown(page, 'ads:ad-b1');
  await absent(page, 'ads:ad-a1');
  await tap(page, 'ads:ad-b1');
  await tap(page, 'ads:contact-telegram');
  const [url] = await opened(page);
  expect(url).toMatch(/^https:\/\/t\.me\/demo_bike_nt\?text=/);
});

test('nothing found says so', async ({ page }) => {
  await open(page, 'ads');
  await type(page, 'ads:search', 'вертолёт');
  await shown(page, 'ads:empty');
});

test('an own ad is published, kept across a reload, and deleted', async ({ page }) => {
  await open(page, 'ads');
  await tap(page, 'ads:new');
  await shown(page, 'ads:problems');
  await tap(page, 'ads:new-cat:chip-Sale');
  await type(page, 'ads:title', 'Продам шлем');
  await type(page, 'ads:text', 'Шлем, размер M, почти новый');
  await type(page, 'ads:price', '300000');
  await tap(page, 'ads:new-district:chip-North');
  await type(page, 'ads:author', 'Ваня');
  await type(page, 'ads:contact', '+84 912 111 222');
  await absent(page, 'ads:problems');
  await tap(page, 'ads:publish');
  await expect(page.getByText('Продам шлем').first()).toBeVisible();

  await reload(page);
  await expect(page.getByText('Продам шлем').first()).toBeVisible();
  await tapAt(page, page.getByRole('button', { name: /Продам шлем/ }));
  await expect(page.getByText('Объявление на модерации — пока его видите только вы.')).toBeVisible();
  await tap(page, 'ads:delete');
  await expect(page.getByText('Продам шлем')).toHaveCount(0);
});
