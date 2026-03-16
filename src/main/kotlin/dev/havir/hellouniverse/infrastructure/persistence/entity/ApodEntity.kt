package dev.havir.hellouniverse.infrastructure.persistence.entity

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "apod")
class ApodEntity(
    @Id
    @Column(nullable = false, unique = true)
    var date: LocalDate,

    @Column(nullable = false)
    var title: String,

    @Column(columnDefinition = "TEXT", nullable = false)
    var explanation: String,

    @Column(name = "media_type", nullable = false)
    @Enumerated(EnumType.STRING)
    var mediaType: MediaTypeEntity,

    @Column(nullable = true)
    var url: String?,

    @Column(name = "hd_url", nullable = true)
    var hdUrl: String?,

    @Column(name = "thumbnail_url", nullable = true)
    var thumbnailUrl: String?,

    @Column(nullable = true)
    var copyright: String?,

    @Column(name = "service_version", nullable = false)
    var serviceVersion: String
)
