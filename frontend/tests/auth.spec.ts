import { test, expect } from '@playwright/test';

test('unauthenticated user is redirected to login', async ({ page }) => {
  await page.goto('/app');
  await expect(page).toHaveURL(/\/app\/login$/);
});

test('register creates an account and signs in', async ({ page }, testInfo) => {
  const email = `user-${Date.now()}-${testInfo.workerIndex}-${testInfo.project.name}@example.com`;

  await page.goto('/app/register');
  await page.getByLabel('Email').fill(email);
  await page.getByLabel('Password').fill('password123');
  await page.getByRole('button', { name: 'Create account' }).click();

  await expect(page).toHaveURL(/\/app$/);
  await expect(page.getByText(email)).toBeVisible();
});

test('log in with existing credentials', async ({ browser }, testInfo) => {
  const email = `user-${Date.now()}-${testInfo.workerIndex}-${testInfo.project.name}@example.com`;
  const password = 'password123';

  const registerContext = await browser.newContext();
  const registerPage = await registerContext.newPage();
  await registerPage.goto('/app/register');
  await registerPage.getByLabel('Email').fill(email);
  await registerPage.getByLabel('Password').fill(password);
  await registerPage.getByRole('button', { name: 'Create account' }).click();
  await expect(registerPage).toHaveURL(/\/app$/);
  await registerContext.close();

  const loginContext = await browser.newContext();
  const loginPage = await loginContext.newPage();
  await loginPage.goto('/app/login');
  await loginPage.getByLabel('Email').fill(email);
  await loginPage.getByLabel('Password').fill(password);
  await loginPage.getByRole('button', { name: 'Log in' }).click();

  await expect(loginPage).toHaveURL(/\/app$/);
  await expect(loginPage.getByText(email)).toBeVisible();
  await loginContext.close();
});

test('login with wrong password shows an error', async ({ page }, testInfo) => {
  const email = `user-${Date.now()}-${testInfo.workerIndex}-${testInfo.project.name}@example.com`;

  await page.goto('/app/login');
  await page.getByLabel('Email').fill(email);
  await page.getByLabel('Password').fill('wrong-password');
  await page.getByRole('button', { name: 'Log in' }).click();

  await expect(page.getByRole('alert').filter({ hasText: 'Invalid email or password.' })).toBeVisible();
  await expect(page).toHaveURL(/\/app\/login$/);
});

test('logout returns to login and protects the dashboard', async ({ page }, testInfo) => {
  const email = `user-${Date.now()}-${testInfo.workerIndex}-${testInfo.project.name}@example.com`;

  await page.goto('/app/register');
  await page.getByLabel('Email').fill(email);
  await page.getByLabel('Password').fill('password123');
  await page.getByRole('button', { name: 'Create account' }).click();

  await expect(page).toHaveURL(/\/app$/);

  await page.getByRole('button', { name: 'Logout' }).click();

  await expect(page).toHaveURL(/\/app\/login$/);

  await page.goto('/app');
  await expect(page).toHaveURL(/\/app\/login$/);
});

test('register rejects a short password', async ({ page }, testInfo) => {
  const email = `user-${Date.now()}-${testInfo.workerIndex}-${testInfo.project.name}@example.com`;

  await page.goto('/app/register');
  await page.getByLabel('Email').fill(email);
  await page.getByLabel('Password').fill('short');
  await page.getByRole('button', { name: 'Create account' }).click();

  await expect(
    page.getByRole('alert').filter({ hasText: 'Password must be at least 8 characters.' })
  ).toBeVisible();
  await expect(page).toHaveURL(/\/app\/register$/);
});
