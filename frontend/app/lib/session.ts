import { cookies } from "next/headers";
import { redirect } from "next/navigation";
import z from "zod";

const User = z.object({ id: z.string(), email: z.string() });

export async function getCurrentUser(): Promise<{ id: string; email: string }> {
  const cookie = (await cookies()).toString();
  const res = await fetch(`${process.env.BACKEND_URL}/auth/me`, {
    headers: { cookie },
    cache: "no-store",
  });

  if (res.status === 401 || res.status === 403) {
    redirect("/app/login");
  }
  if (!res.ok) throw new Error(`Failed to load current user: ${res.status}`);

  return User.parse(await res.json());
}
