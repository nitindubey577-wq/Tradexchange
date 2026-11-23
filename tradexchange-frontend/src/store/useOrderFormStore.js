import { create } from "zustand";

export const useOrderFormStore = create((set, get) => ({
  // 🔹 STATE
  price: "",
  quantity: "",
  orderType: "LIMIT", // LIMIT / MARKET
  side: "BUY", // BUY / SELL

  // 🔹 SETTERS
  setPrice: (price) => set({ price }),
  setQuantity: (quantity) => set({ quantity }),
  setOrderType: (orderType) => set({ orderType }),
  setSide: (side) => set({ side }),

  // 🔹 RESET FORM
  resetForm: () =>
    set({
      price: "",
      quantity: "",
      orderType: "LIMIT",
      side: "BUY",
    }),

  // 🔹 DERIVED VALUE (TOTAL)
  getTotal: () => {
    const { price, quantity } = get();
    return (Number(price) || 0) * (Number(quantity) || 0);
  },
}));