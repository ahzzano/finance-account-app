async function csrfToken(): Promise<string> {
    const read = () => document.cookie.match(/(?:^|; )XSRF-TOKEN=([^;]*)/)?.[1];
    let token = read();
    // any request through the proxy sets the CSRF cookie, even a 401
    if (!token) {
        await fetch("/auth/me");
        token = read();
    }
    return token ? decodeURIComponent(token) : "";
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
