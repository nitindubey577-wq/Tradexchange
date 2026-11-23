import { FixedSizeList as List } from "react-window";

const Row = ({ index, style, data }) => {
  const symbol = data.markets[index];
  const active = data.activeSymbol === symbol;

  return (
    <div
      style={style}
      onClick={() => data.setActive(symbol)}
      className={`px-3 py-2 cursor-pointer text-sm ${
        active ? "bg-blue-500/20" : "hover:bg-gray-100"
      }`}
    >
      {symbol}
    </div>
  );
};

export default function MarketList({ markets, activeSymbol, setActive }) {
  return (
    <List
      height={600}
      itemCount={markets.length}
      itemSize={35}
      width="100%"
      itemData={{ markets, activeSymbol, setActive }}
    >
      {Row}
    </List>
  );
}