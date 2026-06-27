import { type ReactNode } from "react";
import "./Button.css";

type Variant = "solid" | "ghost" | "optic";

interface ButtonProps {
  href: string;
  variant?: Variant;
  className?: string;
  children: ReactNode;
}

/** Link styled as a button. Sharp corners, no glow, three flat variants. */
export function Button({
  href,
  variant = "solid",
  className = "",
  children,
}: ButtonProps) {
  return (
    <a className={`btn btn--${variant} ${className}`} href={href}>
      {children}
    </a>
  );
}
