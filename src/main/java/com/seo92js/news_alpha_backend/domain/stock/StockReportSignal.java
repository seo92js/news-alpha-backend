package com.seo92js.news_alpha_backend.domain.stock;

import com.seo92js.news_alpha_backend.BaseEntity;
import com.seo92js.news_alpha_backend.domain.signal.Signal;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_stock_report_signal_report_signal", columnNames = {"stock_report_id", "signal_id"}),
                @UniqueConstraint(name = "uk_stock_report_signal_report_rank", columnNames = {"stock_report_id", "rank_order"})
        }
)
public class StockReportSignal extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stock_report_id", nullable = false)
    private StockReport stockReport;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "signal_id", nullable = false)
    private Signal signal;

    @Column(name = "rank_order", nullable = false)
    private int rankOrder;

    public static StockReportSignal of(StockReport stockReport, Signal signal, int rankOrder) {
        StockReportSignal stockReportSignal = new StockReportSignal();
        stockReportSignal.stockReport = stockReport;
        stockReportSignal.signal = signal;
        stockReportSignal.rankOrder = rankOrder;
        return stockReportSignal;
    }
}
