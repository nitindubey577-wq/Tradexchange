import axios from "axios";

const API_URL = import.meta.env.VITE_API_URL || "http://localhost:5000";

const api = axios.create({
  baseURL: `${API_URL}/api/v1`,
});

export const getCandles = async (symbol, interval = "1m") => {
  const res = await api.get(
    `/candles?symbol=${symbol}&interval=${interval}`
  );
  return res.data;
};

export default api;