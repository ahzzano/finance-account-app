"use client";

import Link from "next/link";
import { useId, useState } from "react";
import { useRouter } from "next/navigation";

type AuthFormProps = {
  title: string;
  submitLabel: string;
  onSubmit: (email: string, password: string) => Promise<void>;
  errorMessage: string;
  altText: string;
  altHref: string;
  altLabel: string;
  passwordMinLength?: number;
};

export function AuthForm({
  title,
  submitLabel,
  onSubmit,
  errorMessage,
  altText,
  altHref,
  altLabel,
  passwordMinLength,
}: AuthFormProps) {
  const router = useRouter();
  const emailId = useId();
  const passwordId = useId();
  const [pending, setPending] = useState(false);
  const [error, setError] = useState(false);

  async function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    const form = new FormData(e.currentTarget);
    setPending(true);
    setError(false);
    try {
      await onSubmit(String(form.get("email")), String(form.get("password")));
      router.replace("/app");
      router.refresh();
    } catch {
      setError(true);
      setPending(false);
    }
  }

  return (
    <div className="flex flex-1 items-center justify-center p-4">
      <div className="card bg-base-100 shadow-xl w-full max-w-sm">
        <div className="card-body">
          <h1 className="card-title">{title}</h1>
          <form onSubmit={handleSubmit}>
            <label htmlFor={emailId} className="label">
              Email
            </label>
            <input
              id={emailId}
              name="email"
              type="email"
              required
              autoComplete="email"
              className="input w-full"
            />
            <label htmlFor={passwordId} className="label">
              Password
            </label>
            <input
              id={passwordId}
              name="password"
              type="password"
              required
              minLength={passwordMinLength}
              autoComplete={passwordMinLength ? "new-password" : "current-password"}
              className="input w-full"
            />
            {error && (
              <div role="alert" className="alert alert-error mt-4">
                {errorMessage}
              </div>
            )}
            <button type="submit" disabled={pending} className="btn btn-primary w-full mt-4">
              {pending && <span className="loading loading-spinner" aria-hidden="true" />}
              {submitLabel}
            </button>
          </form>
          <p>
            {altText} <Link href={altHref} className="link">{altLabel}</Link>
          </p>
        </div>
      </div>
    </div>
  );
}
