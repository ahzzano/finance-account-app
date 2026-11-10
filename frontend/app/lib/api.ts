import z from "zod";
import { csrfToken } from "./auth";

const Currency = z.object({code: z.string().length(3), name: z.string()})

export async function apiFetch(path: string, init: RequestInit = {}): Promise<Response> {
    const headers = new Headers(init.headers)
    headers.set("X-XSRF-TOKEN", await csrfToken())
    const res = await fetch(path, { ...init, headers })
    if (res.status === 401) {
        // eslint-disable-next-line @next/next/no-location-assign-relative-destination -- plain util, no router; full reload clears client state
        window.location.assign("/app/login")
        throw new Error("Unauthorized")
    }
    return res
}

export async function getCurrencies() {
    const res = await apiFetch("/api/currencies")
    if (!res.ok) throw new Error(`Failed to load currencies: ${res.status}`);
    return z.array(Currency).parse(await res.json())
}