import { useCallback, useEffect, useMemo, useState } from "react";
import { getSports } from "../../api/Sports";
import { matchesApi, type Match, type MatchStatus } from "../../api/matches";
import { ApiError } from "../../api/http";
import { chatMatchPath } from "../../chat/routes";
import { useAuth } from "../../hooks/useAuth";
import { ListingsApiError, listingsApi } from "../../match-listings/api";
import type { ListingResponse, MatchListing } from "../../match-listings/types";
import { MatchDetail } from "../../matches/MatchDetail";
import { useMyTeams } from "../../teams/useMyTeams";
import { useReveal } from "../../teams/useReveal";
import { Avatar, Button } from "../../teams/ui";
import { useSession } from "../../session/SessionContext";
import { PageHead } from "../AppShell";
import "../../matches/matches.css";

type Tab = "listings" | "scheduled";
type StatusFilter = "ALL" | MatchStatus;

function formatWhen(iso: string | null): string {
  if (!iso) return "TBD";
  try {
    return new Date(iso).toLocaleString("en-US", {
      day: "numeric",
      month: "short",
      hour: "2-digit",
      minute: "2-digit",
    });
  } catch {
    return iso;
  }
}

function formatLabel(format: MatchListing["format"]): string {
  return format === "ONE_VS_ONE" ? "1v1" : "Team";
}

function isListingOwner(listing: MatchListing, userId: number, captainTeamIds: Set<number>): boolean {
  if (listing.format === "ONE_VS_ONE") {
    return listing.postedBy.userId === userId;
  }
  return listing.postedBy.teamId != null && captainTeamIds.has(listing.postedBy.teamId);
}

export function MatchesPage() {
  const { user } = useSession();
  const { getIdToken } = useAuth();
  const userId = user?.id;

  const [tab, setTab] = useState<Tab>("listings");

  const [listings, setListings] = useState<MatchListing[]>([]);
  const [listingsLoading, setListingsLoading] = useState(true);
  const [listingsError, setListingsError] = useState<string | null>(null);
  const [sportFilter, setSportFilter] = useState<string>("ALL");
  const [expandedListingId, setExpandedListingId] = useState<number | null>(null);
  const [responses, setResponses] = useState<ListingResponse[]>([]);
  const [responsesLoading, setResponsesLoading] = useState(false);
  const [actionError, setActionError] = useState<string | null>(null);
  const [acceptResult, setAcceptResult] = useState<{ matchId: number } | null>(null);

  const [matches, setMatches] = useState<Match[]>([]);
  const [matchesLoading, setMatchesLoading] = useState(false);
  const [matchesError, setMatchesError] = useState<string | null>(null);
  const [matchSport, setMatchSport] = useState<string>("ALL");
  const [matchStatus, setMatchStatus] = useState<StatusFilter>("ALL");
  const [openMatchId, setOpenMatchId] = useState<number | null>(null);

  const [postSportId, setPostSportId] = useState("");
  const [postTeamId, setPostTeamId] = useState("");
  const [postLocation, setPostLocation] = useState("");
  const [postWhen, setPostWhen] = useState("");
  const [posting, setPosting] = useState(false);

  const [respondTeamId, setRespondTeamId] = useState<Record<number, string>>({});
  const [respondingId, setRespondingId] = useState<number | null>(null);

  const [sports, setSports] = useState<{ id: number; sportName: string }[]>([]);
  const { teams: myTeams } = useMyTeams(userId);

  const captainTeams = useMemo(
    () => myTeams.filter((t) => t.role === "CAPTAIN").map((t) => t.team),
    [myTeams]
  );
  const captainTeamIds = useMemo(
    () => new Set(captainTeams.map((t) => t.id)),
    [captainTeams]
  );

  const requireToken = useCallback(async () => {
    const token = await getIdToken();
    if (!token) throw new ListingsApiError(401, "Your session expired. Sign in again.");
    return token;
  }, [getIdToken]);

  const loadListings = useCallback(() => {
    setListingsLoading(true);
    setListingsError(null);
    const sportId =
      sportFilter === "ALL" ? undefined : sports.find((s) => s.sportName === sportFilter)?.id;
    listingsApi
      .browse(sportId)
      .then(setListings)
      .catch((e: ListingsApiError) => setListingsError(e.message))
      .finally(() => setListingsLoading(false));
  }, [sportFilter, sports]);

  const loadMatches = useCallback(() => {
    if (userId == null) return;
    setMatchesLoading(true);
    setMatchesError(null);
    matchesApi
      .list({ userId })
      .then(setMatches)
      .catch((e: ApiError) => setMatchesError(e.message))
      .finally(() => setMatchesLoading(false));
  }, [userId]);

  useEffect(() => {
    void getSports().then(setSports);
  }, []);

  useEffect(() => {
    if (tab === "listings") loadListings();
  }, [tab, loadListings]);

  useEffect(() => {
    if (tab === "scheduled") loadMatches();
  }, [tab, loadMatches]);

  useReveal([tab, listings.length, matches.length, listingsLoading, matchesLoading]);

  const visibleListings = useMemo(
    () =>
      listings.filter(
        (l) => l.status === "OPEN" && (sportFilter === "ALL" || l.sport.name === sportFilter)
      ),
    [listings, sportFilter]
  );

  const listingSports = useMemo(
    () => Array.from(new Set(listings.map((l) => l.sport.name))).sort(),
    [listings]
  );

  const visibleMatches = useMemo(
    () =>
      matches.filter(
        (m) =>
          (matchSport === "ALL" || m.sport.name === matchSport) &&
          (matchStatus === "ALL" || m.status === matchStatus)
      ),
    [matches, matchSport, matchStatus]
  );

  const matchSports = useMemo(
    () => Array.from(new Set(matches.map((m) => m.sport.name))).sort(),
    [matches]
  );

  async function loadResponses(listingId: number) {
    setExpandedListingId(listingId);
    setResponsesLoading(true);
    setActionError(null);
    setAcceptResult(null);
    try {
      const token = await requireToken();
      const pending = await listingsApi.pendingResponses(token, listingId);
      setResponses(pending);
    } catch (e) {
      setActionError((e as ListingsApiError).message);
      setResponses([]);
    } finally {
      setResponsesLoading(false);
    }
  }

  async function handlePostListing(e: React.FormEvent) {
    e.preventDefault();
    if (!postSportId) return;
    setPosting(true);
    setActionError(null);
    try {
      const token = await requireToken();
      const proposedDatetime = postWhen ? new Date(postWhen).toISOString() : null;
      await listingsApi.create(token, {
        sportId: Number(postSportId),
        teamId: postTeamId ? Number(postTeamId) : null,
        location: postLocation.trim() || null,
        proposedDatetime,
      });
      setPostLocation("");
      setPostWhen("");
      setPostTeamId("");
      loadListings();
    } catch (err) {
      setActionError((err as ListingsApiError).message);
    } finally {
      setPosting(false);
    }
  }

  async function handleRespond(listing: MatchListing) {
    setRespondingId(listing.id);
    setActionError(null);
    try {
      const token = await requireToken();
      const teamId =
        listing.format === "TEAM_VS_TEAM"
          ? Number(respondTeamId[listing.id] || captainTeams[0]?.id)
          : null;
      await listingsApi.respond(token, listing.id, teamId);
      loadListings();
    } catch (err) {
      setActionError((err as ListingsApiError).message);
    } finally {
      setRespondingId(null);
    }
  }

  async function handleAccept(listingId: number, responseId: number) {
    setActionError(null);
    try {
      const token = await requireToken();
      const result = await listingsApi.accept(token, listingId, responseId);
      setAcceptResult({ matchId: result.matchId });
      loadListings();
      loadMatches();
      void loadResponses(listingId);
    } catch (err) {
      setActionError((err as ListingsApiError).message);
    }
  }

  async function handleDecline(listingId: number, responseId: number) {
    setActionError(null);
    try {
      const token = await requireToken();
      await listingsApi.decline(token, listingId, responseId);
      void loadResponses(listingId);
    } catch (err) {
      setActionError((err as ListingsApiError).message);
    }
  }

  return (
    <>
      <PageHead
        eyebrow="Matches"
        title="Find opponents & fixtures"
      />
      <p className="page__lead">
        Post open listings to find opponents, respond to others, and manage your scheduled matches.
      </p>

      <div className="matches-page__tabs" role="tablist">
        <button
          type="button"
          role="tab"
          className={tab === "listings" ? "matches-page__tab matches-page__tab--on" : "matches-page__tab"}
          onClick={() => setTab("listings")}
        >
          Open listings
        </button>
        <button
          type="button"
          role="tab"
          className={tab === "scheduled" ? "matches-page__tab matches-page__tab--on" : "matches-page__tab"}
          onClick={() => setTab("scheduled")}
        >
          Scheduled matches
        </button>
      </div>

      {actionError && <p className="shell__error">{actionError}</p>}

      {tab === "listings" && (
        <>
          <form className="listing-form" onSubmit={(e) => void handlePostListing(e)}>
            <h2 className="pf-section__title">Post a listing</h2>
            <div className="listing-form__row">
              <label>
                Sport
                <select
                  value={postSportId}
                  onChange={(e) => setPostSportId(e.target.value)}
                  required
                >
                  <option value="">Choose sport</option>
                  {sports.map((s) => (
                    <option key={s.id} value={s.id}>
                      {s.sportName}
                    </option>
                  ))}
                </select>
              </label>
              <label>
                Post as team (optional)
                <select value={postTeamId} onChange={(e) => setPostTeamId(e.target.value)}>
                  <option value="">1v1 (just me)</option>
                  {captainTeams.map((t) => (
                    <option key={t.id} value={t.id}>
                      {t.name}
                    </option>
                  ))}
                </select>
              </label>
              <label>
                Location
                <input
                  value={postLocation}
                  onChange={(e) => setPostLocation(e.target.value)}
                  placeholder="e.g. Central court"
                />
              </label>
              <label>
                Proposed time
                <input
                  type="datetime-local"
                  value={postWhen}
                  onChange={(e) => setPostWhen(e.target.value)}
                />
              </label>
            </div>
            <Button variant="solid" type="submit" disabled={posting}>
              {posting ? "Posting…" : "Post listing"}
            </Button>
          </form>

          <div className="match-filters">
            <select value={sportFilter} onChange={(e) => setSportFilter(e.target.value)}>
              <option value="ALL">All sports</option>
              {listingSports.map((name) => (
                <option key={name} value={name}>
                  {name}
                </option>
              ))}
            </select>
            <Button variant="ghost" onClick={loadListings}>
              Refresh
            </Button>
          </div>

          {listingsLoading && <p className="shell__state">Loading listings…</p>}
          {listingsError && !listingsLoading && (
            <p className="shell__state shell__state--error">{listingsError}</p>
          )}

          {!listingsLoading && visibleListings.length === 0 && (
            <p className="shell__state">No open listings right now. Post one above.</p>
          )}

          {visibleListings.map((listing) => {
            const owner = userId != null && isListingOwner(listing, userId, captainTeamIds);
            const expanded = expandedListingId === listing.id;
            return (
              <article key={listing.id} className="listing-card" data-reveal>
                <div className="listing-card__top">
                  <div>
                    <strong>
                      {listing.sport.name} · {formatLabel(listing.format)}
                    </strong>
                    <p className="listing-card__meta">
                      {listing.location ?? "Location TBD"} · {formatWhen(listing.proposedDatetime)}
                    </p>
                  </div>
                  <Avatar
                    src={listing.postedBy.imageUrl}
                    name={listing.postedBy.displayName}
                    size={40}
                  />
                </div>
                <p className="listing-card__meta">Posted by {listing.postedBy.displayName}</p>
                <div className="listing-card__actions">
                  {!owner && (
                    <>
                      {listing.format === "TEAM_VS_TEAM" && captainTeams.length > 0 && (
                        <select
                          value={respondTeamId[listing.id] ?? String(captainTeams[0]?.id ?? "")}
                          onChange={(e) =>
                            setRespondTeamId((prev) => ({
                              ...prev,
                              [listing.id]: e.target.value,
                            }))
                          }
                        >
                          {captainTeams.map((t) => (
                            <option key={t.id} value={t.id}>
                              {t.name}
                            </option>
                          ))}
                        </select>
                      )}
                      {listing.format === "TEAM_VS_TEAM" && captainTeams.length === 0 ? (
                        <span className="listing-card__meta">Captain a team to respond</span>
                      ) : (
                        <Button
                          variant="solid"
                          onClick={() => void handleRespond(listing)}
                          disabled={respondingId === listing.id}
                        >
                          {respondingId === listing.id ? "Sending…" : "Respond"}
                        </Button>
                      )}
                    </>
                  )}
                  {owner && (
                    <Button
                      variant="ghost"
                      onClick={() =>
                        expanded ? setExpandedListingId(null) : void loadResponses(listing.id)
                      }
                    >
                      {expanded ? "Hide responses" : "View responses"}
                    </Button>
                  )}
                  {listing.matchId != null && (
                    <a href={chatMatchPath(listing.matchId)}>Match chat →</a>
                  )}
                </div>
                {expanded && (
                  <div className="listing-responses">
                    {responsesLoading && <p className="shell__state">Loading responses…</p>}
                    {!responsesLoading && responses.length === 0 && (
                      <p className="shell__state">No pending responses.</p>
                    )}
                    {acceptResult && expandedListingId === listing.id && (
                      <p className="shell__state">
                        Match confirmed!{" "}
                        <a href={chatMatchPath(acceptResult.matchId)}>Open match chat →</a>
                      </p>
                    )}
                    {responses.map((r) => (
                      <div key={r.id} className="listing-responses__row">
                        <span>{r.responder.displayName}</span>
                        <div className="listing-card__actions">
                          <Button
                            variant="solid"
                            onClick={() => void handleAccept(listing.id, r.id)}
                          >
                            Accept
                          </Button>
                          <Button
                            variant="ghost"
                            onClick={() => void handleDecline(listing.id, r.id)}
                          >
                            Decline
                          </Button>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </article>
            );
          })}
        </>
      )}

      {tab === "scheduled" && (
        <>
          <div className="match-filters">
            <select value={matchSport} onChange={(e) => setMatchSport(e.target.value)}>
              <option value="ALL">All sports</option>
              {matchSports.map((name) => (
                <option key={name} value={name}>
                  {name}
                </option>
              ))}
            </select>
            <select value={matchStatus} onChange={(e) => setMatchStatus(e.target.value as StatusFilter)}>
              <option value="ALL">All statuses</option>
              <option value="UPCOMING">Upcoming</option>
              <option value="COMPLETED">Completed</option>
              <option value="CANCELLED">Cancelled</option>
            </select>
            <Button variant="ghost" onClick={loadMatches}>
              Refresh
            </Button>
          </div>

          {matchesLoading && <p className="shell__state">Loading matches…</p>}
          {matchesError && !matchesLoading && (
            <p className="shell__state shell__state--error">{matchesError}</p>
          )}

          {!matchesLoading && visibleMatches.length === 0 && (
            <p className="shell__state">No matches yet. Accept a listing to schedule one.</p>
          )}

          {visibleMatches.map((match) => (
            <article
              key={match.id}
              className="match-row"
              data-reveal
              onClick={() => setOpenMatchId(match.id)}
              onKeyDown={(e) => e.key === "Enter" && setOpenMatchId(match.id)}
              role="button"
              tabIndex={0}
            >
              <span className="match-row__sport">{match.sport.name}</span>
              <div className="match-row__vs">
                <strong>{match.home.displayName}</strong>
                <span className="match-row__sep">vs</span>
                <strong>{match.away.displayName}</strong>
              </div>
              <p className="listing-card__meta">
                {match.status} · {formatWhen(match.proposedDatetime)}
                {match.location ? ` · ${match.location}` : ""}
              </p>
            </article>
          ))}
        </>
      )}

      {openMatchId !== null && userId != null && (
        <MatchDetail
          matchId={openMatchId}
          actingUserId={userId}
          onClose={() => setOpenMatchId(null)}
          onUpdated={(updated) =>
            setMatches((prev) => prev.map((m) => (m.id === updated.id ? updated : m)))
          }
        />
      )}
    </>
  );
}
