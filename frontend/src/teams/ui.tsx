import { type ButtonHTMLAttributes, type ReactNode } from "react";

/* ---- Ball: the optic-lime accent shape (the brand's one motif) ---- */
export function Ball({ className = "" }: { className?: string }) {
  return <span className={`ball ${className}`} aria-hidden="true" />;
}

/* ---- Wordmark: FYO logotype, where the O is the optic ball ---- */
export function Wordmark({
  onClick,
  className = "",
}: {
  onClick?: () => void;
  className?: string;
}) {
  const inner = (
    <>
      FY
      <Ball className="ball--mark" />
    </>
  );
  if (onClick) {
    return (
      <button
        type="button"
        className={`wordmark wordmark--btn ${className}`}
        onClick={onClick}
        aria-label="FYO home"
      >
        {inner}
      </button>
    );
  }
  return <span className={`wordmark ${className}`}>{inner}</span>;
}

/* ---- Button: link/button with three flat variants, sharp corners ---- */
type Variant = "solid" | "ghost" | "optic";

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: Variant;
  children: ReactNode;
}

export function Button({
  variant = "solid",
  className = "",
  children,
  ...rest
}: ButtonProps) {
  return (
    <button className={`btn btn--${variant} ${className}`} {...rest}>
      {children}
    </button>
  );
}

/* ---- Avatar: round player image with initials fallback ---- */
export function Avatar({
  src,
  name,
  size = 40,
  className = "",
}: {
  src: string | null;
  name: string;
  size?: number;
  className?: string;
}) {
  const initials = name
    .split(" ")
    .map((w) => w[0])
    .filter(Boolean)
    .slice(0, 2)
    .join("")
    .toUpperCase();

  return (
    <span
      className={`avatar ${className}`}
      style={{ width: size, height: size, fontSize: size * 0.36 }}
    >
      {src ? <img src={src} alt={name} loading="lazy" /> : <span>{initials}</span>}
    </span>
  );
}
