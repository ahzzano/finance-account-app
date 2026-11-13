CREATE TABLE accounts (
    id    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner UUID NOT NULL REFERENCES app_user(id),
    name  TEXT NOT NULL
);

CREATE TABLE account_balances (
    account_id UUID          NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    currency   CHAR(3)       NOT NULL REFERENCES currency(code),
    amount     NUMERIC(19,4) NOT NULL CHECK (amount >= 0),
    PRIMARY KEY (account_id, currency)
);
