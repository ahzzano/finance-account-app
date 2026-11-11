"use client";

import Link from "next/link";
import { useId, useState } from "react";
import { useRouter } from "next/navigation";
import z from "zod";

type AuthFormProps = {
  title: string;
  submitLabel: string;
  onSubmit: (email: string, password: string) => Promise<void>;
  errorMessage: string;
  altText: string;
  altHref: string;
  altLabel: string;
  schema: z.ZodType<{ email: string; password: string }>;
  newPassword?: boolean;
};

export function AuthForm({
  title,
  submitLabel,
  onSubmit,
  errorMessage,
  altText,
  altHref,
  altLabel,
  schema,
  newPassword,
}: AuthFormProps) {
  const router = useRouter();
  const emailId = useId();
  const passwordId = useId();
  const [pending, setPending] = useState(false);
  const [error, setError] = useState(false);
  const [message, setMessage] = useState(errorMessage);

  async function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    const form = new FormData(e.currentTarget);
    const email = String(form.get("email"));
    const password = String(form.get("password"));
    const result = schema.safeParse({ email, password });
    if (!result.success) {
      setMessage(result.error.issues[0].message);
      setError(true);
      return;
    }
    setPending(true);
    setError(false);
    try {
      await onSubmit(email, password);
      router.replace("/app");
      router.refresh();
    } catch {
      setMessage(errorMessage);
      setError(true);
      setPending(false);
    }
  }

  return (
    <div className="flex flex-1 items-center justify-center p-4">
      <div className="card bg-base-100 shadow-xl w-full max-w-sm">
        <div className="card-body">
          <h1 className="card-title">{title}</h1>
          <form onSubmit={handleSubmit} noValidate>
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
              autoComplete={newPassword ? "new-password" : "current-password"}
              className="input w-full"
            />
            {error && (
              <div role="alert" className="alert alert-error mt-4">
                {message}
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
