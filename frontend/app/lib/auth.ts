export async function csrfToken(): Promise<string> {
    const cookie = document.cookie.match(/(?:^|; )XSRF-TOKEN=([^;]*)/)?.[1];
    if (cookie) return decodeURIComponent(cookie);
    // WebKit may not expose a cookie set by fetch to document.cookie yet, so take the token from the body
    const res = await fetch("/auth/csrf");
    if (!res.ok) throw new Error(`/auth/csrf failed: ${res.status}`);
    return ((await res.json()) as { token: string }).token;
}

async function post(path: string, body: unknown): Promise<void> {
    const res = await fetch(path, {
        method: "POST",
        headers: { "Content-Type": "application/json", "X-XSRF-TOKEN": await csrfToken() },
        body: JSON.stringify(body),
    });
    if (!res.ok) throw new Error(`${path} failed: ${res.status}`);
}

export async function login(email: string, password: string): Promise<void> {
    await post("/auth/login", { email, password });
}

export async function register(email: string, password: string): Promise<void> {
    await post("/auth/register", { email, password });
    await login(email, password);
}

export async function logout(): Promise<void> {
    await post("/auth/logout", {});
}
