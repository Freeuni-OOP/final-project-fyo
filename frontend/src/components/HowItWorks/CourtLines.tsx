/** Chalk markings drawn in CSS: a half-court seen from above. */
export function CourtLines() {
  return (
    <div className="court__lines" aria-hidden="true">
      <span className="line line--top" />
      <span className="line line--bottom" />
      <span className="line line--left" />
      <span className="line line--right" />
      <span className="line line--net" />
      <span className="line line--service" />
      <span className="line line--center" />
      <span className="court__mark court__mark--a">YOU</span>
      <span className="court__mark court__mark--b">THEM</span>
    </div>
  );
}
