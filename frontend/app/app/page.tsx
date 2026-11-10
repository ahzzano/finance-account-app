import { Suspense } from "react";
import { getCurrentUser } from "../lib/session";

async function Dashboard() {
  const user = await getCurrentUser();
  return (
    <>
      <h1 className="text-2xl font-bold">Dashboard</h1>
      <p>Signed in as {user.email}</p>
    </>
  );
}

export default function Page() {
  return (
    <main className="p-8">
      <Suspense fallback={<span className="loading loading-spinner" role="status" aria-label="Loading" />}>
        <Dashboard />
      </Suspense>
    </main>
  );
}
