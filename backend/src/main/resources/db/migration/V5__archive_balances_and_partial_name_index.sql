ALTER TABLE account_balances ADD COLUMN archived_at TIMESTAMPTZ;

DROP INDEX accounts_owner_name_unique;
CREATE UNIQUE INDEX accounts_owner_name_unique ON accounts (owner, lower(name)) WHERE archived_at IS NULL;
