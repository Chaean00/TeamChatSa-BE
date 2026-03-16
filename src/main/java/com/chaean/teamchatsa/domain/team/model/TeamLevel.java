package com.chaean.teamchatsa.domain.team.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TeamLevel {
    LEVEL_1("하하", 1),
    LEVEL_2("하", 2),
    LEVEL_3("중하", 3),
    LEVEL_4("중", 4),
    LEVEL_5("중상", 5),
    LEVEL_6("상", 6),
    LEVEL_7("상상", 7);

    private final String description;
    private final int value;
}
