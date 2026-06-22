import { Ball } from "./Ball";
import "./Wordmark.css";

interface WordmarkProps {
  /** When set, renders as a link (used in the top bar). */
  href?: string;
  className?: string;
}

/** The FYO logotype, where the O is the optic ball. */
export function Wordmark({ href, className = "" }: WordmarkProps) {
  const inner = (
    <>
      FY
      <Ball className="ball--mark" />
    </>
  );

  if (href) {
    return (
      <a className={`wordmark ${className}`} href={href} aria-label="FYO home">
        {inner}
      </a>
    );
  }

  return <span className={`wordmark ${className}`}>{inner}</span>;
}
