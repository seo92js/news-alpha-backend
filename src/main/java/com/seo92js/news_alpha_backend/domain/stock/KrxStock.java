package com.seo92js.news_alpha_backend.domain.stock;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "stock_info")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KrxStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String baseDate; // 기준일자

    @Column(nullable = false, unique = true)
    private String ticker; // 종목코드

    @Column(nullable = false)
    private String isinCode; // ISIN코드

    @Column(nullable = false)
    private String market; // 시장구분

    @Column(nullable = false)
    private String name; // 종목명

    @Column(nullable = false)
    private String corpRegNo; // 법인등록번호

    @Column(nullable = false)
    private String corpName; // 법인명

    public static KrxStock of(String baseDate, String ticker, String isinCode, String market, String name
            , String corpRegNo, String corpName) {
        KrxStock meta = new KrxStock();
        meta.baseDate = baseDate;
        meta.ticker = ticker;
        meta.isinCode = isinCode;
        meta.market = market;
        meta.name = name;
        meta.corpRegNo = corpRegNo;
        meta.corpName = corpName;
        return meta;
    }

    public void update(String baseDate, String isinCode, String market, String name, String corpRegNo
            , String corpName) {
        this.baseDate = baseDate;
        this.isinCode = isinCode;
        this.market = market;
        this.name = name;
        this.corpRegNo = corpRegNo;
        this.corpName = corpName;
    }
}