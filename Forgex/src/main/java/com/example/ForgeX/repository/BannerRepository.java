package com.example.ForgeX.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.ForgeX.model.Banner;
import com.example.ForgeX.model.BannerPlacement;

public interface BannerRepository extends JpaRepository<Banner, Long> {

    @Query("select distinct b from Banner b left join fetch b.images "
            + "order by b.placement asc, b.sortOrder asc, b.bannerId asc")
    List<Banner> findAllWithImages();

    @Query("select distinct b from Banner b left join fetch b.images "
            + "where b.placement = :placement order by b.sortOrder asc, b.bannerId asc")
    List<Banner> findByPlacementWithImages(@Param("placement") BannerPlacement placement);

    @Query("select distinct b from Banner b left join fetch b.images where b.bannerId = :bannerId")
    Optional<Banner> findByIdWithImages(@Param("bannerId") Long bannerId);

    @Query("select distinct b from Banner b left join fetch b.images "
            + "where b.active = true "
            + "and (b.startsAt is null or b.startsAt <= :now) "
            + "and (b.endsAt is null or b.endsAt >= :now) "
            + "order by b.placement asc, b.sortOrder asc, b.bannerId asc")
    List<Banner> findLiveWithImages(@Param("now") LocalDateTime now);

    @Query("select coalesce(max(b.sortOrder), -1) from Banner b where b.placement = :placement")
    int maxSortOrder(@Param("placement") BannerPlacement placement);

    long countByPlacement(BannerPlacement placement);
}
