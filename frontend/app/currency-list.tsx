"use client";

import { useEffect, useState } from "react";
import { getCurrencies } from "./lib/api";

type Currency = { code: string; name: string };
type State =
  | { status: "loading" }
  | { status: "loaded"; currencies: Currency[] }
  | { status: "error" };

export function CurrencyList() {
  const [state, setState] = useState<State>({ status: "loading" });

  useEffect(() => {
    let ignore = false;
    getCurrencies()
      .then((currencies) => {
        if (!ignore) setState({ status: "loaded", currencies });
      })
      .catch(() => {
        if (!ignore) setState({ status: "error" });
      });
    return () => {
      ignore = true;
    };
  }, []);

  if (state.status === "loading") return <p>Loading currencies</p>;
  if (state.status === "error") return <p>Failed to load currencies</p>;
  return state.currencies.map(({ code, name }) => (
    <div key={code}>{code} - {name}</div>
  ));
}
