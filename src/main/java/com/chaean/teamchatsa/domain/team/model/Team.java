package com.chaean.teamchatsa.domain.team.model;

import com.chaean.teamchatsa.global.common.model.DeleteAndTimeEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "team")
public class Team extends DeleteAndTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "leader_user_id", nullable = false)
    private Long leaderUserId;

    @Size(max = 50)
    @NotNull
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Size(max = 50)
    @NotNull
    @Column(name = "area", nullable = false, length = 50)
    private String area;

    @Column(name = "description")
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "contact_type", nullable = false, length = 30)
    private ContactType contactType;

    @Size(max = 50)
    @NotNull
    @Column(name = "contact", nullable = false, length = 50)
    private String contact;

    @NotNull
    @Column(name = "level", nullable = false, length = 20)
    private String level;

    @Size(max = 255)
    @Column(name = "img")
    private String img;
}