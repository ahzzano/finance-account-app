import { test, expect } from '@playwright/test';

test('redirects to login on 401', async ({ page }) => {
  await page.route('**/api/currencies', (route) => route.fulfill({ status: 401 }));

  await page.goto('/');

  await expect(page).toHaveURL(/\/app\/login$/);
});

test('sends the CSRF header', async ({ page }) => {
  let token: string | undefined;
  await page.route('**/api/currencies', (route) => {
    token = route.request().headers()['x-xsrf-token'];
    return route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify([{ code: 'USD', name: 'US Dollar' }]),
    });
  });

  await page.goto('/');

  await expect(page.getByText('USD - US Dollar')).toBeVisible();
  // XSRF-TOKEN cookie is set by the real backend via /auth/me
  const cookie = (await page.context().cookies()).find((c) => c.name === 'XSRF-TOKEN');
  expect(token).toBe(decodeURIComponent(cookie!.value));
});

test('does not redirect on 500', async ({ page }) => {
  await page.route('**/api/currencies', (route) => route.fulfill({ status: 500 }));

  await page.goto('/');

  await expect(page.getByText('Failed to load currencies')).toBeVisible();
  await expect(page.waitForURL(/\/app\/login$/, { timeout: 1000 })).rejects.toThrow();
  await expect(page).toHaveURL('/');
});
