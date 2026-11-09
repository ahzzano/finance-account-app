import { Suspense } from "react";
import { getCurrencies } from "./lib/api";

async function CurrencyList() {
  const currencies = await getCurrencies();
  return currencies.map(({code, name}) => (<div key={code}>{code} - {name}</div>));
}

export default function Home() {
  return (
    <div>
      <Suspense  fallback={<p>Loading currencies</p>}>
        <CurrencyList />
      </Suspense>
    </div>
  );
}
