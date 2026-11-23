const API_URL = import.meta.env.VITE_API_URL || "http://localhost:5000";
const BASE_URL = `${API_URL}/api/v1/user`;

// 🔹 GET WATCHLIST
export const getWatchlist = async () => {
  try {
    const res = await fetch(`${BASE_URL}/watchlist`);

    if (!res.ok) {
      throw new Error(`Error ${res.status}: Failed to fetch watchlist`);
    }

    const data = await res.json();

    // ✅ Extract only watchlist
    return data.watchlist || [];
  } catch (err) {
    console.error("getWatchlist error:", err);
    return []; // ✅ fallback (prevents UI crash)
  }
};

// 🔹 TOGGLE WATCHLIST
export const toggleWatchlist = async (symbol) => {
  try {
    const res = await fetch(`${BASE_URL}/watchlist`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ symbol }),
    });

    if (!res.ok) {
      throw new Error(`Error ${res.status}: Failed to update watchlist`);
    }

    const data = await res.json();

    // ✅ Return updated watchlist
    return data.watchlist || [];
  } catch (err) {
    console.error("toggleWatchlist error:", err);
    return null; // UI can handle this
  }
};