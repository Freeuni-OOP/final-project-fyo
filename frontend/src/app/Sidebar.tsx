import { Avatar, Ball, Button, Wordmark } from "../teams/ui";
import { displayNameOf, useSession } from "../session/SessionContext";
import { isNavItemActive, NAV } from "./nav";

interface SidebarProps {
  activeHash: string;
  onNavigate: () => void;
}

export function Sidebar({ activeHash, onNavigate }: SidebarProps) {
  const { user, signOut } = useSession();

  return (
    <aside className="shell__nav" aria-label="Platform">
      <div className="shell__brand">
        <a href="#/app" onClick={onNavigate} aria-label="FYO dashboard">
          <Wordmark />
        </a>
      </div>

      <nav className="shell__navlist">
        <p className="shell__navhead">Menu</p>
        {NAV.filter(item => item.href !== "#/app/admin" || user?.admin).map((item) =>
          item.soon ? (
            <span className="navitem navitem--soon" key={item.href} aria-disabled="true">
              {item.label}
              <em className="navitem__soon">Soon</em>
            </span>
          ) : (
            <a
              className={`navitem ${isNavItemActive(item, activeHash) ? "navitem--on" : ""}`}
              key={item.href}
              href={item.href}
              onClick={onNavigate}
              aria-current={isNavItemActive(item, activeHash) ? "page" : undefined}
            >
              {isNavItemActive(item, activeHash) && <Ball className="navitem__ball" />}
              {item.label}
            </a>
          )
        )}
      </nav>

      {user && (
        <div className="shell__user">
          <div className="shell__user-id">
            <Avatar src={user.imageUrl} name={displayNameOf(user)} size={38} />
            <div className="shell__user-text">
              <span className="shell__user-name">
                {user.name || user.username} {user.surname}
              </span>
              <span className="shell__user-handle">@{user.username}</span>
            </div>
          </div>
          <Button variant="ghost" onClick={signOut}>
            Log out
          </Button>
        </div>
      )}
    </aside>
  );
}
