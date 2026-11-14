import { expect, test } from '@playwright/test';
import { open, opened, shown, tap, title, type } from './app';

test('a bouquet with a card is ordered on Telegram', async ({ page }) => {
  await open(page, 'flowers');
  await tap(page, 'flowers:chip-Box');
  await tap(page, 'flowers:bouquet-box-pink');
  await title(page, 'Розовая коробка');
  await tap(page, 'flowers:order');
  await shown(page, 'flowers:fill-in');

  await type(page, 'flowers:recipient', 'Анна');
  await type(page, 'flowers:address', 'Ana Mandara, 302');
  await type(page, 'flowers:when', 'завтра к 10:00');
  await type(page, 'flowers:card', 'С днём рождения!');
  await type(page, 'flowers:sender', 'Пётр');
  await type(page, 'flowers:sender-phone', '0912345678');
  await tap(page, 'flowers:contact-telegram');
  await shown(page, 'flowers:sent');

  const [url] = await opened(page);
  expect(url).toMatch(/^https:\/\/t\.me\/demo_flowers_nt\?text=/);
  const text = decodeURIComponent(url.split('text=')[1]);
  expect(text).toContain('Букет: Розовая коробка — 690 000 ₫');
  expect(text).toContain('Открытка: «С днём рождения!»');
});
