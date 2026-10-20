package com.wallet.wallet_service.repository;

import com.wallet.wallet_service.entity.EntryType;
import com.wallet.wallet_service.entity.LedgerEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.UUID;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {


    @Query("""
            select sum(case when e.entryType = :credit then e.amount else -e.amount end)
            from LedgerEntry e
            where e.wallet.id = :walletId
            """)
    BigDecimal sumSigned(@Param("walletId") UUID walletId, @Param("credit") EntryType credit);

    default BigDecimal balanceOf(UUID walletId) {
        BigDecimal sum = sumSigned(walletId, EntryType.CREDIT);
        return sum == null ? BigDecimal.ZERO.setScale(2) : sum;
    }


    @EntityGraph(attributePaths = "journalEntry")
    @Query("""
            select e from LedgerEntry e
            where e.wallet.id = :walletId
            order by e.createdAt desc, e.id desc
            """)
    Page<LedgerEntry> findHistory(@Param("walletId") UUID walletId, Pageable pageable);
}
