import { test, expect } from '@playwright/test';

test('dashboard lists seeded currencies', async ({ page }) => {
  await page.goto('/');

  await expect(page.getByText('USD - US Dollar')).toBeVisible();
  await expect(page.getByText('PHP - Philippine Peso')).toBeVisible();
});
