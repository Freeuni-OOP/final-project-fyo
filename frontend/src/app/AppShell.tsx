import { useEffect, useState, type ReactNode } from "react";
import { Wordmark } from "../teams/ui";
import { useHashRoute } from "../routing";
import { Sidebar } from "./Sidebar";
import "../teams/theme.css";
import "../teams/teams.css";
import "./AppShell.css";
import "./pages/pages.css";

/** Sidebar + content column. Wrapped in `.court` so every token and component
 *  style from theme.css / teams.css resolves inside it. */
export function AppShell({ children }: { children: ReactNode }) {
  const hash = useHashRoute();
  const [navOpen, setNavOpen] = useState(false);

  useEffect(() => setNavOpen(false), [hash]);

  useEffect(() => {
    if (!navOpen) return;
    const onKey = (e: KeyboardEvent) => e.key === "Escape" && setNavOpen(false);
    document.addEventListener("keydown", onKey);
    const previousOverflow = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    return () => {
      document.removeEventListener("keydown", onKey);
      document.body.style.overflow = previousOverflow;
    };
  }, [navOpen]);

  return (
    <div className={`court shell ${navOpen ? "shell--navopen" : ""}`}>
      <Sidebar activeHash={hash} onNavigate={() => setNavOpen(false)} />

      {navOpen && (
        <div className="shell__scrim" onClick={() => setNavOpen(false)} aria-hidden="true" />
      )}

      <div className="shell__body">
        <div className="shell__mobilebar">
          <button
            className="shell__burger"
            onClick={() => setNavOpen(true)}
            aria-label="Open menu"
            aria-expanded={navOpen}
          >
            <span />
            <span />
            <span />
          </button>
          <Wordmark />
        </div>

        <main className="shell__main">{children}</main>
      </div>
    </div>
  );
}

interface PageHeadProps {
  eyebrow: string;
  title: ReactNode;
  actions?: ReactNode;
}

export function PageHead({ eyebrow, title, actions }: PageHeadProps) {
  return (
    <header className="page__head">
      <div>
        <p className="eyebrow">{eyebrow}</p>
        <h1 className="page__title">{title}</h1>
      </div>
      {actions && <div className="page__actions">{actions}</div>}
    </header>
  );
}
