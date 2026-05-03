# Python Data Cleaning and Reproduction Package

This package supports the paper **A reproducible floating car data workflow for monitoring data-scarce highway corridors: A TomTom–HERE benchmark case study**. It provides Python scripts for cleaning TomTom and HERE floating car data, calculating traffic indicators, generating daily availability statistics, constructing TomTom–HERE matched benchmark samples, and running sensitivity analyses.

## 1. Scope of the package

The package covers the following processing tasks:

1. Cleaning TomTom 15 min point-level traffic records.
2. Calculating TomTom daily availability, missing rate, and cleaning outcomes.
3. Computing TomTom traffic indicators, including SRR, TTI, ETT, and CI.
4. Cleaning HERE 30 min block-level or segment-level traffic records.
5. Aggregating TomTom 15 min records to 30 min intervals.
6. Aggregating TomTom virtual monitoring points to three benchmark blocks.
7. Matching TomTom and HERE records by benchmark block and 30 min timestamp.
8. Calculating Pearson correlation, Spearman correlation, and Jaccard peak overlap.
9. Running threshold, temporal-lag, per-block, and daily-stratified sensitivity analyses.

## 2. Key methodological checks

### 2.1 Expected daily record count

The paper uses 42 TomTom virtual monitoring points and a 15 min sampling interval. One day contains 96 time slots:

```text
24 h × 4 = 96 slots per day
42 points × 96 slots = 4032 expected records per day
```

Therefore, the correct denominator is:

```text
R_exp = 4032
```

The older value `4038` should not be used.

### 2.2 Indicator formulas

The scripts implement the same indicator definitions as the manuscript:

```text
SRR = (V_ff - V_cur) / V_ff
TTI = TT_cur / TT_ff
ETT = TT_cur - TT_ff
CI  = (TT_cur - TT_ff) / TT_ff × 100
CI  = (TTI - 1) × 100
```

where:

- `V_cur` is current speed, in km/h.
- `V_ff` is free-flow speed, in km/h.
- `TT_cur` is current travel time, in seconds.
- `TT_ff` is free-flow travel time, in seconds.
- `SRR` is the speed reduction ratio.
- `TTI` is the travel time index.
- `ETT` is extra travel time, in seconds.
- `CI` is the congestion index, in percent.

The script also writes `ci_formula_abs_error` to check whether the two CI formulas give the same result. Under normal conditions, this error should be close to zero.

### 2.3 Missing data treatment

Missing observations are not interpolated. This is important because interpolation would create artificial continuity and inflate the reported availability rate. The availability statistics are based only on valid observed records.

### 2.4 Negative CI values

If `current_travel_time < free_flow_travel_time`, CI becomes negative. This may occur because of platform estimation fluctuation, free-flow baseline differences, or short periods with faster-than-free-flow estimates. The main scripts do not force negative CI values to zero. Instead, they add a quality flag named:

```text
ci_negative_flag
```

This approach preserves the original platform signal and improves reproducibility.

### 2.5 TomTom–HERE comparison scale

TomTom and HERE should not be compared at the raw interface level. The benchmark must be constructed at a common scale:

```text
TomTom 15 min point records
→ TomTom 30 min point records
→ TomTom 30 min benchmark-block records
→ matched with HERE 30 min benchmark-block records
```

The final matching key is:

```text
block_id + interval_30min
```

Only records with valid TomTom and HERE values in the same block and time interval are retained.

## 3. Recommended execution order

Install dependencies first:

```bash
pip install -r requirements.txt
```

Then run the complete workflow:

```bash
python scripts/00_run_all.py \
  --tomtom_raw data/tomtom_raw.csv \
  --here_raw data/here_raw.csv \
  --point_config config/tomtom_point_config.csv \
  --output_dir output
```

On Windows PowerShell, use:

```powershell
python scripts/00_run_all.py `
  --tomtom_raw data/tomtom_raw.csv `
  --here_raw data/here_raw.csv `
  --point_config config/tomtom_point_config.csv `
  --output_dir output
```

## 4. Minimum input table requirements

### 4.1 TomTom raw table

The TomTom raw table should include the following fields or equivalent aliases:

| Field | Meaning | Unit |
|---|---|---|
| `point_id` | TomTom virtual monitoring point ID | none |
| `timestamp` | Acquisition timestamp | datetime |
| `current_speed` | Current speed | km/h |
| `free_flow_speed` | Free-flow speed | km/h |
| `current_travel_time` | Current travel time | seconds |
| `free_flow_travel_time` | Free-flow travel time | seconds |
| `confidence` | Platform confidence value | optional |
| `road_closure` | Road closure status | optional |

### 4.2 HERE raw table

The HERE raw table should include the following fields:

| Field | Meaning | Unit |
|---|---|---|
| `block_id` | Benchmark block ID | none |
| `timestamp` | HERE record timestamp | datetime |
| `speed` | HERE speed | km/h |
| `jam_factor` | HERE jam factor | dimensionless |
| `confidence` | Platform confidence value | optional |
| `segment_id` | HERE segment ID | optional |

If the HERE raw table does not contain `block_id`, the segments must first be assigned to benchmark blocks using GIS processing or a spatial configuration file.

### 4.3 Point configuration table

The point configuration table should include at least:

| Field | Meaning |
|---|---|
| `point_id` | TomTom virtual monitoring point ID |
| `block_id` | Assigned benchmark block |
| `corridor_order` | Order along the corridor, optional |
| `latitude` | Point latitude, optional |
| `longitude` | Point longitude, optional |

## 5. Main output files

| Output file | Description |
|---|---|
| `tomtom_cleaned_point_time.csv` | Cleaned TomTom 15 min point-time table |
| `tomtom_daily_availability.csv` | Daily availability and missing-rate summary |
| `tomtom_cleaning_log_by_date.csv` | Daily cleaning log summary |
| `tomtom_duplicate_log.csv` | Duplicate TomTom records removed from the cleaned table |
| `tomtom_block_30min.csv` | TomTom 30 min benchmark-block table |
| `here_cleaned_block_time.csv` | Cleaned HERE 30 min benchmark-block table |
| `matched_tomtom_here_30min.csv` | Matched TomTom–HERE benchmark table |
| `benchmark_summary.csv` | Pooled and block-level benchmark statistics |
| `sensitivity_threshold.csv` | Peak-overlap threshold sensitivity results |
| `sensitivity_lag.csv` | Temporal-lag sensitivity results |
| `sensitivity_per_block.csv` | Per-block benchmark consistency results |
| `sensitivity_daily.csv` | Daily-stratified consistency results |

## 6. Quality-control rules

A TomTom record is retained in the cleaned dataset only when it satisfies the following conditions:

1. The timestamp can be parsed and mapped to the study timeline.
2. Core fields are present.
3. Current speed and free-flow speed are greater than zero.
4. Current travel time and free-flow travel time are greater than zero.
5. The record is not an excluded road-closure record.
6. The confidence field is retained for audit checking when available.

Duplicate records are identified using:

```text
point_id + interval_15min
```

This is more stable than using the raw timestamp alone because API timestamps may contain small second-level differences. The first valid record is retained, and removed duplicates are written to the duplicate log.

## 7. Benchmark statistics

The benchmark module reports three main consistency indicators.

### 7.1 Pearson correlation

Pearson correlation measures linear co-movement between TomTom mean CI and HERE mean jam factor.

### 7.2 Spearman correlation

Spearman correlation measures rank-based consistency. The scripts use:

```python
pandas.Series.corr(method="spearman")
```

This is preferred for real traffic data because ties are common.

### 7.3 Jaccard peak overlap

The Jaccard peak-overlap index measures whether the two platforms identify similar high-pressure periods. High-pressure periods are defined by an upper-quantile threshold, such as 80%, 85%, 90%, or 95%.

## 8. Sensitivity analyses

The package implements four sensitivity checks:

| Sensitivity analysis | Setting | Output |
|---|---|---|
| Threshold sensitivity | Upper quantiles: 0.80, 0.85, 0.90, 0.95 | Peak overlap under each threshold |
| Temporal-lag sensitivity | HERE shifted by -30, 0, and +30 min | Pearson, Spearman, and peak overlap |
| Per-block sensitivity | Block 1, Block 2, and Block 3 | Block-specific consistency statistics |
| Daily-stratified sensitivity | Each date in the benchmark window | Daily consistency statistics |

These results can be placed in Supporting Information if the main text has limited space.

## 9. Reproducibility and data-sharing notes

This package does not contain TomTom or HERE API keys. It also does not send API requests directly. It processes exported or archived traffic tables.

Raw API responses should not be redistributed if platform terms restrict redistribution. Instead, the recommended release package should include:

1. Cleaned TomTom point-time tables.
2. Cleaned HERE block-time tables.
3. Matched TomTom–HERE benchmark tables.
4. Spatial configuration files.
5. Processing scripts.
6. Figure reproduction data.
7. Data dictionary and README files.

This release structure is sufficient to reproduce the reported figures, tables, and benchmark statistics without exposing private API keys or restricted raw responses.

## 10. Items to verify with the final real data

Before submission, the following checks should be repeated using the final released CSV files:

1. Daily `observed_records` should not exceed 4032.
2. The maximum `ci_formula_abs_error` should be close to zero.
3. The TomTom–HERE matched sample count should match the manuscript.
4. The matched sample counts for Block 1, Block 2, and Block 3 should match the manuscript or be clearly explained.
5. Pearson correlation, Spearman correlation, and peak overlap should match the values reported in the manuscript.
6. The share of negative CI values should be checked. If it is high, the manuscript should explain possible free-flow baseline effects.
7. The conclusion should use `cross-platform consistency`, not `absolute accuracy` or `ground-truth validation`.

## 11. Suggested wording for the manuscript or repository

This repository provides the processing scripts and cleaned tables required to reproduce the traffic-state indicators, daily availability statistics, TomTom–HERE matched benchmark sample, and sensitivity analyses reported in the manuscript. The workflow cleans TomTom point-level floating car data, aggregates the 15 min observations to 30 min benchmark blocks, processes HERE benchmark records at the same block-time scale, and calculates cross-platform consistency metrics. The comparison is interpreted as a cross-platform signal consistency check rather than ground-truth validation, because no independent roadside detector was available.
