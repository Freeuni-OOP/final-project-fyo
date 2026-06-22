import "./Ball.css";

interface BallProps {
  className?: string;
}

/** The optic-lime ball: the brand's single accent shape. */
export function Ball({ className = "" }: BallProps) {
  return <span className={`ball ${className}`} aria-hidden="true" />;
}
