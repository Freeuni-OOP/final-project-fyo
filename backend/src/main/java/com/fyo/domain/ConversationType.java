package com.fyo.domain;

public enum ConversationType {
    /** Auto-created when a match is confirmed; linked via match_id. */
    MATCH,
    /** Ad-hoc 1:1 between two users. */
    DIRECT,
    /** Group chat for a team roster; linked via team_id. */
    TEAM
}
