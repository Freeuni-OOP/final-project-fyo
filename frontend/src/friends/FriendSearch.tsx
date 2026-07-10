import { useEffect, useState } from "react";
import { usersApi } from "../api/users";
import type { UserSummary } from "../teams/types";
import { Avatar } from "../teams/ui";

const MIN_SEARCH_LENGTH = 2;

function fullName(user: UserSummary): string {
  return `${user.name} ${user.surname}`.trim() || user.username;
}

interface FriendSearchProps {
  currentUserId: number;
  friendIds: Set<number>;
  onRequestSent: (user: UserSummary) => void;
}

export function FriendSearch({ currentUserId, friendIds, onRequestSent }: FriendSearchProps) {
  const [term, setTerm] = useState("");
  const [results, setResults] = useState<UserSummary[]>([]);
  const [searching, setSearching] = useState(false);

  useEffect(() => {
    const trimmed = term.trim();
    if (trimmed.length < MIN_SEARCH_LENGTH) {
      setResults([]);
      setSearching(false);
      return;
    }

    setSearching(true);
    let alive = true;
    const timer = setTimeout(() => {
      usersApi
        .search(trimmed)
        .then((found) => alive && setResults(found))
        .catch(() => alive && setResults([]))
        .finally(() => alive && setSearching(false));
    }, 250);

    return () => {
      alive = false;
      clearTimeout(timer);
    };
  }, [term]);

  const selectable = results.filter(
    (user) => user.id !== currentUserId && !friendIds.has(user.id)
  );

  return (
    <div className="friends__search">
      <label className="friends__label" htmlFor="friend-search">
        Find players
      </label>
      <input
        id="friend-search"
        className="friends__input"
        value={term}
        onChange={(e) => setTerm(e.target.value)}
        placeholder="Search by username or name"
      />
      {searching && <p className="friends__hint">Searching…</p>}
      {!searching && term.trim().length >= MIN_SEARCH_LENGTH && selectable.length === 0 && (
        <p className="friends__hint">No players found.</p>
      )}
      {selectable.length > 0 && (
        <ul className="friends__results">
          {selectable.map((user) => (
            <li key={user.id}>
              <button
                type="button"
                className="friends__result"
                onClick={() => onRequestSent(user)}
              >
                <Avatar src={user.imageUrl} name={fullName(user)} size={36} />
                <span>
                  <strong>{fullName(user)}</strong>
                  <span>@{user.username}</span>
                </span>
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
