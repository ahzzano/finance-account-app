import z from "zod";

const Currency = z.object({code: z.string().length(3), name: z.string()})

export async function getCurrencies() {
    const res = await fetch("/api/currencies")
    if (!res.ok) throw new Error(`Failed to load currencies: ${res.status}`);
    return z.array(Currency).parse(await res.json())
}