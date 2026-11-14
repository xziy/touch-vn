import { expect, type Locator, type Page } from '@playwright/test';

/** The ARIA node Compose made for a testTag. Playwright's CSS pierces the open shadow root. */
export function node(page: Page, tag: string): Locator {
  return page.locator(`[id="${tag}"]`);
}

/**
 * A fresh start of the portal. `window.open` is recorded rather than followed, so a test can read
 * the WhatsApp/Telegram link an order produced without leaving the page.
 */
export async function open(page: Page, appId?: string): Promise<void> {
  await page.addInitScript(() => {
    (window as any).__opened = [];
    window.open = ((url?: string | URL) => {
      (window as any).__opened.push(String(url));
      return null;
    }) as typeof window.open;
  });
  const link = appId ? `?link=${encodeURIComponent(`touchvn://app/${appId}`)}` : '';
  await page.goto(`/${link}`);
  await expect(page.getByRole('button', { name: 'Открыть меню' }).first()).toBeVisible({ timeout: 45_000 });
  await settle(page);
}

/** Reloads as a person returning to the tab would: same storage, new process. */
export async function reload(page: Page): Promise<void> {
  await page.reload();
  await expect(page.getByRole('button', { name: 'Открыть меню' }).first()).toBeVisible({ timeout: 45_000 });
  await settle(page);
}

export async function opened(page: Page): Promise<string[]> {
  return page.evaluate(() => (window as any).__opened as string[]);
}

/**
 * Brings a node into the visible part of the screen with the mouse wheel, as a person would.
 * Compose only mirrors what is composed, so a node below a long list may not exist yet: scroll
 * until it does.
 */
export async function reveal(page: Page, tag: string): Promise<{ x: number; y: number }> {
  const view = page.viewportSize()!;
  const top = 60; // under the host's top bar
  await settle(page);
  for (let i = 0; i < 25; i++) {
    const loc = node(page, tag);
    const box = (await loc.count()) > 0 ? await loc.first().boundingBox() : null;
    // A row that scrolls sideways (the services on Home): bring it in with a horizontal wheel.
    const inRows = box && box.height > 0 && box.y >= top && box.y + Math.min(box.height, 40) <= view.height - 8;
    if (inRows && (box!.x < 0 || box!.x + box!.width > view.width)) {
      await page.mouse.move(view.width / 2, box!.y + Math.min(box!.height / 2, 20));
      await page.mouse.wheel(box!.x < 0 ? -200 : 200, 0);
      await page.waitForTimeout(120);
      continue;
    }
    if (inRows) {
      // A wheel scroll is animated: only a node that has stopped moving is where a tap will land.
      await page.waitForTimeout(150);
      const again = await loc.first().boundingBox();
      if (again && Math.abs(again.y - box.y) < 1 && Math.abs(again.x - box.x) < 1) {
        return { x: box.x + box.width / 2, y: box.y + Math.min(box.height / 2, 20) };
      }
      continue;
    }
    // Past the edge of a sideways row the node's box is empty too, but its row's is not: wheel the row.
    if (!(box && box.height > 0) && (await loc.count()) > 0) {
      const row = await loc.first().locator('..').boundingBox();
      if (row && row.height > 0 && row.height < 200 && row.width > row.height * 2 && row.y >= top && row.y + Math.min(row.height, 40) <= view.height - 8 && row.width > 0) {
        await page.mouse.move(view.width / 2, row.y + Math.min(row.height / 2, 20));
        await page.mouse.wheel(i < 12 ? 300 : -300, 0);
        await page.waitForTimeout(120);
        continue;
      }
    }
    // An off-screen node is mirrored with an empty box, which says nothing about where it is:
    // look below first, then above. A real box says which way to go.
    const known = box && box.height > 0;
    const delta = known ? (box!.y < top ? -300 : 300) : (i < 12 ? 300 : -300);
    await page.mouse.move(view.width / 2, view.height / 2);
    await page.mouse.wheel(0, delta);
    await page.waitForTimeout(120);
  }
  throw new Error(`no visible node with testTag "${tag}"`);
}

/** Waits out the shell's slide between screens: two frames are on screen while it runs. */
export async function settle(page: Page): Promise<void> {
  await expect(page.getByRole('button', { name: 'Открыть меню' })).toHaveCount(1);
}

/**
 * A real click on whatever a locator found (a role, a text). The canvas sits above the ARIA layer
 * and takes the pointer, so this clicks the place, not the element.
 */
export async function tapAt(page: Page, loc: Locator): Promise<void> {
  const box = await loc.first().boundingBox();
  if (!box) throw new Error(`nothing to tap: ${loc}`);
  await page.mouse.click(box.x + box.width / 2, box.y + Math.min(box.height / 2, 20));
  await page.waitForTimeout(150);
}

/** The top bar's back arrow. */
export async function back(page: Page): Promise<void> {
  await settle(page);
  await tapAt(page, page.getByRole('button', { name: 'Назад' }).first());
  await settle(page);
}

/** A real click at the node's place on the canvas. */
export async function tap(page: Page, tag: string): Promise<void> {
  const at = await reveal(page, tag);
  await page.mouse.click(at.x, at.y);
  await page.waitForTimeout(150);
}

/** Focuses a field by clicking it and types with the keyboard. `replace` clears it first. */
export async function type(page: Page, tag: string, text: string, replace = false): Promise<void> {
  await tap(page, tag);
  if (replace) {
    await page.keyboard.press('ControlOrMeta+A');
    await page.keyboard.press('Backspace');
  }
  await page.keyboard.type(text, { delay: 10 });
  await expect(node(page, tag)).toHaveText(replace ? text : new RegExp(escape(text)));
}

export async function shown(page: Page, tag: string): Promise<void> {
  await reveal(page, tag);
  await expect(node(page, tag)).toBeVisible();
}

export async function absent(page: Page, tag: string): Promise<void> {
  await expect(node(page, tag)).toHaveCount(0);
}

/** The bar's title — what the host shows for the page the app is on. */
export async function title(page: Page, text: string): Promise<void> {
  await expect(page.getByText(text, { exact: true }).first()).toBeVisible();
}

function escape(s: string): string {
  return s.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}
