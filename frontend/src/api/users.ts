import { request } from "./http";
import type { UserSummary } from "../teams/types";

export const usersApi = {
  /** Typeahead over usernames and full names. A term under 2 characters returns []. */
  search: (term: string, limit = 8) =>
    request<UserSummary[]>(
      `/api/users?q=${encodeURIComponent(term)}&limit=${limit}`
    ),
};
