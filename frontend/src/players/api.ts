import { request } from "../api/http";
import type { PlayerSearchFilters, PlayerSummary } from "./types";

export { ApiError } from "../api/http";

export const playersApi = {
  search: (filters: PlayerSearchFilters = {}) => {
    const params = new URLSearchParams();
    if (filters.sportId) params.set("sportId", String(filters.sportId));
    if (filters.region) params.set("region", filters.region);
    if (filters.skillLevel) params.set("skillLevel", filters.skillLevel);
    if (filters.limit) params.set("limit", String(filters.limit));

    const qs = params.toString();
    return request<PlayerSummary[]>(`/api/players${qs ? `?${qs}` : ""}`);
  },
};