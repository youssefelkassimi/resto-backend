package com.fst.rsi.resto.dto;

@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public  class TableStatistics {
    private long totalTables;
    private long tablesLibres;
    private long tablesOccupees;
    private long tablesReservees;
    private long tablesHorsService;
}
