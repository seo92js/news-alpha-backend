package com.seo92js.news_alpha_backend.domain.stock;

import com.seo92js.news_alpha_backend.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_stock_report_stock_report_date", columnNames = {"stock_id", "report_date"})
        }
)
public class StockReport extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;

    @Column(nullable = false)
    private int signalCount;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String report;

    @Column(nullable = false)
    private LocalDateTime generatedAt;

    public static StockReport of(Stock stock, LocalDate reportDate, int signalCount, String report, LocalDateTime generatedAt) {
        StockReport stockReport = new StockReport();
        stockReport.stock = stock;
        stockReport.reportDate = reportDate;
        stockReport.signalCount = signalCount;
        stockReport.report = report;
        stockReport.generatedAt = generatedAt;
        return stockReport;
    }

    public void update(int signalCount, String report, LocalDateTime generatedAt) {
        this.signalCount = signalCount;
        this.report = report;
        this.generatedAt = generatedAt;
    }
}
