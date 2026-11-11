import z from "zod";

const email = z.email("Enter a valid email address.");

export const loginSchema = z.object({
    email,
    password: z.string().min(1, "Enter your password."),
});

export const registerSchema = z.object({
    email,
    password: z
        .string()
        .min(8, "Password must be at least 8 characters.")
        .refine(
            (p) => new TextEncoder().encode(p).length <= 72,
            "Password must be at most 72 bytes.",
        ),
});
