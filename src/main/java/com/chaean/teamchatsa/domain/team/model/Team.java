package com.chaean.teamchatsa.domain.team.model;

import com.chaean.teamchatsa.global.common.model.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "team")
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE team SET deleted_at = NOW() WHERE id = ?")
public class Team extends BaseEntity {
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

    public static Team of(Long leaderUserId, String name, String area, String description, ContactType contactType, String contact, String level, String img) {
        Team team = new Team();
        team.leaderUserId = leaderUserId;
        team.name = name;
        team.area = area;
        team.description = description;
        team.contactType = contactType;
        team.contact = contact;
        team.level = level;
        team.img = img;
        return team;
    }
}