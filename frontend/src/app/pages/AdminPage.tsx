import { useCallback, useEffect, useState } from "react";
import { PageHead } from "../AppShell";
import { adminApi, AdminApiError } from "../../Admin/api";
import type { UserAdmin, TeamAdmin, SportAdmin } from "../../Admin/types";
import { useAuth } from "../../hooks/useAuth";
import { Button } from "../../teams/ui";

type Tab = "users" | "teams" | "sports";

export function AdminPage() {
    const { getIdToken } = useAuth();
    const [tab, setTab] = useState<Tab>("users");

    const [users, setUsers] = useState<UserAdmin[]>([]);
    const [teams, setTeams] = useState<TeamAdmin[]>([]);
    const [sports, setSports] = useState<SportAdmin[]>([]);

    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const [newSport, setNewSport] = useState("");
    const [addingSport, setAddingSport] = useState(false);

    const requireToken = useCallback(async () => {
        const token = await getIdToken();
        if (!token) throw new AdminApiError(401, "Your session expired. Sign in again.");
        return token;
    }, [getIdToken]);

    useEffect(() => {
        setError(null);
        setLoading(true);
        (async () => {
            try {
                const token = await requireToken();
                if (tab === "users") {
                    setUsers(await adminApi.getUsers(token));
                } else if (tab === "teams") {
                    setTeams(await adminApi.getTeams(token));
                } else {
                    setSports(await adminApi.getSports(token));
                }
            } catch (e) {
                setError((e as AdminApiError).message);
            } finally {
                setLoading(false);
            }
        })();
    }, [tab, requireToken]);

    async function handleArchiveUser(id: number) {
        try {
        const token = await requireToken();
        const updated = await adminApi.archiveUser(token, id);
        setUsers((prev) => prev.map((u) => (u.id === id ? updated : u)));
        } catch (e) {
        alert((e as AdminApiError).message);
        }
    }

    async function handleArchiveTeam(id: number) {
        try {
        const token = await requireToken();
        const updated = await adminApi.archiveTeam(token, id);
        setTeams((prev) => prev.map((t) => (t.id === id ? updated : t)));
        } catch (e) {
        alert((e as AdminApiError).message);
        }
    }

    async function handleAddSport(e: React.FormEvent) {
        e.preventDefault();
        if (!newSport.trim()) return;
        setAddingSport(true);
        try {
            const token = await requireToken();
            const created = await adminApi.createSport(token, newSport.trim());
            setSports((prev) => [...prev, created]);
            setNewSport("");
        } catch (e) {
            alert((e as AdminApiError).message);
        } finally {
            setAddingSport(false);
        }
    }

    async function handleDeleteSport(id: number) {
        if (!confirm("Delete this sport?")) return;
        try {
            const token = await requireToken();
            await adminApi.deleteSport(token, id);
            setSports((prev) => prev.filter((s) => s.id !== id));
        } catch (e) {
            alert((e as AdminApiError).message);
        }
    }

    return (
    <>
      <PageHead eyebrow="Platform" title="Admin" />

      <div className="admin__tabs">
        {(["users", "teams", "sports"] as Tab[]).map((t) => (
          <button
            key={t}
            className={`chip ${tab === t ? "chip--on" : ""}`}
            onClick={() => setTab(t)}
          >
            {t}
          </button>
        ))}
      </div>

      {loading && <p className="admin__state">Loading…</p>}
      {error && <p className="admin__state admin__state--error">{error}</p>}

      {!loading && !error && tab === "users" && (
        <div className="admin__section">
          <table className="admin__table">
            <thead>
              <tr>
                <th>User</th>
                <th>Email</th>
                <th>Region</th>
                <th>Status</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {users.map((u) => (
                <tr key={u.id} className={u.archived ? "admin__row--archived" : ""}>
                  <td>
                    <span className="admin__name">
                      {u.name} {u.surname}
                    </span>
                    <span className="admin__handle">@{u.username}</span>
                  </td>
                  <td className="admin__email">{u.email}</td>
                  <td>{u.region ?? "—"}</td>
                  <td>
                    {u.archived ? (
                      <span className="badge badge--closed">Archived</span>
                    ) : u.admin ? (
                      <span className="badge badge--open">Admin</span>
                    ) : (
                      <span className="admin__active">Active</span>
                    )}
                  </td>
                  <td>
                    {!u.archived && !u.admin && (
                      <Button variant="ghost" onClick={() => handleArchiveUser(u.id)}>
                        Archive
                      </Button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {!loading && !error && tab === "teams" && (
        <div className="admin__section">
          <table className="admin__table">
            <thead>
              <tr>
                <th>Team</th>
                <th>Sport</th>
                <th>Captain</th>
                <th>Spots</th>
                <th>Status</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {teams.map((t) => (
                <tr key={t.id} className={t.archived ? "admin__row--archived" : ""}>
                  <td>
                    <span className="admin__name">{t.name}</span>
                    <span className="admin__handle">{t.region ?? "—"}</span>
                  </td>
                  <td>{t.sportName}</td>
                  <td>@{t.captainUsername}</td>
                  <td>{t.openSpots} / {t.maxPlayers}</td>
                  <td>
                    {t.archived ? (
                      <span className="badge badge--closed">Archived</span>
                    ) : t.recruiting ? (
                      <span className="badge badge--open">Recruiting</span>
                    ) : (
                      <span className="admin__active">Closed</span>
                    )}
                  </td>
                  <td>
                    {!t.archived && (
                      <Button variant="ghost" onClick={() => handleArchiveTeam(t.id)}>
                        Archive
                      </Button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {!loading && !error && tab === "sports" && (
        <div className="admin__section">
          <form className="admin__add-sport" onSubmit={handleAddSport}>
            <input
              className="form__input"
              placeholder="New sport name"
              value={newSport}
              onChange={(e) => setNewSport(e.target.value)}
            />
            <Button variant="solid" type="submit" disabled={addingSport}>
              {addingSport ? "Adding…" : "Add sport"}
            </Button>
          </form>

          <table className="admin__table">
            <thead>
              <tr>
                <th>Sport</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {sports.map((s) => (
                <tr key={s.id}>
                  <td>{s.sportName}</td>
                  <td>
                    <Button variant="ghost" onClick={() => handleDeleteSport(s.id)}>
                      Delete
                    </Button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </>
  );
}
