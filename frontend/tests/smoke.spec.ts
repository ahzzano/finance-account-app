import { test, expect } from '@playwright/test';

test('dashboard lists seeded currencies', async ({ page }) => {
  const currenciesResponse = page.waitForResponse(r => new URL(r.url()).pathname === '/api/currencies' && r.ok());
  await page.goto('/');
  await currenciesResponse;

  await expect(page.getByText('USD - US Dollar')).toBeVisible();
  await expect(page.getByText('PHP - Philippine Peso')).toBeVisible();
});
