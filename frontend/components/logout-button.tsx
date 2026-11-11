"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { logout } from "@/app/lib/auth";

export function LogoutButton() {
  const router = useRouter();
  const [pending, setPending] = useState(false);
  const [error, setError] = useState(false);

  async function handleClick() {
    setPending(true);
    setError(false);
    try {
      await logout();
      router.replace("/app/login");
      router.refresh();
    } catch {
      setError(true);
      setPending(false);
    }
  }

  return (
    <>
      <button type="button" disabled={pending} onClick={handleClick} className="btn">
        Logout
      </button>
      {error && (
        <div role="alert" className="alert alert-error">
          Logout failed. Please try again.
        </div>
      )}
    </>
  );
}
