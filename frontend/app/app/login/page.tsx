"use client";

import { AuthForm } from "@/components/auth-form";
import { login, loginSchema } from "../../lib/auth";

export default function LoginPage() {
  return (
    <AuthForm
      title="Log in"
      submitLabel="Log in"
      onSubmit={login}
      errorMessage="Invalid email or password."
      altText="No account?"
      altHref="/app/register"
      altLabel="Create one"
      schema={loginSchema}
    />
  );
}
