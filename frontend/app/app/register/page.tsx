"use client";

import { AuthForm } from "@/components/auth-form";
import { register } from "../../lib/auth";

export default function RegisterPage() {
  return (
    <AuthForm
      title="Create account"
      submitLabel="Create account"
      onSubmit={register}
      errorMessage="Registration failed. Check your email and use a password of at least 8 characters."
      altText="Already have an account?"
      altHref="/app/login"
      altLabel="Log in"
      passwordMinLength={8}
    />
  );
}
