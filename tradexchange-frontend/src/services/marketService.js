const API_URL = import.meta.env.VITE_API_URL || "http://localhost:5000";

export const fetchMarkets = async ({ signal } = {}) => {
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), 5000);

  try {
    const res = await fetch(`${API_URL}/api/v1/markets`, {
      signal: signal || controller.signal,
    });

    if (!res.ok) {
      throw new Error(`HTTP ${res.status}`);
    }

    const data = await res.json();

    if (!data || !Array.isArray(data.markets)) {
      return [];
    }

    return data.markets.map((m) => ({
      symbol: normalize(m.symbol),
      price: Number(m.price) || 0,
      change: Number(m.change) || 0,
    }));
  } catch (err) {
    if (err.name === "AbortError") {
      console.warn("Request aborted");
    } else {
      console.error("fetchMarkets error:", err.message);
    }

    return []; // 🔥 NEVER crash UI
  } finally {
    clearTimeout(timeout);
  }
};

/* ---------------- NORMALIZER ---------------- */
const normalize = (s) =>
  s?.replace("/", "-").toUpperCase();