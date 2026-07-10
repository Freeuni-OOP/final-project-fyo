import { Avatar } from "../teams/ui";
import { chatMatchPath } from "../chat/routes";
import type { Profile } from "./types";
import "./profile.css";

function formatDate(value: string | null): string {
  if (!value) return "—";
  try {
    return new Date(value).toLocaleString(undefined, {
      dateStyle: "medium",
      timeStyle: "short",
    });
  } catch {
    return value;
  }
}

function formatRating(value: number | null): string {
  if (value == null) return "No ratings yet";
  return `${value.toFixed(1)} / 5`;
}

export function ProfileView({
  profile,
  showEmail = false,
}: {
  profile: Profile;
  showEmail?: boolean;
}) {
  const fullName = `${profile.name} ${profile.surname}`.trim();

  return (
    <div className="pf-view">
      <section className="pf-hero" data-reveal>
        <Avatar src={profile.imageUrl} name={fullName || profile.username} size={88} />
        <div className="pf-hero__text">
          <p className="eyebrow">Player profile</p>
          <h1 className="pf-hero__name">{fullName || profile.username}</h1>
          <p className="pf-hero__handle">@{profile.username}</p>
          <div className="pf-hero__meta">
            {profile.region && <span>{profile.region}</span>}
            {profile.age != null && <span>{profile.age} yrs</span>}
            {profile.sex && <span>{profile.sex}</span>}
            {showEmail && profile.email && <span>{profile.email}</span>}
          </div>
          <p className="pf-hero__rating">{formatRating(profile.ratingAverage)}</p>
        </div>
      </section>

      <section className="pf-section" data-reveal>
        <h2 className="pf-section__title">Sports</h2>
        {profile.sports.length === 0 ? (
          <p className="pf-empty">No sports selected yet.</p>
        ) : (
          <ul className="pf-sports">
            {profile.sports.map((sport) => (
              <li key={sport.sportId} className="pf-sport">
                <span className="pf-sport__name">{sport.sportName}</span>
                <span className="pf-sport__level">{sport.skillLevel}</span>
              </li>
            ))}
          </ul>
        )}
      </section>

      <section className="pf-section" data-reveal>
        <h2 className="pf-section__title">Match history</h2>
        {profile.matchHistory.length === 0 ? (
          <p className="pf-empty">No matches yet.</p>
        ) : (
          <ul className="pf-list">
            {profile.matchHistory.map((match) => (
              <li key={match.matchId} className="pf-card">
                <div className="pf-card__top">
                  <strong>{match.sportName}</strong>
                  <span className="pf-pill">{match.status}</span>
                </div>
                <p className="pf-card__line">
                  {match.format}
                  {match.opponentUsername
                    ? ` · vs @${match.opponentUsername}`
                    : match.homeTeamName && match.awayTeamName
                      ? ` · ${match.homeTeamName} vs ${match.awayTeamName}`
                      : ""}
                </p>
                {match.location && <p className="pf-card__muted">{match.location}</p>}
                <p className="pf-card__muted">{formatDate(match.proposedDatetime)}</p>
                {(match.homeScore != null || match.awayScore != null) && (
                  <p className="pf-card__score">
                    Score {match.homeScore ?? "—"}–{match.awayScore ?? "—"}
                    {match.winner ? ` · ${match.winner}` : ""}
                  </p>
                )}
                {match.status === "UPCOMING" && (
                  <p className="pf-card__actions">
                    <a href={chatMatchPath(match.matchId)}>Open match chat →</a>
                  </p>
                )}
              </li>
            ))}
          </ul>
        )}
      </section>

      <section className="pf-section" data-reveal>
        <h2 className="pf-section__title">Reviews</h2>
        {profile.reviews.length === 0 ? (
          <p className="pf-empty">No reviews yet.</p>
        ) : (
          <ul className="pf-list">
            {profile.reviews.map((review) => (
              <li key={review.id} className="pf-card">
                <div className="pf-card__top">
                  <strong>@{review.reviewerUsername}</strong>
                  <span className="pf-pill">{review.score}/5</span>
                </div>
                {review.comment && <p className="pf-card__line">{review.comment}</p>}
                <p className="pf-card__muted">{formatDate(review.createdAt)}</p>
              </li>
            ))}
          </ul>
        )}
      </section>
    </div>
  );
}
