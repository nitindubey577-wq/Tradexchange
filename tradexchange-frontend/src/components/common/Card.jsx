import clsx from "clsx";
import { motion } from "framer-motion";

const variants = {
  default: `
    bg-white border-gray-200
    dark:bg-[#0f172a] dark:border-white/10
  `,

  glass: `
    bg-white/70 border-gray-200 backdrop-blur-xl
    dark:bg-white/5 dark:border-white/10
  `,

  elevated: `
    bg-white border-gray-200 shadow-md
    dark:bg-[#0b1220] dark:border-white/10 dark:shadow-black/40
  `,

  flat: `
    bg-transparent border-transparent
  `,
};

const Card = ({
  children,
  className,
  variant = "default",
  padding = "p-4",
  header,
  footer,
  actions,
  hover = false,
}) => {
  return (
    <motion.div
      whileHover={hover ? { scale: 1.01 } : undefined}
      transition={{ duration: 0.15 }}
      className={clsx(
        "rounded-2xl border transition-all duration-200 min-h-0",
        variants[variant],
        hover && "hover:border-blue-500/40",
        className
      )}
    >
      {/* HEADER */}
      {(header || actions) && (
        <div className="
          flex items-center justify-between px-4 py-2
          border-b border-gray-200
          dark:border-white/10
        ">
          <div className="
            text-xs font-medium tracking-wide
            text-gray-600 dark:text-gray-400
          ">
            {header}
          </div>

          {actions && (
            <div className="flex items-center gap-2">
              {actions}
            </div>
          )}
        </div>
      )}

      {/* BODY */}
      <div className={clsx("min-h-0", padding)}>
        {children}
      </div>

      {/* FOOTER */}
      {footer && (
        <div className="
          px-4 py-2 text-xs
          border-t border-gray-200
          dark:border-white/10
          text-gray-500 dark:text-gray-400
        ">
          {footer}
        </div>
      )}
    </motion.div>
  );
};

export default Card;